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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The base-class for defining ants.
 * 
 * @author adutot, gsavin
 * 
 */
public abstract class Ant {
	protected static long automaticId = 0;
	protected static AtomicInteger automaticIdAtomic = new AtomicInteger(0);

	/**
	 * The id of this ant.
	 */
	protected final String id;
	/**
	 * The colony of the ant.
	 */
	protected Colony colony;
	/**
	 * The position (node) of the ant.
	 */
	protected AntCo2Node curNode;
	/**
	 * The context in which this ant evolve.
	 */
	protected AntContext ctx;

	/**
	 * Constructor for an ant.
	 * 
	 * @param id
	 * @param colony
	 * @param start
	 * @param ctx
	 */
	public Ant(String id, Colony colony, AntCo2Node start, AntContext ctx) {
		if (id == null)
			// id = String.format( "ant-%X-%X", automaticId++,
			// System.currentTimeMillis() ).toLowerCase();
			id = String.format("ant-%X", automaticIdAtomic.getAndIncrement());

		this.id = id;
		this.colony = colony;
		this.ctx = ctx;

		goTo(start);
	}

	/**
	 * Accessor for the id attribute.
	 * 
	 * @return id of this ant
	 */
	public String getId() {
		return id;
	}

	/**
	 * Accessor for the colony attribute.
	 * 
	 * @return colony of this ant
	 */
	public Colony getColony() {
		return colony;
	}

	/**
	 * Accessor for the position attribute.
	 * 
	 * @return position of this ant
	 */
	public AntCo2Node getCurrentNode() {
		return curNode;
	}

	/**
	 * Ask the ant to travel from its current node to the given new node. The
	 * new node can be null, in which case the ant disappear from the
	 * environment and becomes inactive. The destination node needs not to be
	 * connected via an edge to the current node. This is also called a jump.
	 * 
	 * @param newNode
	 *            The destination node.
	 */
	public void goTo(AntCo2Node newNode) {
		if (curNode != null)
			curNode.unregisterAnt(this);

		if (newNode != null)
			newNode.registerAnt(this);

		curNode = newNode;
	}

	/**
	 * Go to the opposite node of the current node via the given edge.
	 * 
	 * @param edge
	 *            The edge to cross.
	 * @param dropPheromon
	 *            If true an amount of pheromone defined by
	 *            {@link #getPheromonDrop()} is deposited on the edge.
	 * @throws IllegalArgumentException
	 *             If the edge is not connected to the current node (or if the
	 *             current node is null).
	 */
	public void cross(AntCo2Edge edge, boolean dropPheromon)
			throws IllegalArgumentException {
		assert edge != null : "cannot cross null edge";

		if (curNode == null)
			throw new IllegalArgumentException(
					"cannot cross an edge if the current node is null, use goTo()");

		if (curNode != edge.getSourceNode() && curNode != edge.getTargetNode())
			throw new IllegalArgumentException(
					"the current node is not connected to the given edge");

		AntCo2Node newNode = (AntCo2Node) edge.getOpposite(curNode);

		if (dropPheromon)
			edge.incrPheromon(colony, getPheromonDrop());

		goTo(newNode);
	}

	/**
	 * Jump to a random node in the graph. This method is quite heavy and may,
	 * at worst, iterate on all nodes of the graph.
	 */
	protected void jumpRandomly() {
		AntCo2Node node = null;
		int n = ctx.getNodeCount();

		if (n <= 0)
			return;

		int r = ctx.random().nextInt(n);
		int i = 0;

		for (AntCo2Node tmpNode : ctx.eachNode()) {
			if (i == r) {
				node = tmpNode;
				break;
			}

			i++;
		}

		assert node != null : "jumpRandomly() got a null node to jump to!";
		// System.err.printf( "Jumping randomly from node %s to node %s.%n",
		// curNode.getTag(), node.getTag() );

		ctx.incrJumps(this);
		goTo(node);
	}

	/**
	 * Jump from node to node, choosing the edges to cross randomly. This
	 * method, contrary to {@link #jumpRandomly()} follow the graph topoly to
	 * jump far away. Given a current node, the ant chooses randomly one of its
	 * edges and jump to the opposite node. It does so a given number of times.
	 * 
	 * @param howFar
	 *            How many jumps to do.
	 * @throws IllegalArgumentException
	 *             If the jump is less than 1.
	 */
	protected void jumpFarAway(int howFar) throws IllegalArgumentException {
		int rand;
		AntCo2Node node = curNode;

		if (howFar < 1)
			throw new IllegalArgumentException("jumps must be larger than 1");

		if (curNode.getDegree() == 0)
			return;

		for (int i = 0; i < howFar; ++i) {
			rand = ctx.random().nextInt(node.getDegree());
			node = (AntCo2Node) node.getEdge(rand).getOpposite(node);
		}

		ctx.incrJumps(this);
		goTo(node);
	}

	/**
	 * Defines what this ant will do at each step.
	 */
	public abstract void step();

	/**
	 * Defines the amoung of pheromons drop by this ant at each edge-cross.
	 * 
	 * @return the pheromon drop
	 */
	public abstract float getPheromonDrop();
}
