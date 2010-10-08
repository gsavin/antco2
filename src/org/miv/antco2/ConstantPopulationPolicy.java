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

import org.miv.graphstream.graph.*;
import java.util.*;

/**
 * Constant population policy.
 * 
 * <p>
 * This policy both maintain an ant population proportional to the number of nodes in the graph, and
 * also maintain, for a given number of nodes, a constant population level for any number of
 * colours.
 * </p>
 * 
 * @author Antoine Dutot
 * @since 29 juil. 2005
 */
public class ConstantPopulationPolicy extends PopulationPolicy
{
// Attributes

// Constructors

	/**
	 * New popol.
	 */
	public ConstantPopulationPolicy()
	{

	}

// Access

// Commands -- AntCO² events

	@Override
	public void nodeAdded( Node node, NodeInfos infos )
	{
		// Allocate / colours

		AntParams params = antco2.getAntParams();

		if( params.colors > params.antsPerVertex )
			antco2.listener.error( "cannot allocate enought ants: there are " + params.colors
			        + " colors but only " + params.antsPerVertex + " ants per vertex are allowed",
			        null, true );

		int nAntsPerClr = params.antsPerVertex / params.colors;
		int remains = params.antsPerVertex % params.colors;

		if( remains != 0 )
			antco2.listener.warning( "with " + params.antsPerVertex + " and " + params.colors
			        + " colors it remains " + remains + " unallocated ants" );

		// System.err.println( "ADD "+nAntsPerClr+" ("+(nAntsPerClr*params.colors)+" ants for all
		// colors)" );

		for( Colony color : antco2.getColonies() )
		{
			for( int i = 0; i < nAntsPerClr; ++i )
			{
				color.addAnt( null, node );
			}
		}
	}

	@Override
	public void nodeRemoved( Node node, NodeInfos infos )
	{
		AntParams params = antco2.getAntParams();
		int nAntsPerClr = params.antsPerVertex / params.colors;

		for( Colony color : antco2.getColonies() )
		{
			color.removeAnts( nAntsPerClr );
		}
	}

	@Override
	public void colorAdded( Colony color )
	{
		AntParams params = antco2.getAntParams();
		Graph graph = antco2.getGraph();
		int colors = params.colors;		 		// Actual colour count (counting the new colony).
		int nodeCount = graph.getNodeCount(); 	// Actual node count.
		int antCount = antco2.getMetrics().getAntCount(); // Actual ant count.

		if( nodeCount == 0 )
			return;

		int colonyCount = antCount / colors; 					// How many ants per colour now.
		int toRemovePerColor = colonyCount / ( colors - 1 );	// How many ants of each old colour.
																// to remove.

		// Remove ants in each already present colony.

		for( Colony c : antco2.getColonies() )
		{
			if( c != color )
			{
				c.removeAnts( toRemovePerColor );
			}
		}

		// Add ants in the new colony.

		int toAddPerNode = colonyCount / nodeCount;
		int added = 0;

		Iterator<? extends Node> nodes = graph.getNodeIterator();

		while( nodes.hasNext() )
		{
			Node node = nodes.next();

			for( int i = 0; i < toAddPerNode; ++i )
			{
				color.addAnt( null, node );
				added++;
			}
		}

		// Due to float -> integer conversion we can loose ants (if we must
		// for example allocate 1.2 ants per node (always less than nodeCount).

		int remains = colonyCount - added;

		if( remains > 0 )
		{
			nodes = graph.getNodeIterator();

			while( nodes.hasNext() && remains > 0 )
			{
				Node node = nodes.next();

				color.addAnt( null, node );
				added++;
				remains--;
			}
		}

		// Add a small value of pheromone of the new colour, else ants will never
		// get a chance to appear.

		// antco2.flytoxx( color, 1.0f );

		// Reseting agoraphobia to correct values if needed.

		if( params.agoraphobia > 1f / colors )
		{
			System.err.printf( "*** RESETING AGORAPHOBIA TO %f ***, old value %f was too high.%n",
			        ( 1f / colors ), params.agoraphobia );
			params.agoraphobia = 1f / colors;
		}

		System.err.printf( "Added a colony [%s/%d]:%n", color.getName(), color.getIndex() );
		System.err.printf( "    There was %d ants, %d colors, %d nodes.%n", antCount, colors,
		        nodeCount );
		System.err.printf( "    There is now %d ants per color.%n", colonyCount );
		System.err.printf( "    Removed %d ants in each of the %d previous colonies.%n",
		        toRemovePerColor, colors - 1 );
		System.err.printf( "    Added %d/%d (should be %d, %d ants per node) new ants.%n", added,
		        remains, colonyCount, toAddPerNode );
	}

	@Override
	public void colorRemoved( Colony color )
	{
		// TODO
		throw new RuntimeException( "color removing in PopulationPolicy: TODO!!!" );
	}

	@Override
	public void stepFinished( int time, Metrics metrics )
	{
		// NOP!!
	}
}