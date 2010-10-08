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


import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;

import java.util.*;

import java.nio.IntBuffer;

/**
 * Shared information.
 * 
 * @author Antoine Dutot
 * @since 20050419
 */
public class AntContext
{
// Attributes

	/**
	 * AntCO² parameters.
	 */
	public AntParams params;

	/**
	 * Current ant time step.
	 */
	public int time = 0;

	/**
	 * Shared graph.
	 */
	public Graph graph;

	/**
	 * Random number generator.
	 */
	public Random random;

	/**
	 * Statistics and results.
	 */
	public Metrics metrics;

	/**
	 * Number of jumps of the last step.
	 */
	public int jumps;

	/**
	 * Number of over populated nodes encountered.
	 */
	public int surpop;

	/**
	 * Number of nodes/edges migrations.
	 */
	public int migrations;

	/**
	 * Number of jumps of the last step per colony.
	 */
	public IntBuffer jumpsPerColony = new IntArray();

	// Constructors

	/**
	 * New ant context at time 0 with an empty graph.
	 */
	public AntContext( AntParams params )
	{
		if( sharedGraph == null )
			sharedGraph = new DefaultGraph( "Ant Environment" );
		
		this.params  = params;
		this.graph   = sharedGraph;
		this.metrics = new Metrics();
		
		if( params.randomSeed >= 0 )
		     random = new Random( params.randomSeed );
		else random = new Random();
	}

// Access

	/**
	 * Shared graph.
	 * @return The graph containing pheromone and colour informations.
	 */
	public Graph getGraph()
	{
		return graph;
	}

	/**
	 * Time.
	 * @return The current time.
	 */
	public int getTime()
	{
		return time;
	}

	/**
	 * Random number generator used everywhere.
	 * @return The random number generator initialised with the correct seed as given by AntCO²
	 *         parameters.
	 */
	public Random getRandom()
	{
		return random;
	}

	/**
	 * Statistics and results.
	 * @return The metrics.
	 */
	public Metrics getMetrics()
	{
		return metrics;
	}

	/**
	 * AntCO² parameters.
	 * @return the parameters.
	 */
	public AntParams getParams()
	{
		return params;
	}

	/**
	 * Return a random node chosen in the graph. This method tries to cache a direct access map of
	 * the graph to avoid looking for a random node by iterating on the graph (the only possible way
	 * if no direct access map is present).
	 * @return A random node.
	 */
	public Node getRandomNode()
	{
		return graph.algorithm().getRandomNode( random );
	}

// Commands

	public void incrJumps( Ant ant )
	{
		jumps++;

		Colony color = ant.getColor();

		while( color.getIndex() >= jumpsPerColony.size() )
			jumpsPerColony.add( 0 );

		jumpsPerColony.incr( color.getIndex(), 1 );
	}

	public void incrSurpop()
	{
		surpop++;
	}

	public void incrMigrations()
	{
		migrations++;
	}

	/**
	 * Switch to the next time step and update metrics.
	 */
	public void step( Collection<Colony> colonies, boolean graphChanged )
	{
		time += 1;

		metrics.step( params, colonies, graph, jumps, new IntArray( jumpsPerColony ), surpop,
		        migrations );

		jumps = 0;
		surpop = 0;
		migrations = 0;

		for( int i = 0; i < jumpsPerColony.size(); ++i )
			jumpsPerColony.set( i, 0 );

		if( graphChanged )
		{
			// if( directNodeAccess != null )
			// directNodeAccess.clear();
		}
	}
}