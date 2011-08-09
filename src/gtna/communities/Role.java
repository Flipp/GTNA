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
 * Role.java
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
package gtna.communities;

import gtna.util.Config;

import java.util.ArrayList;

/**
 * @author benni
 * 
 */
public class Role {
	// TODO check names...

	public static final byte ULTRA_PERIPHERAL = 1;

	public static final byte PERIPHERAL = 2;

	public static final byte SATTELITE_CONNECTOR = 3;

	public static final byte KINLESS_NODE = 4;

	public static final byte PRINCIPAL_HUB = 5;

	public static final byte CONNECTOR_HUP = 6;

	public static final byte GLOBAL_HUB = 7;

	private byte type;

	private int[] nodes;

	public Role(byte type) {
		this(type, new int[] {});
	}

	public Role(byte type, int[] nodes) {
		this.type = type;
		this.nodes = nodes;
	}

	public Role(byte type, ArrayList<Integer> nodes) {
		this.type = type;
		this.nodes = new int[nodes.size()];
		for (int i = 0; i < nodes.size(); i++) {
			this.nodes[i] = nodes.get(i);
		}
	}

	public Role(String stringRepresentation) {
		String sep1 = Config.get("GRAPH_PROPERTY_SEPARATOR_1");
		String sep2 = Config.get("GRAPH_PROPERTY_SEPARATOR_2");
		String temp1[] = stringRepresentation.split(sep1);
		this.type = Byte.parseByte(temp1[0]);
		if (temp1.length < 2 || temp1[1].length() == 0) {
			this.nodes = new int[] {};
		} else {
			String[] temp2 = temp1[1].split(sep2);
			this.nodes = new int[temp2.length];
			for (int i = 0; i < temp2.length; i++) {
				this.nodes[i] = Integer.parseInt(temp2[i]);
			}
		}
	}

	public String getStringRepresentation() {
		String sep1 = Config.get("GRAPH_PROPERTY_SEPARATOR_1");
		String sep2 = Config.get("GRAPH_PROPERTY_SEPARATOR_2");
		StringBuffer buff = new StringBuffer(type + sep1);
		if (this.nodes.length == 0) {
			return buff.toString();
		}
		buff.append(this.nodes[0]);
		for (int i = 1; i < this.nodes.length; i++) {
			buff.append(sep2 + this.nodes[i]);
		}
		return buff.toString();
	}

	/**
	 * @return the type
	 */
	public byte getType() {
		return this.type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(byte type) {
		this.type = type;
	}

	/**
	 * @return the nodes
	 */
	public int[] getNodes() {
		return this.nodes;
	}

	/**
	 * @param nodes
	 *            the nodes to set
	 */
	public void setNodes(int[] nodes) {
		this.nodes = nodes;
	}
}
