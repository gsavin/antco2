/*
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */

package org.miv.antco2;

import java.util.Iterator;

import org.miv.graphstream.graph.*;

import book.set.IntArray;

/**
 * Node parameters.
 * 
 * <p>
 * Node parameters define the colour of a node, the total number of ants present on it, and ant
 * counts for each colour. Some methods ({@link #registerAnt(Ant)} and {@link #unregisterAnt(Ant)}
 * work in two phases. First the user modify them by calling them without in fact modifying the ant
 * counts as reported by {@link #getAntCountForColor(Colony)} and {@link #getTotalAntCount()}. Then
 * the user calls {@link #commit()} to apply changes to the ant counts.
 * </p>
 * 
 * @author Antoine Dutot
 */
public class NodeInfos extends GraphInfos
{
// Constants

	/**
	 * Used to retrieve this attribute in a graph.
	 */
	public static final String ATTRIBUTE_NAME = "NodeInfos";

// Attributes

	/**
	 * Colour of the node (colony having the maximum colour on all incident edges).
	 */
	protected Colony color;

	/**
	 * Old colour of the node. This keeps the previous colour of the node when the node changes
	 * colour.
	 */
	protected Colony oldColor;

	/**
	 * Second colour of the node (second colony having the maximum colour on all incident edges).
	 */
	protected Colony secondColor;

	/**
	 * Importance of the second colour compared to the primary colour. This number is a float
	 * between 0 and 1 included representing the presence of the second colour. 0 means the second
	 * is not present, 1 means the second colour is as much present as the primary one.
	 */
	protected float colorRatio;

	/**
	 * Total ant count of all colours on this node.
	 */
	protected int totalAntCount;

	/**
	 * Same as {@link #totalAntCount}, but used by commit.
	 */
	protected int totalAntCountTmp;

	/**
	 * Number of ants of each colour. Indices in this array maps to indices in the colonies.
	 */
	protected IntArray antCountsPerColor = new IntArray();

	/**
	 * Same as {@link #antCountsPerColor}, but used by commit.
	 */
	protected IntArray antCountsPerColorTmp = new IntArray();

	/**
	 * Set to true as soon as some inter-step setting changed.
	 */
	protected boolean needCommit = false;

	/**
	 * Peer node.
	 */
	protected Node peer;

// Constructors

	/**
	 * New empty node parameter set.
	 * @param value Numerical value associated with the node.
	 * @param color Initial colour of the node.
	 * @param params AntCO² parameters.
	 */
	public NodeInfos( float value, Colony color, AntParams params, Node node )
	        throws IllegalArgumentException
	{
		super( value );

		peer = node;

		setColor( color );
		antCountsPerColor.setCount( params.colors );
		antCountsPerColorTmp.setCount( params.colors );
	}

// Accessors

	/**
	 * Colour of the node.
	 * @return The colour.
	 */
	public Colony getColor()
	{
		return color;
	}

	/**
	 * Second most important colour of the node.
	 * @return The second colour.
	 */
	public Colony getSecondColor()
	{
		return secondColor;
	}

	/**
	 * Importance of the second colour compared to the primary colour. This number is a float between
	 * 0 and 1 included representing the presence of the second colour. 0 means the second is not
	 * present, 1 means the second colour is as much present as the primary one.
	 * @return The colour ratio.
	 */
	public float getColorRatio()
	{
		return colorRatio;
	}

	/**
	 * Previous colour of the node after it has changed colour.
	 * @return The previous colour.
	 */
	public Colony getOldColor()
	{
		return oldColor;
	}

	/**
	 * Total ant count on this node during the last step.
	 * @return The ant count.
	 */
	public int getTotalAntCount()
	{
		return totalAntCount;
	}

	/**
	 * Number of ants of a given colour on this node during the last step.
	 * @param color The colour.
	 * @return The corresponding number of ants on this node.
	 */
	public int getAntCountForColor( Colony color )
	{
		int index = color.getIndex();

		if( index >= antCountsPerColor.getCount() )
			return 0;

		return antCountsPerColor.get( index );
	}

// Commands

	/**
	 * Change the colour of the node. Can only be called at the end of an AntCO² step.
	 * @param newColor The colour to set.
	 */
	public void setColor( Colony newColor )
	{
		if( needCommit )
			throw new IllegalStateException( "cannot add a color if the NodeInfo is not commited" );

		if( newColor != color )
		{

			Colony oldColor = color;
			color = newColor;

			if( oldColor != null )
				oldColor.unregisterNode( this );

			if( newColor != null )
				newColor.registerNode( this );
		}
	}

	/**
	 * Check that the antCountPerColor arrays are large enough.
	 * @param index
	 */
	protected void checkColorArraySizes( int index )
	{
		int n = antCountsPerColor.getCount();

		if( index >= n )
		{
			antCountsPerColorTmp.setCount( index + 1 );
			antCountsPerColor.setCount( index + 1 );
			// System.err.printf( "NodeColorArray %d -> %d%n", n, index+1 );
		}
	}

	/**
	 * An ant arrived on this node. This only update temporary informations. Use {@link #commit()}
	 * to update. Commit should be called only at the end of each AntCO² step.
	 * @param ant The ant to register.
	 */
	public void registerAnt( Ant ant )
	{
		needCommit = true;
		totalAntCountTmp += 1;

		int index = ant.getColor().getIndex();
		checkColorArraySizes( index );
		antCountsPerColorTmp.incr( index, 1 );
	}

	/**
	 * An ant left this node. This only update temporary informations. Use {@link #commit()} to
	 * update. Commit should be called only at the end of each AntCO² step.
	 * @param ant The ant to de-register.
	 */
	public void unregisterAnt( Ant ant )
	{
		needCommit = true;
		totalAntCountTmp -= 1;

		int index = ant.getColor().getIndex();
		checkColorArraySizes( index );
		antCountsPerColorTmp.incr( index, -1 );
	}

	/**
	 * Commit all temporary changes to this object. The commit operation will switch:
	 * <ul>
	 * <li>the ant counts for each colour;</li>
	 * <li>the total ant count on this node;</li>
	 * </ul>
	 * Commit should only be called at the end of each AntCO² step.
	 */
	public void commit()
	{
		if( needCommit )
		{
			totalAntCount = totalAntCountTmp;
			// totalAntCountTmp = 0;
			int n = antCountsPerColor.getCount();

			for( int i = 0; i < n; ++i )
			{
				antCountsPerColor.set( i, antCountsPerColorTmp.get( i ) );
			}

			needCommit = false;
		}
	}

	/**
	 * Run the node tasks. The {@link #commit()} operation is automatically called by this method.
	 * @param antco2 AntCO².
	 */
	public void step( AntCo2 antco2 )
	{
		checkColorChange( antco2 );
		commit();
	}

	/**
	 * Check if the node changed colour by looking at each incident edge dominant pheromone.
	 */
	protected void checkColorChange( AntCo2 antco2 )
	{
		int colors = antco2.getAntParams().colors;
		float values[] = new float[colors];

		// First compute the global pheromone levels for all incident edges.

		Iterator<? extends Edge> edges = peer.getEdgeIterator();

		while( edges.hasNext() )
		{
			Edge edge = edges.next();
			EdgeInfos infos = (EdgeInfos) edge.getAttribute( EdgeInfos.ATTRIBUTE_NAME );
			assert infos != null : "each edge must have an EdgeInfos attribute";

			for( int c = 0; c < colors; ++c )
			{
				values[c] += infos.getPheromon( c );
			}
		}

		// Then find the max level (primary and secondary colours).

		float valueMax = 0;
		int maxIndex = -1;
		float secondValueMax = 0;
		int secondMaxIndex = -1;

		for( int c = 0; c < colors; ++c )
		{
			if( values[c] > valueMax )
			{
				maxIndex = c;
				valueMax = values[c];
			}
		}

		for( int c = 0; c < colors; ++c )
		{
			if( c != maxIndex )
			{
				if( values[c] > secondValueMax )
				{
					secondMaxIndex = c;
					secondValueMax = values[c];
				}

			}
		}

		if( maxIndex >= 0 )
		{
			Colony newColor = antco2.getColor( maxIndex );

			if( newColor != color )
			{
				oldColor = color;
				color = newColor;

				oldColor.unregisterNode( this );
				newColor.registerNode( this );
				antco2.ctx.incrMigrations();
			}
			else
			{
				oldColor = color;
			}
		}

		if( secondMaxIndex >= 0 )
		{
			Colony newColor = antco2.getColor( secondMaxIndex );

			if( newColor != secondColor )
				secondColor = newColor;

			colorRatio = ( secondValueMax / valueMax );
		}

		if( maxIndex >= 0 || secondMaxIndex >= 0 )
			antco2.listener.nodeInfos( peer.getId(), this );
	}
}