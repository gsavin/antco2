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

import java.awt.Color;
import java.util.HashMap;

/**
 * Model of an ants colony.
 * 
 * @author adutot, gsavin
 * 
 */
public abstract class Colony {
	// Attributes

	/**
	 * Context.
	 */
	protected AntContext ctx;

	/**
	 * Colony name.
	 */
	protected String name;

	/**
	 * Colony colour.
	 */
	protected Color color;

	/**
	 * Index of this colony in the AntCo2 instance.
	 */
	protected int index;

	/**
	 * Ants of this colony, by their id.
	 */
	protected HashMap<String, Ant> ants = new HashMap<String, Ant>();

	/**
	 * Same as {@link #ants} but used by commit.
	 */
	protected HashMap<String, Ant> antsAdd = new HashMap<String, Ant>();

	/**
	 * Same as {@link #ants} but used by commit.
	 */
	protected HashMap<String, Ant> antsDel = new HashMap<String, Ant>();

	/**
	 * Is a commit operation needed.
	 */
	protected boolean needCommit = false;

	/**
	 * Number of nodes having this colour.
	 */
	protected int nodeCount;

	/**
	 * Automatic allocator for ants id.
	 */
	protected int newAntId;

	/**
	 * Factory used by the colony to create ants.
	 */
	protected AntFactory antFactory;

	protected int migrationForThisStep;

	protected double migrationCostForThisStep;

	protected int nodeCountAtStepBeginning;

	// Constructors

	/**
	 * Empty constructor for descendants.
	 */
	protected Colony() {
	}

	/**
	 * New colony.
	 * 
	 * @param context
	 *            Ant context.
	 * @param name
	 *            Colony name (unique on all hosts).
	 * @param index
	 *            Unique index on this host of this colony.
	 * @param red
	 *            Colour red component.
	 * @param green
	 *            Colour green component.
	 * @param blue
	 *            Colour blue component.
	 */
	public Colony(AntContext context, String name, int index) {
		init(context, name, index);
	}

	/**
	 * Initialize a colony. This method acts as the colony constructor, since
	 * colonies have to be instantiated dynamically, and therefore need a
	 * default constructor.
	 * 
	 * @param context
	 *            Ant context.
	 * @param name
	 *            Colony name (unique on all hosts).
	 * @param index
	 *            Unique index on this host of this colony.
	 * @param red
	 *            Colour red component.
	 * @param green
	 *            Colour green component.
	 * @param blue
	 *            Colour blue component.
	 */
	public void init(AntContext context, String name, int index) {
		assert ctx == null : "cannot call init() on an already initialized colony";

		if (ctx == null) {
			this.ctx = context;
			this.name = name;
			this.index = index;
			// this.color = color;
		}
	}

	// Access

	/**
	 * Colony name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Colony colour.
	 * 
	 * @return The colour.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Index of this colony in the AntCo2 instance.
	 * 
	 * @return The index.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Number of ants in this colony.
	 * 
	 * @return Ant count.
	 */
	public int getAntCount() {
		return ants.size();
	}

	/**
	 * Number of nodes having this colour.
	 * 
	 * @return Node count.
	 */
	public int getNodeCount() {
		return nodeCount;
	}

	public int getMigrationCountForThisStep() {
		return migrationForThisStep;
	}

	public double getMigrationCostForThisStep() {
		return migrationCostForThisStep;
	}

	public int getNodeCountAtStepBeginning() {
		return nodeCountAtStepBeginning;
	}

	/**
	 * Create a new colony instance based in the species given in the AntCO²
	 * parameters.
	 * 
	 * @param context
	 *            Ant context (define the class to instantiate).
	 * @param name
	 *            Colony name (global to all hosts).
	 * @param index
	 *            Colony index (local to this host).
	 * @param red
	 *            Colony color red component.
	 * @param green
	 *            Colony color green component.
	 * @param blue
	 *            Colony color blue component.
	 * @return The newly created colony.
	 * @throws ClassNotFoundException
	 *             If the species does not identify a class in the classpath.
	 * @throws InstantiationException
	 *             If the class specified is not instantiable.
	 * @throws IllegalAccessException
	 *             If the class specified is not accessible.
	 * @throws ExceptionInInitializerError
	 *             If the class constructor fails.
	 * @throws SecurityException
	 *             You know why.
	 */
	public static Colony newColony(AntContext context, String name, int index) {
		try {
			Colony colony = ColonyFactory.newColony(context);
			colony.init(context, name, index);

			return colony;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	// Commands

	/**
	 * The given node is now of this colour.
	 * 
	 * @param node
	 *            A node info associated to a node of the graph.
	 */
	protected void registerNode(AntCo2Node node) {
		nodeCount++;
		assert node.getColor() == this;
	}

	/**
	 * The given node is no more of this colour.
	 * 
	 * @param node
	 *            A node info associated to a node of the graph.
	 */
	protected void unregisterNode(AntCo2Node node) {
		nodeCount--;
		migrationForThisStep++;
		migrationCostForThisStep += node.getValue();
		assert node.getColor() != this;
	}

	/**
	 * Add a new ant to the colony. This method only register an "add action"
	 * but will effectively add the ant only when {@link #commit()} is called.
	 * 
	 * @param id
	 *            Ant identifier (null means creating automatically the
	 *            identifier).
	 * @param start
	 *            Ant start node.
	 * @throws SingletonException
	 *             If an ant with the same identifier already exists.
	 */
	public void addAnt(String id, AntCo2Node start) {
		if (antsDel.get(id) != null)
			antsDel.remove(id);

		if (antsAdd.get(id) == null) {
			Ant ant = antFactory().newAnt(id, start);
			antsAdd.put(id, ant);
		}

		needCommit = true;
	}

	/**
	 * Remove arbitrarily n ants of this colour.
	 * 
	 * @param n
	 *            The number of ants to remove.
	 */
	public void removeAnts(int n) {
		for (Ant ant : ants.values()) {
			Ant old = antsDel.put(ant.getId(), ant);

			if (old == null)
				n--;

			if (n == 0)
				break;
		}

		needCommit = true;
	}

	/**
	 * The ant factory used for this colony.
	 * 
	 * @return the ant factory
	 * @see org.graphstream.algorithm.antco2.AntFactory
	 */
	public AntFactory antFactory() {
		return antFactory;
	}

	/**
	 * Commit ants add or removal. Ants are not added or removed while a step of
	 * AntCO² is running, however, the {@link #addAnt(String, Node)} can be
	 * called at any time. Therefore ant add or removal are registered in a
	 * special buffer, then really added or removed when this method is called.
	 */
	public void commit() {
		if (needCommit) {
			for (Ant ant : antsDel.values()) {
				Ant old = ants.remove(ant.getId());
				assert old != null : "an ant '" + ant.getId()
						+ "' that does not exits has been removed";
			}

			for (Ant ant : antsAdd.values()) {
				Ant old = ants.put(ant.getId(), ant);
				assert old == null : "identifier '" + ant.getId()
						+ "' is already registered";
			}

			antsAdd.clear();
			antsDel.clear();

			needCommit = false;
		}
	}

	/**
	 * Make all the ants of the colony run.
	 */
	public void step() {
		migrationForThisStep = 0;
		migrationCostForThisStep = 0;
		nodeCountAtStepBeginning = getNodeCount();

		for (Ant ant : ants.values()) {
			ant.step();
		}
	}

	/**
	 * Called when a colony is removed. This removes all ants.
	 */
	public void removed() {
		for (Ant ant : ants.values()) {
			ant.goTo(null);
		}
	}
}
