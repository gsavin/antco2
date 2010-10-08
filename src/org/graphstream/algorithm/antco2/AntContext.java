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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.graphstream.algorithm.antco2.policy.ProportionalPopulationPolicy;
import org.graphstream.algorithm.antco2.smoothingBox.CohesionBox;
import org.graphstream.algorithm.antco2.smoothingBox.IdentityBox;
import org.graphstream.algorithm.antco2.smoothingBox.RandomTimedBox;
import org.graphstream.algorithm.antco2.smoothingBox.TimedBox;
import org.graphstream.stream.ElementSink;

/**
 * Context of the AntCo2 algorithm.
 * The context is composed of params, an internal graph and colonies.
 * 
 * @author adutot, gsavin
 *
 */
public class AntContext
	implements ElementSink
{
	/**
	 * Iterator over colonies.
	 * This iterator is safe to colonies changes.
	 * 
	 * @author adutot, gsavin
	 *
	 */
	class ColonyIterator
		implements Iterator<Colony>
	{
		/**
		 * Current index.
		 */
		protected int index = 0;
		
		/**
		 * Has next colonie.
		 */
		public boolean hasNext()
		{
			for( int i = index; i < colonies.size(); i++ )
				if( colonies.get(i) != null ) return true;
			
			return false;
		}

		/**
		 * Get next colonie.
		 */
		public Colony next()
		{
			for( ; index < colonies.size(); index++ )
				if( colonies.get(index) != null ) return colonies.get(index++);
			
			return null;
		}

		/**
		 * Not implemented.
		 */
		public void remove()
		{
			throw new Error("not implemented");
		}
	}
	
	/**
	 * Iterable over colonies.
	 * 
	 * @author adutot, gsavin
	 *
	 */
	class ColonyIterable
		implements Iterable<Colony>
	{
		/**
		 * Return a new colony iterator.
		 */
		public Iterator<Colony> iterator()
		{
			return new ColonyIterator();
		}
	}
	
	/**
	 * Internal graph model.
	 * @see org.graphstream.algorithm.antco2.AntCo2Graph
	 */
	protected AntCo2Graph internalGraph;

	private ReentrantLock locked;
	
	protected LinkedList<AntCo2Listener> listeners;
	
	/**
	 * Algoritm parameters.
	 */
	protected AntParams params;
	
	/**
	 * Policy used to populate nodes with ants.
	 */
	protected PopulationPolicy populationPolicy;
	
	/**
	 * List of colonies.
	 * @see org.graphstream.algorithm.antco2.Colony
	 */
	protected ArrayList<Colony> colonies;
	
	/**
	 * Iterable over colonies, used to provide safe iterators.
	 */
	protected ColonyIterable coloniesAsIterable;
	
	/**
	 * Current time id.
	 * Use for the ElementSink implementation.
	 */
	protected long timeId;
	
	/**
	 * Random instance.
	 */
	protected Random random;

	/**
	 * Number of jumps of the last step.
	 */
	protected int jumps;

	/**
	 * Number of over populated nodes encountered.
	 */
	protected int surpop;

	/**
	 * Number of nodes/edges migrations.
	 */
	protected int migrations;
	
	/**
	 * Current step.
	 */
	protected int step;
	
	/**
	 * Jumps count per colony.
	 */
	protected int [] jumpsPerColony;
	
	/**
	 * Ants count.
	 */
	protected int antCount;
	
	protected SmoothingBox smoothingBox;

	protected Measures measures;
	
	protected String outputMeasures;
	
	/**
	 * Default constructor.
	 */
	public AntContext()
	{
		smoothingBox = new IdentityBox();
		outputMeasures = "measures-identity.dat";
		//smoothingBox = new TimedBox(2,TimeUnit.SECONDS);
		//outputMeasures = "measures-timed.dat";
		//smoothingBox = new RandomTimedBox(2000,TimeUnit.MILLISECONDS,0.2);
		//outputMeasures = "measures-random-timed.dat";
		//smoothingBox = new CohesionBox();
		//outputMeasures = "measures-cohesion.dat";
		
		colonies = new ArrayList<Colony>();
		coloniesAsIterable = new ColonyIterable();
		internalGraph = new AntCo2Graph(this);
		params = new AntParams();
		populationPolicy = new ProportionalPopulationPolicy();
		populationPolicy.init(this);
		random = new Random(params.randomSeed);
		measures = new Measures();
		locked = new ReentrantLock();
		listeners = new LinkedList<AntCo2Listener>();
		
		addColony( "localhost" );
		
		jumpsPerColony = new int [Math.max(1,colonies.size())];
		
		internalGraph.addElementSink(this);
	}
	
	public SmoothingBox getSmoothingBox()
	{
		return smoothingBox;
	}
	
	/**
	 * Access to parameters.
	 * @return parameters
	 */
	public AntParams getAntParams()
	{
		return params;
	}
	
	/**
	 * Get the policy used for the ants population.
	 * @return the ants population policy
	 */
	public PopulationPolicy getPopulationPolicy()
	{
		return populationPolicy;
	}
	
	/**
	 * Get the colonies count.
	 * @return colonies count
	 */
	public int getColonyCount()
	{
		return colonies.size();
	}
	
	/**
	 * Get the i-th colony.
	 * @param i index of the colony
	 * @return
	 */
	public Colony getColony( int i )
	{
		return colonies.get(i);
	}
	
	/**
	 * Get the current step.
	 * @return current step
	 */
	public int getCurrentStep()
	{
		return step;
	}
	
	/**
	 * Get the nodes count in the internal graph.
	 * @return nodes count
	 */
	public int getNodeCount()
	{
		return internalGraph.getNodeCount();
	}
	
	/**
	 * Get the ants count.
	 * @return ants count
	 */
	public int getAntCount()
	{
		return populationPolicy.getAntCount();
	}
	
	public String getOutputMeasures()
	{
		return outputMeasures;
	}
	
	public AntCo2Graph getInternalGraph()
	{
		return internalGraph;
	}
	
	void lock()
	{
		locked.lock();
	}
	
	void unlock()
	{
		locked.unlock();
	}
	
	/**
	 * Access to the random object used for random operations.
	 * @return random object
	 */
	public Random random()
	{
		return random;
	}
	
	/**
	 * Get the source id used for sink operations.
	 * @return source id
	 */
	public String sourceId()
	{
		return internalGraph.getId();
	}
	
	/**
	 * Get a new time id for sink operations.
	 * @return a time id
	 */
	public long timeId()
	{
		return timeId++;
	}
	
	public void addAntCo2Listener( AntCo2Listener listener )
	{
		listeners.add(listener);
	}
	
	public void removeAntCo2Listener( AntCo2Listener listener )
	{
		listeners.remove(listener);
	}
	
	/**
	 * Add a new colony.
	 * @param name name of the new colony
	 */
	public void addColony( String name )
	{
		int index = 0;
		
		while( index < colonies.size() && colonies.get(index) != null )
			index++;
		
		Colony colony = Colony.newColony(this, name, index);
		
		if( index < colonies.size() ) colonies.set(index,colony);
		else colonies.add(index,colony);
		
		populationPolicy.colonyAdded(colony);
		
		for( AntCo2Listener l: listeners )
			l.colonyAdded(colony);
	}
	
	/**
	 * Remove a colony.
	 * @param name name of the colony to remove
	 */
	public void removeColony( String name )
	{
		Colony toRemove = null;
		
		for( Colony c : coloniesAsIterable )
			if( c.getName().equals(name) )
				toRemove = c;
		
		if( toRemove != null )
			removeColony(toRemove);
	}
	
	/**
	 * Remove a colony.
	 * @param colony colony to remove
	 */
	public void removeColony( Colony colony )
	{
		populationPolicy.colonyRemoved(colony);
		colony.removed();
		colonies.set( colony.getIndex(), null );
		
		for( AntCo2Listener l: listeners )
			l.colonyRemoved(colony);
	}
	
	/**
	 * Access to colonies in a for-each operation.
	 * @return an iterable over colonies
	 */
	public Iterable<Colony> eachColony()
	{
		return coloniesAsIterable;
	}
	
	/**
	 * Access to edges in a for-each operation.
	 * @return an iterable over edges
	 */
	public Iterable<? extends AntCo2Edge> eachEdge()
	{
		return internalGraph.antco2EdgeSet();
	}

	/**
	 * Access to nodes in a for-each operation.
	 * @return an iterable over nodes
	 */
	public Iterable<? extends AntCo2Node> eachNode()
	{
		return internalGraph.antco2NodeSet();
	}

	public void init()
	{
		smoothingBox.init(this);
		measures.init(this);
	}
	
	public void step()
	{
		lock();
		
		for( Colony colony : eachColony() )
			colony.commit();
		
		for( Colony colony : eachColony() )
			colony.step();
		
		for( AntCo2Edge e : eachEdge() )
			e.step(this);
		
		for( AntCo2Node n : eachNode() )
			n.step(this);
		
		populationPolicy.step();
		
		unlock();
		
		for( int i = 0; i < listeners.size(); i++ )
			listeners.get(i).step(this);
		
		measures.step();
	}
	
	/**
	 * Increments jumps count.
	 * @param ant the ant which has jumped
	 */
	public void incrJumps( Ant ant )
	{
		jumps++;

		Colony color = ant.getColony();

		if( color.getIndex() >= jumpsPerColony.length )
			jumpsPerColony = Arrays.copyOf(jumpsPerColony, color.getIndex() + 1 );

		jumpsPerColony [color.getIndex()]++;
	}

	/**
	 * Increments surpopulation count.
	 */
	public void incrSurpop()
	{
		surpop++;
	}

	/**
	 * Increments migrations count.
	 */
	public void incrMigrations()
	{
		migrations++;
	}

	/**
	 * @see org.graphstream.stream.ElementSink
	 */
	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		
	}

	/**
	 * @see org.graphstream.stream.ElementSink
	 */
	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
		
	}

	/**
	 * @see org.graphstream.stream.ElementSink
	 */
	public void graphCleared(String sourceId, long timeId) {
		
	}

	/**
	 * @see org.graphstream.stream.ElementSink
	 */
	public void nodeAdded(String sourceId, long timeId, String nodeId)
	{
		populationPolicy.nodeAdded( (AntCo2Node) internalGraph.getNode(nodeId) );
	}

	/**
	 * @see org.graphstream.stream.ElementSink
	 */
	public void nodeRemoved(String sourceId, long timeId, String nodeId)
	{
		populationPolicy.nodeRemoved( (AntCo2Node) internalGraph.getNode(nodeId) );
	}

	/**
	 * @see org.graphstream.stream.ElementSink
	 */
	public void stepBegins(String sourceId, long timeId, double step) {
		
	}
}
