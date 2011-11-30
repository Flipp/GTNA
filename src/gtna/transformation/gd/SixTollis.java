/* ===========================================================
 * GTNA : Graph-Theoretic Network Analyzer
 * ===========================================================
 *
 * (C) Copyright 2009-2011, by Benjamin Schiller (P2P, TU Darmstadt)
 * and Contributors
 *
 * Project Info:  http://www.p2p.tu-darmstadt.de/research/gtna/
 *
 * GTNA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GTNA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * ---------------------------------------
 * SixTollis.java
 * ---------------------------------------
 * (C) Copyright 2009-2011, by Benjamin Schiller (P2P, TU Darmstadt)
 * and Contributors 
 *
 * Original Author: Nico;
 * Contributors:    -;
 *
 * Changes since 2011-05-17
 * ---------------------------------------
 *
 */
package gtna.transformation.gd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import gtna.graph.Edge;
import gtna.graph.Graph;
import gtna.graph.Node;
import gtna.id.ring.RingIdentifier;
import gtna.id.ring.RingIdentifierSpace;
import gtna.id.ring.RingPartition;
import gtna.metrics.EdgeCrossings;
import gtna.plot.GraphPlotter;

/**
 * @author Nico
 * 
 */
public class SixTollis extends CircularAbstract {
	private TreeSet<Node> waveCenterNodes, waveFrontNodes;
	private List<Node> nodeList, removedNodes;
	private HashMap<String, Edge> removalList;
	private HashMap<String, Edge>[] additionalEdges;
	private TreeNode deepestNode;
	private Boolean useOriginalGraphWithoutRemovalList = false;
	private Graph g;

	public SixTollis(int realities, double modulus, boolean wrapAround, GraphPlotter plotter) {
		super("GDA_SIX_TOLLIS", new String[] { "REALITIES", "MODULUS", "WRAPAROUND" }, new String[] { "" + realities,
				"" + modulus, "" + wrapAround });
		this.realities = realities;
		this.modulus = modulus;
		this.wrapAround = wrapAround;
		this.graphPlotter = plotter;
	}

	@Override
	public Graph transform(Graph g) {
		initIDSpace(g);
		if (graphPlotter != null)
			graphPlotter.plotStartGraph(g, idSpace);

		EdgeCrossings ec = new EdgeCrossings();
		int countCrossings;
		// countCrossings = ec.calculateCrossings(g.generateEdges(), idSpace,
		// true);
		// System.out.println("Crossings randomized: " + countCrossings);
		this.g = g;

		/*
		 * Phase 1
		 */
		Node tempNode = null;
		Node currentNode = null;
		Node lastNode = null;
		Node randDst1, randDst2;
		Edge tempEdge;
		String tempEdgeString;
		removalList = new HashMap<String, Edge>();
		HashMap<String, Edge> pairEdges = null;
		removedNodes = new ArrayList<Node>();
		waveCenterNodes = new TreeSet<Node>(new NodeComparator());
		waveFrontNodes = new TreeSet<Node>(new NodeComparator());

		additionalEdges = new HashMap[g.getNodes().length];
		for (int i = 0; i < g.getNodes().length; i++) {
			additionalEdges[i] = new HashMap<String, Edge>();
		}

		nodeList = Arrays.asList(g.getNodes().clone());
		Collections.sort(nodeList, new NodeDegreeComparator());
		for (int counter = 1; counter < (nodeList.size() - 3); counter++) {
			currentNode = getNode();
			pairEdges = getPairEdges(currentNode);
			for (Edge singleEdge : pairEdges.values()) {
				removalList.put(getEdgeString(singleEdge), singleEdge);
			}

			HashMap<String, Edge> currentNodeConnections = getEdges(currentNode);
			int currentNodeDegree = currentNodeConnections.size();
			int triangulationEdgesCount = (currentNodeDegree - 1) - pairEdges.size();
			int[] outgoingEdges = filterOutgoingEdges(currentNode, currentNodeConnections);

			// System.out.print(currentNode.getIndex() +
			// " has a current degree of " + currentNodeDegree + ", "
			// + pairEdges.size() + " pair edges and a need for " +
			// triangulationEdgesCount
			// + " triangulation edges - existing edges:");
			// for (Edge sE : currentNodeConnections.values()) {
			// System.out.print(" " + sE);
			// }
			// System.out.println();

			int firstCounter = 0;
			int secondCounter = 1;
			while (triangulationEdgesCount > 0) {
				randDst1 = g.getNode(outgoingEdges[firstCounter]);
				randDst2 = g.getNode(outgoingEdges[secondCounter]);
				if (randDst1.equals(randDst2))
					continue;
				if (removedNodes.contains(randDst1) || removedNodes.contains(randDst2)) {
					continue;
				}

				// System.out.println("rand1: " + randDst1.getIndex() +
				// "; rand2: " + randDst2.getIndex());
				// System.out.print("Outgoing edges for r1:");
				// for (int i : filterOutgoingEdges(randDst1,
				// getEdges(randDst1))) {
				// System.out.print(" " + i);
				// }
				// System.out.println("");

				if (!connected(randDst1, randDst2)) {
					tempEdge = new Edge(Math.min(randDst1.getIndex(), randDst2.getIndex()), Math.max(
							randDst1.getIndex(), randDst2.getIndex()));
					tempEdgeString = getEdgeString(tempEdge);
					if (randDst1.getIndex() != randDst2.getIndex()
							&& !additionalEdges[randDst1.getIndex()].containsKey(tempEdgeString)) {
						// System.out.println("Adding triangulation edge " +
						// tempEdge);
						additionalEdges[randDst1.getIndex()].put(tempEdgeString, tempEdge);
						additionalEdges[randDst2.getIndex()].put(tempEdgeString, tempEdge);
						triangulationEdgesCount--;
					}
				} else {
					// System.out.println("Node " + randDst1.getIndex() +
					// " is already connected to "
					// + randDst2.getIndex());
				}

				secondCounter++;
				if (secondCounter == (currentNodeDegree - 1)) {
					firstCounter++;
					secondCounter = firstCounter + 1;
				}

				if (firstCounter == (currentNodeDegree - 1))
					throw new RuntimeException("Could not find anymore pair edges for " + currentNode.getIndex());
			}

			/*
			 * Keep track of wave front and wave center nodes!
			 */

			waveFrontNodes = new TreeSet<Node>(new NodeComparator());
			for (Edge i : getEdges(currentNode).values()) {
				int otherEnd;
				if (i.getDst() == currentNode.getIndex()) {
					otherEnd = i.getSrc();
				} else {
					otherEnd = i.getDst();
				}
				tempNode = g.getNode(otherEnd);
				if (removedNodes.contains(tempNode)) {
					continue;
				}
				waveFrontNodes.add(tempNode);
				waveCenterNodes.add(tempNode);
			}
			lastNode = currentNode;
			removedNodes.add(currentNode);
			// System.out.println("Adding " + currentNode.getIndex() +
			// " to removedNodes");
		}

		/*
		 * Do the DFS here
		 */
		LinkedList<Node> longestPath = longestPath();

		/*
		 * Check which nodes still need to be placed, as they do not lie on the
		 * longestPath
		 */
		ArrayList<Node> todoList = new ArrayList<Node>();
		todoList.addAll(nodeList);
		todoList.removeAll(longestPath);
		
		Node neighbor, singleNode;
		int neighborPosition = -1;
		int errors = 0;
		int modCounter = 0;
		while (!todoList.isEmpty()) {
			singleNode = todoList.get(modCounter % todoList.size());
			for (int singleNeighbor : singleNode.getOutgoingEdges()) {
				neighbor = g.getNode(singleNeighbor);
				neighborPosition = longestPath.indexOf(neighbor);
				if (neighborPosition > -1) {
					break;
				}
			}
			if (neighborPosition != -1) {
				todoList.remove(singleNode);
				longestPath.add(neighborPosition, singleNode);
			} else {
				modCounter = (modCounter + 1) % todoList.size();
				System.err.println("Cannot place " + singleNode + " yet, errors=" + errors);
				if (errors++ == 50) {
					System.exit(0);
				}
			}
		}

		partitions = new RingPartition[g.getNodes().length];
		idSpace = new RingIdentifierSpace(partitions, this.modulus, this.wrapAround);
		RingIdentifier[] ids = new RingIdentifier[partitions.length];
		double lastPos = 0;
		double posDiff = modulus / partitions.length;
		for (int i = 0; i < partitions.length; i++) {
			ids[i] = new RingIdentifier(lastPos, idSpace);
			lastPos += posDiff;
		}
		for (int i = 0; i < partitions.length; i++) {
			partitions[i] = new RingPartition(ids[i], ids[(i + 1) % ids.length]);
		}

		writeIDSpace(g);

		if (graphPlotter != null)
			graphPlotter.plotFinalGraph(g, idSpace);
		// countCrossings = ec.calculateCrossings(g.generateEdges(), idSpace,
		// true);
		// System.out.println("Crossings enhanced: " + countCrossings);

		return g;
	}

	private ArrayList<Edge> getAllEdges(Node n) {
		ArrayList<Edge> edges = new ArrayList<Edge>();
		for (Edge e : n.generateAllEdges())
			edges.add(e);
		if (!useOriginalGraphWithoutRemovalList) {
			edges.addAll(additionalEdges[n.getIndex()].values());
		}
		return edges;
	}

	private HashMap<String, Edge> getEdges(Node n) {
		Node tempNode = null;
		HashMap<String, Edge> edges = new HashMap<String, Edge>();

		for (Edge i : getAllEdges(n)) {
			int otherEnd;
			if (i.getDst() == n.getIndex()) {
				otherEnd = i.getSrc();
			} else {
				otherEnd = i.getDst();
			}
			tempNode = g.getNode(otherEnd);
			if (!useOriginalGraphWithoutRemovalList && removedNodes.contains(tempNode)) {
				continue;
			}
			edges.put(getEdgeString(i), i);
		}
		return edges;
	}

	private int[] filterOutgoingEdges(Node n, HashMap<String, Edge> edges) {
		int[] result = new int[edges.size()];
		int edgeCounter = 0;
		for (Edge sE : edges.values()) {
			int otherEnd;
			if (sE.getDst() == n.getIndex()) {
				otherEnd = sE.getSrc();
			} else {
				otherEnd = sE.getDst();
			}
			result[edgeCounter++] = otherEnd;
		}
		return result;
	}

	private Boolean connected(Node n, Node m) {
		int[] edges = filterOutgoingEdges(n, getEdges(n));
		for (int sE : edges) {
			if (sE == m.getIndex())
				return true;
		}
		return false;
	}

	private LinkedList<Node> longestPath() {
		LinkedList<Node> result = new LinkedList<Node>();

		useOriginalGraphWithoutRemovalList = true;
		Node start = removedNodes.get(0);
		TreeNode root = new TreeNode(null, start.getIndex(), 0);
		deepestNode = root;
		dfs(start, root, new ArrayList<Integer>());

		/*
		 * Create the path now by doing a second dfs starting from the
		 * deepestNode
		 */
		TreeNode sourceOfLongestPath = new TreeNode(null, deepestNode.index, 0);
		deepestNode = sourceOfLongestPath;
		dfs(g.getNode(sourceOfLongestPath.index), sourceOfLongestPath, new ArrayList<Integer>());
		/*
		 * Find the path between sourceOfLongestPath and deepestNode
		 */
		do {
			result.push(g.getNode(deepestNode.index));
			deepestNode = deepestNode.root;
		} while (!deepestNode.equals(sourceOfLongestPath));
		return result;
	}

	private void dfs(Node n, TreeNode root, ArrayList<Integer> visited) {
		int otherEnd;

		if (visited.contains(n.getIndex())) {
			return;
		}

		visited.add(n.getIndex());
		TreeNode current = new TreeNode(root, n.getIndex(), root.depth + 1);
		root.children.add(current);
		if (current.depth > deepestNode.depth) {
			deepestNode = current;
		}

		for (Edge mEdge : getEdges(n).values()) {
			if (mEdge.getDst() == n.getIndex()) {
				otherEnd = mEdge.getSrc();
			} else {
				otherEnd = mEdge.getDst();
			}
			Node mNode = g.getNode(otherEnd);
			dfs(mNode, current, visited);
		}
	}

	/**
	 * @return
	 */
	private Node getNode() {
		/*
		 * Retrieve any wave front node...
		 */
		if (waveFrontNodes != null) {
			for (Node tempNode : waveFrontNodes) {
				if (!removedNodes.contains(tempNode)) {
					waveFrontNodes.remove(tempNode);
					return tempNode;
				}
			}
		}
		/*
		 * ...or a wave center node...
		 */
		if (waveCenterNodes != null) {
			for (Node tempNode : waveCenterNodes) {
				if (!removedNodes.contains(tempNode)) {
					waveCenterNodes.remove(tempNode);
					return tempNode;
				}
			}
		}
		/*
		 * ...or any lowest degree node
		 */
		for (Node tempNode : nodeList) {
			if (!removedNodes.contains(tempNode)) {
				return tempNode;
			}
		}
		throw new RuntimeException("No node left");
	}

	private HashMap<String, Edge> getPairEdges(Node n) {
		HashMap<String, Edge> result = new HashMap<String, Edge>();
		Node tempInnerNode;
		Edge tempEdge;
		int otherOuterEnd, otherInnerEnd;

		// System.out.println("\n\nCalling getPairEdges for node " +
		// n.getIndex());
		HashMap<String, Edge> allOuterEdges = getEdges(n);
		for (Edge tempOuterEdge : allOuterEdges.values()) {
			// System.out.println("\n");
			if (tempOuterEdge.getDst() == n.getIndex()) {
				otherOuterEnd = tempOuterEdge.getSrc();
			} else {
				otherOuterEnd = tempOuterEdge.getDst();
			}
			// System.out.println("For the edge " + tempOuterEdge + ", " +
			// otherOuterEnd + " is the other node");
			HashMap<String, Edge> allInnerEdges = getEdges(g.getNode(otherOuterEnd));
			for (Edge tempInnerEdge : allInnerEdges.values()) {
				// System.out.println(tempInnerEdge + " is an edge for " +
				// otherOuterEnd);
				if (tempInnerEdge.getDst() == otherOuterEnd) {
					otherInnerEnd = tempInnerEdge.getSrc();
				} else {
					otherInnerEnd = tempInnerEdge.getDst();
				}
				if (otherInnerEnd == n.getIndex()) {
					continue;
				}
				tempInnerNode = g.getNode(otherInnerEnd);
				if (connected(n, tempInnerNode)) {
					tempEdge = new Edge(Math.min(otherInnerEnd, otherOuterEnd), Math.max(otherInnerEnd, otherOuterEnd));
					// System.out.println(getEdgeString(tempEdge) +
					// " is a pair edge of " + otherOuterEnd + " and " +
					// otherInnerEnd);
					result.put(getEdgeString(tempEdge), tempEdge);
				} else {
					// System.out.println("No pair edge between " +
					// otherOuterEnd + " and " + otherInnerEnd);
				}
			}
		}

		return result;
	}

	private class NodeDegreeComparator implements Comparator<Node> {
		@Override
		public int compare(Node n1, Node n2) {
			if (n1.getDegree() == n2.getDegree())
				return 0;
			else if (n1.getDegree() > n2.getDegree())
				return 1;
			else
				return -1;
		}
	}

	private String getEdgeString(Edge e) {
		return Math.min(e.getSrc(), e.getDst()) + "->" + Math.max(e.getSrc(), e.getDst());
	}

	private class NodeComparator implements Comparator<Node> {
		@Override
		public int compare(Node n1, Node n2) {
			if (n1 == null || n2 == null || n1.getIndex() == n2.getIndex())
				return 0;
			else if (n1.getIndex() > n2.getIndex())
				return 1;
			else
				return -1;
		}
	}

	private class TreeNode {
		public TreeNode root;
		public int index, depth;
		public ArrayList<TreeNode> children;

		public TreeNode(TreeNode root, int index, int depth) {
			this.root = root;
			this.index = index;
			this.depth = depth;
			this.children = new ArrayList<TreeNode>();
		}

		public boolean equals(TreeNode x) {
			return (x.index == this.index);
		}
	}
}
