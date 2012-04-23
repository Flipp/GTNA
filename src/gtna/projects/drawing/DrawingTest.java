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
 * Test.java
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
package gtna.projects.drawing;

import gtna.drawing.Gephi;
import gtna.graph.Graph;
import gtna.id.IdentifierSpace;
import gtna.networks.Network;
import gtna.networks.model.ErdosRenyi;
import gtna.transformation.Transformation;
import gtna.transformation.id.RandomRingIDSpace;

/**
 * @author benni
 *
 */
public class DrawingTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Network nw = new ErdosRenyi(100, 10, true, null);
		Graph g = nw.generate();
		Transformation t = new RandomRingIDSpace();
		g = t.transform(g);
		
		Gephi gephi = new Gephi();
		gephi.plot(g, (IdentifierSpace) g.getProperty("ID_SPACE_0"), "./plots/gephi.png");
	}

}
