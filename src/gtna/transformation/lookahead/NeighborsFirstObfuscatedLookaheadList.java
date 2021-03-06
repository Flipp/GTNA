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
 * NeighborsFirstLookaheadList.java
 * ---------------------------------------
 * (C) Copyright 2009-2011, by Benjamin Schiller (P2P, TU Darmstadt)
 * and Contributors 
 *
 * Original Author: benni;
 * Contributors:    -;
 *
 * Changes since 2011-05-17
 * ---------------------------------------
 *
 */
package gtna.transformation.lookahead;

import gtna.graph.Graph;
import gtna.graph.GraphProperty;
import gtna.graph.Node;
import gtna.id.IdentifierSpace;
import gtna.id.lookahead.LookaheadElement;
import gtna.id.lookahead.LookaheadList;
import gtna.id.lookahead.LookaheadLists;
import gtna.util.parameter.BooleanParameter;
import gtna.util.parameter.Parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * @author benni
 * 
 */
public class NeighborsFirstObfuscatedLookaheadList extends
		ObfuscatedLookaheadList {
	protected boolean randomizeOrder;

	public NeighborsFirstObfuscatedLookaheadList(double minEpsilon,
			double maxEpsilon, boolean randomizeOrder) {
		super("NEIGHBORS_FIRST_OBFUSCATED_LOOKAHEAD_LIST", minEpsilon,
				maxEpsilon, new Parameter[] { new BooleanParameter(
						"RANDOMIZE_ORDER", randomizeOrder) });
		this.randomizeOrder = randomizeOrder;
	}

	public NeighborsFirstObfuscatedLookaheadList(int minBits, int maxBits,
			boolean randomizeOrder) {
		super("NEIGHBORS_FIRST_OBFUSCATED_LOOKAHEAD_LIST", minBits, maxBits,
				new Parameter[] { new BooleanParameter("RANDOMIZE_ORDER",
						randomizeOrder) });
		this.randomizeOrder = randomizeOrder;
	}

	protected NeighborsFirstObfuscatedLookaheadList(String key,
			boolean randomizeOrder) {
		super(key, new Parameter[] { new BooleanParameter("RANDOMIZE_ORDER",
				randomizeOrder) });
		this.randomizeOrder = randomizeOrder;
	}

	@Override
	public Graph transform(Graph g) {
		Random rand = new Random();
		GraphProperty[] gps = g.getProperties("ID_SPACE");
		for (GraphProperty p : gps) {
			@SuppressWarnings("rawtypes")
			IdentifierSpace ids = (IdentifierSpace) p;
			ArrayList<LookaheadList> lists = new ArrayList<LookaheadList>();
			for (Node n : g.getNodes()) {
				ArrayList<LookaheadElement> list = new ArrayList<LookaheadElement>();

				// add neighbors
				ArrayList<LookaheadElement> neighbors = new ArrayList<LookaheadElement>();
				for (int outIndex : n.getOutgoingEdges()) {
					neighbors.add(new LookaheadElement(
							ids.getPartitions()[outIndex], outIndex));
				}
				if (this.randomizeOrder) {
					Collections.shuffle(neighbors);
				}
				list.addAll(neighbors);
				if (this.randomizeOrder) {
					Collections.shuffle(neighbors);
				}

				// add neighbors' neighbors
				for (LookaheadElement neighbor : neighbors) {
					Node out = g.getNode(neighbor.getVia());
					ArrayList<LookaheadElement> lookahead = new ArrayList<LookaheadElement>();
					for (int lookaheadIndex : out.getOutgoingEdges()) {
						if (lookaheadIndex == n.getIndex()) {
							continue;
						}
						lookahead.add(new LookaheadElement(this
								.obfuscatePartition(
										ids.getPartitions()[lookaheadIndex],
										rand), neighbor.getVia()));
					}
					if (this.randomizeOrder) {
						Collections.shuffle(lookahead);
					}
					list.addAll(lookahead);
				}
				lists.add(new LookaheadList(n.getIndex(), list));
			}
			g.addProperty(g.getNextKey("LOOKAHEAD_LIST"), new LookaheadLists(
					lists));
		}
		return g;
	}

}
