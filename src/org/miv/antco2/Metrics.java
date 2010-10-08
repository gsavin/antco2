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

import java.io.*;
import java.util.*;

import org.miv.graphstream.graph.*;
import book.set.IntArray;

/**
 * Various statistics and results.
 * 
 * @author Antoine Dutot
 * @since 29 juil. 2005
 */
public class Metrics implements Cloneable
{
// Attributes

	/**
	 * Number of ants.
	 */
	protected int antCount;

	/**
	 * Number of nodes.
	 */
	protected int nodeCount;

	/**
	 * Number of edges.
	 */
	protected int edgeCount;

	/**
	 * Number of edges between nodes of distinct colours.
	 */
	protected int cutEdges;

	/**
	 * Weight of all cut edges.
	 */
	protected float cutWeight;

	/**
	 * Weight of all edges.
	 */
	protected float weight;

	/**
	 * Number of nodes in the smallest partition.
	 */
	protected int minPart;

	/**
	 * Number of nodes in the largest partition.
	 */
	protected int maxPart;

	/**
	 * Number of jumps during the last step.
	 */
	protected int jumpCount;

	/**
	 * Number of ants reporting over population.
	 */
	protected int surpopCount;

	/**
	 * Number of nodes in each colour.
	 */
	protected IntArray partitions = new IntArray();

	/**
	 * Number of ants in each colony.
	 */
	protected IntArray population = new IntArray();

	/**
	 * Number of jumps per colony.
	 */
	protected IntArray jumpsPerColony = new IntArray();

	/**
	 * How many entities changed colour during the last step.
	 */
	protected int migrationCount;

	/**
	 * Stability of colours on nodes. The more a node stay at the same colour the more it is stable.
	 */
	protected float colorStability;

	/**
	 * Ratio cut weight / weight.
	 */
	protected float r1;

	/**
	 * Ratio minPart / maxPart.
	 */
	protected float r2;

	/**
	 * Ratio jumps / antCount.
	 */
	protected float r3;

	/**
	 * Ratio over pop / antCount.
	 */
	protected float r4;

	/**
	 * Ratio migrationCount / nodeCount.
	 */
	protected float r5;

	/**
	 * Output stream if outputting metrics.
	 */
	protected PrintStream out = null;

	// Constructors

	/**
	 * New metrics.
	 */
	public Metrics()
	{
	}

// Access

	/**
	 * Clone of these metrics.
	 */
	public Metrics clone()
	{
		Metrics m = new Metrics();

		m.antCount = antCount;
		m.cutEdges = cutEdges;
		m.cutWeight = cutWeight;
		m.edgeCount = edgeCount;
		m.jumpCount = jumpCount;
		m.surpopCount = surpopCount;
		m.maxPart = maxPart;
		m.minPart = minPart;
		m.nodeCount = nodeCount;
		m.partitions = new IntArray( partitions );
		m.population = new IntArray( population );
		m.jumpsPerColony = new IntArray( jumpsPerColony );
		m.migrationCount = migrationCount;
		m.colorStability = colorStability;
		m.r1 = r1;
		m.r2 = r2;
		m.r3 = r3;
		m.r4 = r4;
		m.r5 = r5;

		return m;
	}

	/**
	 * Number of ants.
	 */
	public int getAntCount()
	{
		return antCount;
	}

	/**
	 * Number of edges between nodes of distinct colours.
	 * @return Cut edge count.
	 */
	public int getCutEdges()
	{
		return cutEdges;
	}

	/**
	 * Weight of all the edges between nodes of distinct colours.
	 * @return Cut weight.
	 */
	public float getCutWeight()
	{
		return cutWeight;
	}

	/**
	 * Total weight of all the edges.
	 * @return Returns the total edge weight.
	 */
	public float getWeight()
	{
		return weight;
	}

	/**
	 * Number of edges.
	 * @return Number of edges.
	 */
	public int getEdges()
	{
		return edgeCount;
	}

	/**
	 * Number of nodes.
	 * @return Number of nodes.
	 */
	public int getNodes()
	{
		return nodeCount;
	}

	/**
	 * Number of nodes in the largest partition.
	 * @return Largest partition cardinal.
	 */
	public int getMaxPartition()
	{
		return maxPart;
	}

	/**
	 * Number of nodes in the smallest partition.
	 * @return Smallest partition cardinal.
	 */
	public int getMinPartition()
	{
		return minPart;
	}

	/**
	 * Number of jumps during the last step.
	 * @return Jump count.
	 */
	public int getJumps()
	{
		return jumpCount;
	}

	/**
	 * Array of integers containing the number of ants in each colour. Colour indices match this array
	 * indices.
	 * @return The population count array.
	 */
	public final IntArray getPopulations()
	{
		return population;
	}

	/**
	 * Number of jumps per colony. Each cell maps to the index of the corresponding colony.
	 * @return The number of jumps for each colony.
	 */
	public final IntArray getJumpsPerColony()
	{
		return jumpsPerColony;
	}

	/**
	 * Number of nodes in each colony. Each cell maps to the index of the corresponding colony.
	 * @return The number of nodes colonised by each colony.
	 */
	public final IntArray getPartitions()
	{
		return partitions;
	}

	/**
	 * Number of ants reporting over population. If an ant, during the last step encountered a
	 * over populated node, it counts it. If the ant encounters other over populated nodes during
	 * the same step it does not count them.
	 * @return The over population count.
	 */
	public int getSurpopulationCount()
	{
		return surpopCount;
	}

	/**
	 * Number of entities/nodes that changed colour during the last step.
	 * @return The migration count.
	 */
	public int getMigrationCount()
	{
		return migrationCount;
	}

	/**
	 * Colour stability of all nodes. The more the nodes stay at the same colour the more they are
	 * stable.
	 * @return The stability ratio (0_1).
	 */
	public float getColorStability()
	{
		return colorStability;
	}

	/**
	 * Ratio cut weight over edges weight. The smaller the better.
	 * @return r1.
	 */
	public float getR1()
	{
		return r1;
	}

	/**
	 * Ratio minimal partition over maximal partition. The higher the better.
	 * @return r2.
	 */
	public float getR2()
	{
		return r2;
	}

	/**
	 * Ratio jump count over ant population.
	 * @return r3.
	 */
	public float getR3()
	{
		return r3;
	}

	/**
	 * Ratio over population over ant population.
	 * @return r4.
	 */
	public float getR4()
	{
		return r4;
	}

	/**
	 * Ratio migration count over node count.
	 * @return r5
	 */
	public float getR5()
	{
		return r5;
	}

	// Commands

	/**
	 * Update metrics by analysing the graph.
	 * @param params AntCO² parameters.
	 * @param graph The graph to analyse.
	 * @param jumps Number of jumps during the last step.
	 * @param surpop Number of ants reporting over population.
	 * @param migrations Number of nodes/entities that changed colour during the last step.
	 */
	public void step( AntParams params, Collection<Colony> colonies, Graph graph, int jumps,
	        IntArray jumpsPerColony, int surpop, int migrations )
	{
		this.antCount = 0;
		this.jumpCount = jumps;
		this.jumpsPerColony = jumpsPerColony;
		this.surpopCount = surpop;
		this.nodeCount = 0;
		this.edgeCount = 0;
		this.cutEdges = 0;
		this.weight = 0;
		this.cutWeight = 0;
		this.minPart = Integer.MAX_VALUE;
		this.maxPart = Integer.MIN_VALUE;
		this.migrationCount = migrations;

		// Get nodes informations.

		Iterator<? extends Node> nodes = graph.getNodeIterator();

		colorStability = 0;

		while( nodes.hasNext() )
		{
			Node node = nodes.next();
			NodeInfos info = (NodeInfos) node.getAttribute( NodeInfos.ATTRIBUTE_NAME );
			assert info != null : "all nodes should have a NodeInfos attribute";

			colorStability += info.colorRatio;
		}

		this.nodeCount = graph.getNodeCount();
		colorStability /= this.nodeCount;

		// Get edges informations.

		Iterator<? extends Edge> edges = graph.getEdgeIterator();

		while( edges.hasNext() )
		{
			Edge edge = edges.next();
			EdgeInfos info = (EdgeInfos) edge.getAttribute( EdgeInfos.ATTRIBUTE_NAME );
			assert info != null : "all edges should have a EdgeInfos attribute";
			NodeInfos info0 = (NodeInfos) edge.getSourceNode().getAttribute(
			        NodeInfos.ATTRIBUTE_NAME );
			assert info0 != null : "all nodes should have a NodeInfos attribute";
			NodeInfos info1 = (NodeInfos) edge.getTargetNode().getAttribute(
			        NodeInfos.ATTRIBUTE_NAME );
			assert info1 != null : "all nodes should have a NodeInfos attribute";

			if( info0.getColor() != info1.getColor() )
			{
				cutEdges++;
				cutWeight += info.getValue();
			}

			weight += info.getValue();
			edgeCount++;
		}

		// Get colony informations.

		for( Colony color : colonies )
		{
			int index = color.getIndex();
			int nodeCount = color.getNodeCount();

			if( index >= partitions.size() )
				partitions.setCount( index + 1 );

			if( index >= population.size() )
				population.setCount( index + 1 );

			partitions.set( index, nodeCount );
			population.set( index, color.getAntCount() );

			if( params.powers != null && params.powers.size() > index )
				nodeCount /= params.powers.get( index );

			if( nodeCount > maxPart )
				maxPart = nodeCount;

			if( nodeCount < minPart )
				minPart = nodeCount;

			antCount += color.getAntCount();
		}

		// Compute some useful values.

		r1 = weight != 0 ? ( cutWeight / weight ) : 0;
		r2 = maxPart != 0 ? ( (float) minPart / (float) maxPart ) : 0;
		r3 = antCount != 0 ? ( (float) jumpCount / (float) antCount ) : 0;
		r4 = antCount != 0 ? ( (float) surpopCount / (float) antCount ) : 0;
		r5 = nodeCount != 0 ? ( (float) migrationCount / (float) antCount ) : 0;

		outputMetrics( params );
	}

	/**
	 * Output the metrics if requested. The output stream is created dynamically if needed and then
	 * reused.
	 */
	protected void outputMetrics( AntParams params )
	{
		try
		{
			if( params.outputMetrics != null )
			{
				if( out == null )
				{
					out = new PrintStream( params.outputMetrics );
					out.printf( "# 17 fields%n" );
					out
					        .printf( "# R1(com) R2(load) R3(jmp) R4(ovpop) R5(mig) AntCount JumpCount SurpopCount MigrationCount, NodeCount EdgeCount CutEdges Weight CutWeight MinPart MaxPart colorStability%n" );
				}

				out.printf( Locale.US, "%f %f %f %f %f %d %d %d %d %d %d %d %f %f %d %d %f%n", r1,
				        r2, r3, r4, r5, antCount, jumpCount, surpopCount, migrationCount,
				        nodeCount, edgeCount, cutEdges, weight, cutWeight, minPart, maxPart,
				        colorStability );

				out.flush();
			}
		}
		catch( FileNotFoundException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * Close open files if needed.
	 */
	public void close()
	{
		if( out != null )
		{
			out.flush();
			out.close();
		}
	}
}