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

/**
 * Grand-Daddy ant.
 * 
 * @author Antoine Dutot
 * @since 22 juil. 2005
 */
public abstract class Ant
{
// Attributes

	/**
	 * Ant identifier.
	 */
	protected String id;

	/**
	 * Node the ant is visiting actually.
	 */
	protected Node curNode;

	/**
	 * Ant colour.
	 */
	protected Colony color;

	/**
	 * Ant context.
	 */
	protected AntContext ctx;

	// Constructors

	/**
	 * New ant located on the given node. The ant automatically register in the environment.
	 * @param startNode Node the ant starts its life on.
	 * @param context AntCO² context.
	 * @param color Ant colour.
	 */
	public Ant( String id, Node startNode, AntContext context, Colony color )
	{
		this.id = id;
		this.ctx = context;
		this.color = color;

		goTo( startNode );
	}

// Access

	/**
	 * Ant identifier.
	 * @return The identifier.
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Node where the ant is located. The node can be null if the ant was removed from the
	 * environment and not reinserted yet. In this case the ant is inactive.
	 * @return The node the ant is visiting actually.
	 */
	public Node getCurrentNode()
	{
		return curNode;
	}

	/**
	 * AntCO² parameters.
	 * @return AntCO² parameters.
	 */
	public AntParams getParameters()
	{
		return ctx.params;
	}

	/**
	 * Ant colour.
	 * @return Colour of the ant.
	 */
	public Colony getColor()
	{
		return color;
	}

	/**
	 * Amount of pheromone drop by the ant when it crosses an edge.
	 * @return The pheromone drop value.
	 */
	public abstract float getPheromonDrop();

// Commands

	/**
	 * Ask the ant to travel from its current node to the given new node. The new node can be null,
	 * in which case the ant disappear from the environment and becomes inactive. The destination
	 * node needs not to be connected via an edge to the current node. This is also called a jump.
	 * @param newNode The destination node.
	 */
	public void goTo( Node newNode )
	{
		NodeInfos curInfos = null;
		NodeInfos newInfos = null;

		if( curNode != null )
		{
			curInfos = (NodeInfos) curNode.getAttribute( NodeInfos.ATTRIBUTE_NAME, NodeInfos.class );
			assert curInfos != null : "A node must have a NodeInfos";
			curInfos.unregisterAnt( this );
		}

		if( newNode != null )
		{
			newInfos = (NodeInfos) newNode.getAttribute( NodeInfos.ATTRIBUTE_NAME, NodeInfos.class );
			assert newInfos != null : "A node must have a NodeInfos";
			newInfos.registerAnt( this );
		}

		curNode = newNode;
	}

	/**
	 * Go to the opposite node of the current node via the given edge.
	 * @param edge The edge to cross.
	 * @param dropPheromon If true an amount of pheromone defined by {@link #getPheromonDrop()} is
	 *        deposited on the edge.
	 * @throws IllegalArgumentException If the edge is not connected to the current node (or if the
	 *         current node is null).
	 */
	public void cross( Edge edge, boolean dropPheromon ) throws IllegalArgumentException
	{
		assert edge != null : "cannot cross null edge";

		if( curNode == null )
			throw new IllegalArgumentException(
			        "cannot cross an edge if the current node is null, use goTo()" );

		if( curNode != edge.getSourceNode() && curNode != edge.getTargetNode() )
			throw new IllegalArgumentException(
			        "the current node is not connected to the given edge" );

		Node newNode = edge.getOpposite( curNode );
		EdgeInfos infos = (EdgeInfos) edge.getAttribute( EdgeInfos.ATTRIBUTE_NAME );
		assert infos != null : "each edge must have an EdgeInfos attribute";

		infos.incrPheromon( color, getPheromonDrop() );
		goTo( newNode );
	}

	/**
	 * Make the ant move.
	 */
	public abstract void step();
}