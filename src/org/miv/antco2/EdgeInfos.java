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

import book.set.*;

import org.miv.graphstream.graph.*;

/**
 * Edge parameters.
 * 
 * <p>
 * Edges parameters contains the edge pheromone value. Users apply changes in two phases. First the
 * user calls {@link #setPheromon(Colony, float)} or {@link #incrPheromon(Colony, float)} without in
 * fact modifying the pheromone array as reported by {@link #getPheromon(int)}. Then the user calls
 * {@link #commit()} and apply changes to the pheromone array.
 * </p>
 * 
 * @author Antoine Dutot
 * @since 21 juil. 2005
 */
public class EdgeInfos extends GraphInfos
{
	// Constants

	/**
	 * Used to retrieve this attribute in a graph.
	 */
	public static final String ATTRIBUTE_NAME = "NodeInfos";

	// Attributes

	/**
	 * Peer edge.
	 */
	protected Edge peer;

	/**
	 * Pheromone array.
	 */
	protected FloatArray pheromones = new FloatArray();

	/**
	 * Temporary pheromone array.
	 */
	protected FloatArray pheromonesTmp = new FloatArray();

	/**
	 * Total of all pheromones for all colours after the last commit().
	 */
	protected float pheromonesTotal;

	/**
	 * Set to true if some inter-step value changed.
	 */
	protected boolean commitNeeded = false;

	/**
	 * Index of the dominant colour.
	 */
	protected int dominantColor = -1;

	/**
	 * Is the edge between two distinct colours?.
	 */
	protected boolean cutEdge;

	// Constructors

	/**
	 * New edge parameters.
	 * @param value Edge value.
	 * @param ctx AntCO² parameters used to initialise this object.
	 */
	public EdgeInfos( Edge edge, float value, AntContext ctx ) throws IllegalArgumentException
	{
		super( value );

		if( ctx.params.colors <= 0 )
			throw new IllegalArgumentException( "Invalid number of colors " + ctx.params.colors
			        + ", cannot initialize edge parameters" );

		this.peer = edge;

		pheromonesTmp.setCount( ctx.params.colors );
		pheromones.setCount( ctx.params.colors );

		// Initialise the pheromones to a very small value to avoid 0.

		pheromonesTotal = 0;

		for( int i = 0; i < ctx.params.colors; ++i )
		{
			float nb = ctx.random.nextFloat() * 0.0001f;

			pheromonesTmp.set( i, nb );
			pheromones.set( i, nb );
			pheromonesTotal += pheromones.get( i );
		}

		// Also avoid a weight of 0.

		if( this.value == 0 )
		{
			// System.err.printf( "[reset edge weight, was 0, set to 1] " );
			this.value = 1;
		}
	}

// Access

	/**
	 * Pheromone value for a given colour.
	 * @param color Colour index.
	 * @return The pheromone value for a given colour.
	 */
	public float getPheromon( int color )
	{
		if( color >= 0 && pheromones.size() > color )
			return pheromones.get( color );

		return 0;
	}

	/**
	 * Phromone value for all colours.
	 * @return The total pheromone value.
	 */
	public float getPheromonTotal()
	{
		return pheromonesTotal;
	}

	/**
	 * Colour that have the more pheromone on this edge.
	 * @return The dominant colour index.
	 */
	public int getDominantColor()
	{
		return dominantColor;
	}

	/**
	 * Is the edge between two nodes of distinct colours?.
	 */
	public boolean isCutEdge()
	{
		return cutEdge;
	}

// Commands

	/**
	 * Set the pheromone value for a given colour. If the given colour is null all the colours are
	 * changed. This directly changes the pheromone, no need to commit.
	 * @param color Colour index or null for all the colours.
	 * @param value Value to set.
	 */
	public void setPheromon( Colony color, float value )
	{
		if( color != null )
		{
			int index = color.getIndex();

			checkPheromonesArraySizes( index );
			pheromones.set( index, value );
		}
		else
		{
			int n = pheromones.size();

			for( int i = 0; i < n; ++i )
				pheromones.set( i, value );

			pheromonesTotal = value * pheromones.size();
		}
	}

	/**
	 * Increment the pheromone value for a given colour. If the given colour is null all the colours
	 * are changed. This change is stored in a temporary buffer, you need to call commit() to make
	 * it real.
	 * @param color Colour index or null for all the colours.
	 * @param value Value to add.
	 */
	public void incrPheromon( Colony color, float value )
	{
		commitNeeded = true;

		if( color != null )
		{
			int index = color.getIndex();

			checkPheromonesArraySizes( index );
			pheromonesTmp.incr( index, value );
		}
		else
		{
			int n = pheromonesTmp.size();

			for( int i = 0; i < n; ++i )
				pheromonesTmp.incr( i, value );
		}
	}

	/**
	 * Check that the antCountPerColor arrays are large enough.
	 * @param index
	 */
	protected void checkPheromonesArraySizes( int index )
	{
		int n = pheromones.getCount();

		if( index >= n )
		{
			pheromonesTmp.setCount( index + 1 );
			pheromones.setCount( index + 1 );
		}
	}

	/**
	 * switch: Commit all temporary changes to this object. The commit operation will
	 * <ul>
	 * <li>the pheromones;</li>
	 * </ul>
	 * Commit should only be called at the end of each AntCO² step.
	 */
	public void commit()
	{
		if( commitNeeded )
		{
			int n = pheromones.size();

			for( int i = 0; i < n; ++i )
			{
				float incr = pheromonesTmp.get( i );

				pheromones.incr( i, incr );
				pheromonesTotal += incr;
				pheromonesTmp.set( i, 0 );
			}

			commitNeeded = false;
		}
	}

	/**
	 * Run edge tasks. The {@link #commit()} operation is automatically called by this method. This
	 * method implements the evaporation.
	 * @param antco2 AntCO² instance.
	 */
	public void step( AntCo2 antco2 )
	{
		int n = pheromones.size();

		if( n > 0 )
		{
			// Evaporate the pheromones already present on the edge.

			AntParams params = antco2.getAntParams();

			pheromonesTotal = 0;

			for( int i = 0; i < n; ++i )
			{
				pheromones.mult( i, params.rho );
			}

			// Then, and only then, copy pheromones added by ants at the previous
			// step to the pheromones on the edge.

			commit();

			// At last, compute the total pheromone value on the edge, and
			// find the dominant colour.

			float max = Float.NEGATIVE_INFINITY;
			int maxI = -1;

			for( int i = 0; i < n; ++i )
			{
				float ph = pheromones.get( i );

				pheromonesTotal += ph;

				if( ph > max )
				{
					max = ph;
					maxI = i;
				}
			}

			dominantColor = maxI;
		}

		NodeInfos src = (NodeInfos) peer.getSourceNode().getAttribute( NodeInfos.ATTRIBUTE_NAME,
		        NodeInfos.class );
		NodeInfos trg = (NodeInfos) peer.getTargetNode().getAttribute( NodeInfos.ATTRIBUTE_NAME,
		        NodeInfos.class );

		cutEdge = src.getColor() != trg.getColor();

		// System.err.printf( "PH TOTAL %f%n", pheromonesTotal );
		antco2.listener.edgeInfos( peer.getId(), this );
	}

	public void debug( int index )
	{
		int n = pheromones.size();

		if( index < 0 )
			throw new RuntimeException( "invalid color index < 0" );

		if( index >= n )
			throw new RuntimeException( "invalid color index >= n" );

		System.err.printf( " %s => [", peer.getId() );
		for( int i = 0; i < n; ++i )
			System.err.printf( " %f", pheromones.get( i ) );
		System.err.printf( " ] = %f%n", pheromonesTotal );
	}
}