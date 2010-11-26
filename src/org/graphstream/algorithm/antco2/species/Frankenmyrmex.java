/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.graphstream.algorithm.antco2.species;

import java.util.LinkedList;

import org.graphstream.algorithm.antco2.Ant;
import org.graphstream.algorithm.antco2.AntCo2Edge;
import org.graphstream.algorithm.antco2.AntCo2Node;
import org.graphstream.algorithm.antco2.AntContext;
import org.graphstream.algorithm.antco2.AntParams;
import org.graphstream.algorithm.antco2.Colony;

/**
 * Doctor Frankenstein's myrmex.
 * 
 * @author Antoine Dutot
 * @since 2 nov. 2005
 */
public class Frankenmyrmex extends Ant {
	// Attributes

	/**
	 * Ant memory.
	 */
	protected LinkedList<AntCo2Node> mem = new LinkedList<AntCo2Node>();

	/**
	 * Perceived pheromone array. This should be alloced at each call to step(),
	 * but to avoid such an overhead, an over-sized array is created and only
	 * re-allocated when it is too small.
	 */
	protected float P[] = new float[30];

	// Constructors

	public Frankenmyrmex(String id, Colony colony, AntCo2Node startNode,
			AntContext context) {
		super(id, colony, startNode, context);
	}

	// Accessors

	public float getPheromonDrop() {
		return 0.1f;
	}

	/**
	 * Is the given node in the ant memory?.
	 * 
	 * @param node
	 *            The node to test.
	 * @return True if the ant remembers the node.
	 */
	protected boolean isRemembered(AntCo2Node node) {
		return mem.contains(node);
	}

	// Commands

	protected float perceivedPheromonValue(AntCo2Edge edge) {
		return 0;
	}

	protected float correction(AntCo2Edge edge) {
		return 1;
	}

	protected AntCo2Edge chooseEdge(float P[], float totalP, int nEdges) {
		if (totalP > 0) {
			float r = ctx.random().nextFloat();
			float s = 0;

			for (int i = 0; i < nEdges; ++i) {
				s += P[i] / totalP;

				if (s >= r)
					return (AntCo2Edge) curNode.getEdge(i);
			}

			return (AntCo2Edge) curNode.getEdge(nEdges - 1);
		} else {
			int r = ctx.random().nextInt(nEdges);

			return (AntCo2Edge) curNode.getEdge(r);
		}

		// throw new RuntimeException( "No Edge Choosen!!!" );
	}

	protected int forHowLong = 0;

	public void step() {
		// This behavior make the ant choose arcs randomly and jump if it is
		// in an environment where too many ants of other colonies are.

		int nEdges = curNode.getDegree(); // Number of edges.
		float totalPerceived = 0; // Perceived pheromone total on all incident
									// edges.
		float totalPop = 0; // Total population of the ant color on all incident
							// edges.
		float totalMyColor = 0; // Pheromone total of color c on all incident
								// edges, with c = ant color.
		float total = 0; // Pheromone total on all indicent edges.
		AntParams params = ctx.getAntParams();
		AntCo2Edge next = null; // Next edge to cross.

		if (P.length <= nEdges)
			P = new float[nEdges];

		if (nEdges <= 0) {
			jumpRandomly();
		} else {
			for (int i = 0; i < nEdges; ++i) {
				AntCo2Edge edge = (AntCo2Edge) curNode.getEdge(i);

				// P[i] = (float) Math.pow( perceivedPheromonValue( edge ),
				// params.alpha )
				// * (float) Math.pow( info.getValue(), params.beta )
				// * correction( edge );
				P[i] = (float) Math.pow(edge.getValue(), params.beta)
						* correction(edge);

				totalPerceived += P[i];
				totalMyColor += edge.getPheromon(colony.getIndex());
				total += edge.getPheromonTotal();
				totalPop += ((AntCo2Node) edge.getOpposite(curNode))
						.getAntCountForColor(colony);
				;
			}

			totalPop += curNode.getAntCountForColor(colony);

			float overpop = totalPop / (nEdges + 1);
			float agoraphobia = totalMyColor / total;

			// System.out.printf( " [%f/%d] (%f)%n", overpop,
			// params.overPopulated, totalPop );

			if (forHowLong < params.jump) {
				forHowLong++;

				if (overpop > params.overPopulated) {
					ctx.incrSurpop();
					jumpRandomly();
					forHowLong = 0;
				} else {
					next = chooseEdge(P, totalPerceived, nEdges);
					cross(next, true);
				}
			} else {
				if (agoraphobia <= params.agoraphobia) {
					jumpRandomly();
					forHowLong = 0;
				} else if (overpop > params.overPopulated) {
					ctx.incrSurpop();
					jumpRandomly();
					forHowLong = 0;
				} else {
					next = chooseEdge(P, totalPerceived, nEdges);
					cross(next, true);
				}
			}
		}
	}

	/**
	 * Redefine the method to add the new current node in the ant memory (if
	 * memory is enabled).
	 * 
	 * @param edge
	 *            The edge to cross.
	 * @param depositPheromon
	 *            Does pheromon is to be deposited on the crossed edge?.
	 */
	public void cross(AntCo2Edge edge, boolean depositPheromon) {
		super.cross(edge, depositPheromon);
		remember(curNode);
	}

	/**
	 * Add the given node to the ant memory.
	 * 
	 * @param node
	 *            The node to remember.
	 */
	protected void remember(AntCo2Node node) {
		mem.addLast(node);

		if (mem.size() > ctx.getAntParams().mem)
			mem.removeFirst();
	}
}