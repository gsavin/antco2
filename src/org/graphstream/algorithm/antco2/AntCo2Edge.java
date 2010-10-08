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

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListEdge;

/**
 * Defines an edge crossable by ants.
 * Pheromons can be dropped on such edges.
 * 
 * @author adutot, gsavin
 *
 */
public class AntCo2Edge
	extends AdjacencyListEdge
{
	/**
	 * Pheromone array.
	 */
	protected float [] pheromones = new float [1];

	/**
	 * Temporary pheromone array.
	 */
	protected float [] pheromonesTmp = new float [1];

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
	
	/**
	 * Weight of the edge.
	 */
	protected float value;
	
	/**
	 * Basic constructor for an edge.
	 * 
	 * @param id id of the edge
	 * @param from source node when edge is directed, else first extremity
	 * @param to target node when edge is directed, else second extremity
	 * @param directed is the edge directed or not
	 */
	public AntCo2Edge( String id, Node from, Node to, boolean directed )
	{
		super(id,from,to,directed);
	}
	
	/**
	 * Constructor for an edge.
	 * 
	 * @param ctx ant context 
	 * @param value weight of the edge
	 * @param id id of the edge
	 * @param from source node when edge is directed, else first extremity
	 * @param to target node when edge is directed, else second extremity
	 * @param directed is the edge directed or not
	 */
	public AntCo2Edge( AntContext ctx, float value, String id, Node from, Node to, boolean directed )
	{
		this(id,from,to,directed);
	
		this.value = value;
		
		pheromonesTmp = new float [ctx.getColonyCount()];
		pheromones = new float [ctx.getColonyCount()];

		// Initialise the pheromones to a very small value to avoid 0.

		pheromonesTotal = 0;

		for( int i = 0; i < ctx.getColonyCount(); ++i )
		{
			float nb = ctx.random().nextFloat() * 0.0001f;

			pheromonesTmp [i] = nb;
			pheromones [i] = nb;
			pheromonesTotal += nb;
		}

		// Also avoid a weight of 0.

		if( this.value == 0 )
			this.value = 1;
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
			int n = pheromones.length;

			for( int i = 0; i < n; ++i )
			{
				float incr = pheromonesTmp [i];

				pheromones [i] += incr;
				pheromonesTotal += incr;
				pheromonesTmp [i] = 0;
			}

			commitNeeded = false;
		}
	}
	
	/**
	 * Step method for this edge.
	 * Pheromones evaporation is done here.
	 * 
	 * @param ctx ants context
	 */
	public void step( AntContext ctx )
	{
		int n = pheromones.length;

		if( n > 0 )
		{
			// Evaporate the pheromones already present on the edge.

			pheromonesTotal = 0;

			for( int i = 0; i < n; ++i )
				pheromones [i] *= ctx.getAntParams().rho;

			// Then, and only then, copy pheromones added by ants at the previous
			// step to the pheromones on the edge.

			commit();

			// At last, compute the total pheromone value on the edge, and
			// find the dominant colour.

			float max = Float.NEGATIVE_INFINITY;
			int maxI = -1;

			for( int i = 0; i < n; ++i )
			{
				if( ctx.getColony(i) != null )
				{
					float ph = pheromones [i];

					pheromonesTotal += ph;

					if( ph > max )
					{
						max = ph;
						maxI = i;
					}
				}
			}

			dominantColor = maxI;
		}

		AntCo2Node src = (AntCo2Node) getSourceNode();
		AntCo2Node trg = (AntCo2Node) getTargetNode();

		cutEdge = src.getColor() != trg.getColor();
	}

	/**
	 * Pheromone value for a given colour.
	 * @param color Colour index.
	 * @return The pheromone value for a given colour.
	 */
	public float getPheromon( int color )
	{
		if( color >= 0 && pheromones.length > color )
			return pheromones [color];

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
			
			pheromonesTotal -= pheromones [index];
			pheromones [index] = value;
			pheromonesTotal += pheromones [index];
		}
		else
		{
			Arrays.fill(pheromones,value);
			pheromonesTotal = value * pheromones.length;
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
			pheromonesTmp [index] += value;
		}
		else
		{
			for( int i = 0; i < pheromonesTmp.length; ++i )
				pheromonesTmp [i] += value;
		}
	}

	/**
	 * Check that the antCountPerColor arrays are large enough.
	 * @param index
	 */
	protected void checkPheromonesArraySizes( int index )
	{
		if( index >= pheromones.length )
		{
			int n = pheromones.length;
			
			pheromonesTmp = Arrays.copyOf( pheromonesTmp, index + 1 );
			pheromones    = Arrays.copyOf( pheromones, index + 1 );
			
			Arrays.fill(pheromones,n,index+1,0.000001f);
			pheromonesTotal += 0.000001f * (index+1-n);
		}
	}

	/**
	 * Get the weight of the edge.
	 * @return weight of the edge
	 */
	public float getValue()
	{
		return value;
	}

	/**
	 * Set the weight of the edge.
	 * @param value new weight of the edge
	 */
	public void setValue(float value)
	{
		this.value = value;
	}
}
