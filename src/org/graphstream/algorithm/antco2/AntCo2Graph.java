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

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.NodeFactory;
import org.graphstream.graph.EdgeFactory;
import org.graphstream.graph.implementations.AdjacencyListGraph;

/**
 * The internal graph of AntCo2.
 * 
 * @author adutot, gsavin
 *
 */
public class AntCo2Graph
	extends AdjacencyListGraph
{
	/**
	 * The context of this graph.
	 */
	protected AntContext ctx;
	
	/**
	 * Constructor for the graph.
	 * @param context context in which ants will evolve
	 */
	public AntCo2Graph( AntContext context )
	{
		super( String.format( "antco2-%X@%X", System.currentTimeMillis(), Thread.currentThread().getId() ) );
		
		this.ctx = context;
		
		this.nodeFactory = new NodeFactory<AntCo2Node>()
		{
			public AntCo2Node newInstance(String id, Graph graph)
			{
				Colony c = ctx.getColonyCount() > 0 ? 
						ctx.getColony(ctx.random().nextInt(ctx.getColonyCount())) : null;
				
				return new AntCo2Node(ctx,c,graph,id);
			}
		};

		this.edgeFactory = new EdgeFactory<AntCo2Edge>()
		{
			public AntCo2Edge newInstance(String id, Node src, Node dst,
					boolean directed)
			{
				return new AntCo2Edge(ctx,1,id,src,dst,directed);
			}
		};
	}
	
	/**
	 * Iterate over edges as AntCo2Edge.
	 * @return an iterable over edges of the graph
	 */
	public Iterable<? extends AntCo2Edge> antco2EdgeSet()
	{
		return getEachEdge();
	}
	
	/**
	 * Iterate over nodes as AntCo2Node.
	 * @return an iterable over nodes of the graph
	 */
	public Iterable<? extends AntCo2Node> antco2NodeSet()
	{
		return getEachNode();
	}
	
	public AntContext getAntContext()
	{
		return ctx;
	}
}
