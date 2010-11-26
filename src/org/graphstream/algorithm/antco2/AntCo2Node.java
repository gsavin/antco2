/*
 * This file is part of AntCo2.
 * 
 * AntCo2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * AntCo2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with AntCo2.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2009 - 2010
 * 	Antoine Dutot
 * 	Guilhelm Savin
 */
package org.graphstream.algorithm.antco2;

import java.util.Arrays;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.AdjacencyListNode;

/**
 * Defines a node position for ants.
 * 
 * @author adutot, gsavin
 * 
 */
public class AntCo2Node extends AdjacencyListNode {
	/**
	 * Colony of ants owning this node.
	 */
	protected Colony color;

	/**
	 * Previous colony owning this node.
	 */
	protected Colony oldColor;

	/**
	 * Second colony owning this node.
	 */
	protected Colony secondColor;

	/**
	 * Ratio between pheromones rate of the colony owning the node and
	 * pheromones rate of the second colony owning the node.
	 */
	protected float colorRatio;

	/**
	 * Number of ants of each colour. Indices in this array maps to indices in
	 * the colonies.
	 */
	protected int[] antCountsPerColor;

	/**
	 * Same as {@link #antCountsPerColor}, but used by commit.
	 */
	protected int[] antCountsPerColorTmp;

	/**
	 * Total ant count of all colours on this node.
	 */
	protected int totalAntCount;

	/**
	 * Same as {@link #totalAntCount}, but used by commit.
	 */
	protected int totalAntCountTmp;

	/**
	 * Flag indicating if commit is needed.
	 */
	protected boolean needCommit = false;

	/**
	 * Allows to attribute a value to this node.
	 */
	protected float value;

	/**
	 * Is this node on the membrane of its organization. If true, this means
	 * that at least one of its neightbor is not from the same organization.
	 */
	protected boolean membrane;

	/**
	 * Constructor of an AntCo2Node.
	 * 
	 * @param ctx
	 *            ants context
	 * @param colony
	 *            initial node colony
	 * @param g
	 *            graph
	 * @param id
	 *            id of the node
	 */
	public AntCo2Node(AntContext ctx, Colony colony, Graph g, String id) {
		super(g, id);

		setColor(color);
		antCountsPerColor = new int[ctx.getColonyCount()];
		antCountsPerColorTmp = new int[ctx.getColonyCount()];
	}

	/**
	 * Attribute a new value to the node.
	 * 
	 * @param value
	 */
	public void setValue(float value) {
		this.value = value;
	}

	/**
	 * Retrieve the value attributed to the node.
	 * 
	 * @return the value attributed to the node
	 */
	public float getValue() {
		return value;
	}

	/**
	 * Colour of the node.
	 * 
	 * @return The colour.
	 */
	public Colony getColor() {
		return color;
	}

	/**
	 * Second most important colour of the node.
	 * 
	 * @return The second colour.
	 */
	public Colony getSecondColor() {
		return secondColor;
	}

	/**
	 * Importance of the second colour compared to the primary colour. This
	 * number is a float between 0 and 1 included representing the presence of
	 * the second colour. 0 means the second is not present, 1 means the second
	 * colour is as much present as the primary one.
	 * 
	 * @return The colour ratio.
	 */
	public float getColorRatio() {
		return colorRatio;
	}

	/**
	 * Previous colour of the node after it has changed colour.
	 * 
	 * @return The previous colour.
	 */
	public Colony getOldColor() {
		return oldColor;
	}

	/**
	 * Total ant count on this node during the last step.
	 * 
	 * @return The ant count.
	 */
	public int getTotalAntCount() {
		return totalAntCount;
	}

	/**
	 * Number of ants of a given colour on this node during the last step.
	 * 
	 * @param color
	 *            The colour.
	 * @return The corresponding number of ants on this node.
	 */
	public int getAntCountForColor(Colony color) {
		int index = color.getIndex();

		if (index >= antCountsPerColor.length)
			return 0;

		return antCountsPerColor[index];
	}

	/**
	 * Change the colour of the node. Can only be called at the end of an AntCO²
	 * step.
	 * 
	 * @param newColor
	 *            The colour to set.
	 */
	public void setColor(Colony newColor) {
		// if( needCommit )
		// throw new IllegalStateException(
		// "cannot add a color if the NodeInfo is not commited" );

		if (newColor != null) {
			if (newColor != color) {

				Colony oldColor = color;
				color = newColor;

				if (oldColor != null)
					oldColor.unregisterNode(this);

				if (newColor != null)
					newColor.registerNode(this);
			}
		}
	}

	/**
	 * Commit changes.
	 */
	public void commit() {
		if (needCommit) {
			totalAntCount = totalAntCountTmp;

			for (int i = 0; i < antCountsPerColor.length; ++i)
				antCountsPerColor[i] = antCountsPerColorTmp[i];

			needCommit = false;
		}
	}

	/**
	 * Submit a new color for the node. This can be overriden by subclasses to
	 * introduce mechanisms between color-submission and color-changes.
	 * 
	 * @param ctx
	 * @param newColor
	 */
	protected void submitColor(AntContext ctx, Colony newColor) {
		// setColor(newColor);
		ctx.getSmoothingBox().submitColor(this, color, newColor);
	}

	/**
	 * Step this node.
	 * 
	 * @param ctx
	 */
	public void step(AntContext ctx) {
		checkMembrane();
		checkColorChange(ctx);
		commit();
	}

	/**
	 * Get edges adjacent to this node.
	 * 
	 * @return an iterable on adjacent edges
	 */
	public Iterable<? extends AntCo2Edge> eachEdge() {
		return getEdgeSet();
	}

	/**
	 * An ant arrived on this node. This only update temporary informations. Use
	 * {@link #commit()} to update. Commit should be called only at the end of
	 * each AntCO² step.
	 * 
	 * @param ant
	 *            The ant to register.
	 */
	public void registerAnt(Ant ant) {
		needCommit = true;
		totalAntCountTmp += 1;

		int index = ant.getColony().getIndex();
		checkColorArraySizes(index);
		antCountsPerColorTmp[index]++;
	}

	/**
	 * An ant left this node. This only update temporary informations. Use
	 * {@link #commit()} to update. Commit should be called only at the end of
	 * each AntCO² step.
	 * 
	 * @param ant
	 *            The ant to de-register.
	 */
	public void unregisterAnt(Ant ant) {
		needCommit = true;
		totalAntCountTmp -= 1;

		int index = ant.getColony().getIndex();
		checkColorArraySizes(index);
		antCountsPerColorTmp[index] -= 1;
	}

	/**
	 * Check if the node changed colour by looking at each incident edge
	 * dominant pheromone.
	 */
	protected void checkColorChange(AntContext ctx) {
		int colors = ctx.getColonyCount();
		float values[] = new float[colors];

		// First compute the global pheromone levels for all incident edges.

		for (AntCo2Edge edge : eachEdge()) {
			for (int c = 0; c < colors; ++c) {
				if (ctx.getColony(c) != null)
					values[c] += edge.getPheromon(c);
				else
					values[c] = 0;
			}
		}

		// Then find the max level (primary and secondary colours).

		float valueMax = 0;
		int maxIndex = -1;
		float secondValueMax = 0;
		int secondMaxIndex = -1;

		for (int c = 0; c < colors; ++c) {
			if (ctx.getColony(c) != null) {
				if (values[c] > valueMax) {
					maxIndex = c;
					valueMax = values[c];
				}
			}
		}

		for (int c = 0; c < colors; ++c) {
			if (ctx.getColony(c) != null) {
				if (c != maxIndex) {
					if (values[c] > secondValueMax) {
						secondMaxIndex = c;
						secondValueMax = values[c];
					}
				}
			}
		}

		if (maxIndex >= 0) {
			Colony newColor = ctx.getColony(maxIndex);
			/*
			 * if( newColor != null ) { if( newColor != color ) { oldColor =
			 * color; color = newColor;
			 * 
			 * if( oldColor != null ) oldColor.unregisterNode( this );
			 * 
			 * if( newColor != null ) newColor.registerNode( this ); } else {
			 * oldColor = color; } } else
			 * System.err.printf("warning: newcolor is null\n");
			 */

			// setColor(newColor);
			submitColor(ctx, newColor);
		}

		if (secondMaxIndex >= 0) {
			Colony newColor = ctx.getColony(secondMaxIndex);

			if (newColor != null) {
				if (newColor != secondColor)
					secondColor = newColor;

				colorRatio = (secondValueMax / valueMax);
			} else
				System.err.printf("warning: second newcolor is null\n");
		}
	}

	/**
	 * Check that the antCountPerColor arrays are large enough.
	 * 
	 * @param index
	 */
	protected void checkColorArraySizes(int index) {
		if (index >= antCountsPerColor.length)
			resizeArrays(index + 1);
	}

	/**
	 * Resize arrays.
	 * 
	 * @param newSize
	 */
	protected void resizeArrays(int newSize) {
		antCountsPerColorTmp = Arrays.copyOf(antCountsPerColorTmp, newSize);
		antCountsPerColor = Arrays.copyOf(antCountsPerColor, newSize);
	}

	/**
	 * Check if this node is on the membrane of its organizations.
	 */
	protected void checkMembrane() {
		membrane = false;

		for (AntCo2Edge edge : eachEdge()) {
			if (edge.isCutEdge()) {
				membrane = true;
				break;
			}
		}
	}

	/**
	 * Access to the membrane-attribute of this node.
	 * 
	 * @return true if node is on the membrane of its organization
	 */
	public boolean isMembrane() {
		return membrane;
	}
}
