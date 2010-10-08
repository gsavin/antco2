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

import java.util.*;

import book.*;
import book.gfx.*;

import org.miv.graphstream.graph.*;

/**
 * Ant colony representation.
 * 
 * <p>
 * A colony (or colour) represents a group of ant. Each colony has a unique name. Names are shared
 * across hosts. Additionally, each colony defines a RGB colour and an index. Indices are not shared
 * across hosts, they are only defined for one AntCo2 instance. However, indices are imutable: once
 * a colony is declared with an index, it keeps it until it is removed.
 * </p>
 * 
 * <p>
 * Colonies contain a set of ants used in the simulation.
 * </p>
 * 
 * @author Antoine Dutot
 * @since 22 juil. 2005
 */
public abstract class Colony
{
	// Constants

	/**
	 * Predefined names for colonies.
	 */
	public enum ColonyName
	{
		Red( "Red", 1, 0, 0 ), Green( "Green", 0, 1, 0 ), Blue( "Blue", 0, 0, 1 ), Yellow(
		        "Yellow", 1, 1, 0 ), Magenta( "Magenta", 1, 0, 1 ), Cyan( "Cyan", 0, 1, 1 ), Orange(
		        "Orange", 1, 0.5f, 0 ), Purple( "Purple", 1, 0, 0.5f ), Lime( "Lime", 0.5f, 1, 0 ), Ocean(
		        "Ocean", 0, 1, 0.5f ), Violet( "Violet", 0.5f, 0, 1 ), BlueGreen( "BlueGreen", 0,
		        0.5f, 1 ), White( "White", 1, 1, 1 ), DarkGray( "DarkGray", 0.3f, 0.3f, 0.3f ), LightGray(
		        "LightGray", 0.6f, 0.6f, 0.6f ), Black( "Black", 0, 0, 0 );

		String name;

		Rgba color;

		ColonyName( String name, float red, float green, float blue )
		{
			this.name = name;
			this.color = new Rgba( red, green, blue, 1 );
		}

		public String getName()
		{
			return name;
		}

		public Rgba getColor()
		{
			return color;
		}
	};

	/**
	 * Default colony name set.
	 */
	public static final ColonyName DEFAULT_NAMES[] = { ColonyName.Red, ColonyName.Green,
	        ColonyName.Blue, ColonyName.Yellow, ColonyName.Magenta, ColonyName.Cyan,
	        ColonyName.Orange, ColonyName.Purple, ColonyName.Lime, ColonyName.Ocean,
	        ColonyName.Violet, ColonyName.BlueGreen, ColonyName.White, ColonyName.DarkGray,
	        ColonyName.LightGray, ColonyName.Black };

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
	protected Rgba color;

	/**
	 * Index of this colony in the AntCo2 instance.
	 */
	protected int index;

	/**
	 * To generate new colony names.
	 */
	protected static int arbitraryName = 0;

	/**
	 * Ants of this colony, by their id.
	 */
	protected HashMap<String,Ant> ants = new HashMap<String,Ant>();

	/**
	 * Same as {@link #ants} but used by commit.
	 */
	protected HashMap<String,Ant> antsAdd = new HashMap<String,Ant>();

	/**
	 * Same as {@link #ants} but used by commit.
	 */
	protected HashMap<String,Ant> antsDel = new HashMap<String,Ant>();

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

	// Constructors

	/**
	 * Empty constructor for descendants.
	 */
	protected Colony()
	{
	}

	/**
	 * New colony.
	 * @param context Ant context.
	 * @param name Colony name (unique on all hosts).
	 * @param index Unique index on this host of this colony.
	 * @param red Colour red component.
	 * @param green Colour green component.
	 * @param blue Colour blue component.
	 */
	public Colony( AntContext context, String name, int index, float red, float green, float blue )
	{
		init( context, name, index, red, green, blue );
	}

	/**
	 * Initialize a colony. This method acts as the colony constructor, since colonies have to be
	 * instantiated dynamically, and therefore need a default constructor.
	 * @param context Ant context.
	 * @param name Colony name (unique on all hosts).
	 * @param index Unique index on this host of this colony.
	 * @param red Colour red component.
	 * @param green Colour green component.
	 * @param blue Colour blue component.
	 */
	public void init( AntContext context, String name, int index, float red, float green, float blue )
	{
		assert ctx == null : "cannot call init() on an already initialized colony";

		if( ctx == null )
		{
			this.ctx = context;
			this.name = name;
			this.index = index;
			this.color = new Rgba( red, green, blue, 1 );
		}
	}

// Access

	/**
	 * Colony name.
	 * @return The name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Colony colour.
	 * @return The colour.
	 */
	public Rgba getColor()
	{
		return color;
	}

	/**
	 * Index of this colony in the AntCo2 instance.
	 * @return The index.
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * Number of ants in this colony.
	 * @return Ant count.
	 */
	public int getAntCount()
	{
		return ants.size();
	}

	/**
	 * Number of nodes having this colour.
	 * @return Node count.
	 */
	public int getNodeCount()
	{
		return nodeCount;
	}

	/**
	 * Generate an arbitrary name for a colour.
	 * @return The arbitrary name.
	 */
	public static String generateName()
	{
		return Integer.toString( arbitraryName++ );
	}

	/**
	 * Create a new colony instance based in the species given in the AntCO² parameters.
	 * @param context Ant context (define the class to instantiate).
	 * @param name Colony name (global to all hosts).
	 * @param index Colony index (local to this host).
	 * @param red Colony colour red component.
	 * @param green Colony colour green component.
	 * @param blue Colony color blue component.
	 * @return The newly created colony.
	 * @throws ClassNotFoundException If the species does not identify a class in the classpath.
	 * @throws InstantiationException If the class specified is not instantiable.
	 * @throws IllegalAccessException If the class specified is not accessible.
	 * @throws ExceptionInInitializerError If the class constructor fails.
	 * @throws SecurityException You know why.
	 */
	public static Colony newColony( AntContext context, String name, int index, float red,
	        float green, float blue ) throws ClassNotFoundException, InstantiationException,
	        IllegalAccessException, ExceptionInInitializerError, SecurityException
	{
		Class<?> clazz = Class.forName( context.params.species );

		Object o = clazz.newInstance();

		if( !( o instanceof Colony ) )
			throw new InstantiationException( "The ant species given '" + context.params.species
			        + "' is not an instance of antco2.Colony" );

		Colony colony = (Colony) o;

		colony.init( context, name, index, red, green, blue );

		return colony;
	}

// Commands

	/**
	 * The given node is now of this colour.
	 * @param node A node info associated to a node of the graph.
	 */
	protected void registerNode( NodeInfos node )
	{
		nodeCount++;
		assert node.getColor() == this;
	}

	/**
	 * The given node is no more of this colour.
	 * @param node A node info associated to a node of the graph.
	 */
	protected void unregisterNode( NodeInfos node )
	{
		nodeCount--;
		assert node.getColor() != this;
	}

	/**
	 * Add a new ant to the colony. This method only register an "add action" but will effectively
	 * add the ant only when {@link #commit()} is called.
	 * @param id Ant identifier (null means creating automatically the identifier).
	 * @param start Ant start node.
	 * @throws SingletonException If an ant with the same identifier already exists.
	 */
	public void addAnt( String id, Node start ) throws SingletonException
	{
		if( id == null )
			id = "ant" + Integer.toString( newAntId++ );

		if( antsDel.get( id ) != null )
			antsDel.remove( id );

		if( antsAdd.get( id ) == null )
		{
			Ant ant = createAnt( id, start );
			antsAdd.put( id, ant );
		}

		needCommit = true;
	}

	/**
	 * Remove arbitrarily n ants of this colour.
	 * @param n The number of ants to remove.
	 */
	public void removeAnts( int n )
	{
		for( Ant ant : ants.values() )
		{
			Ant old = antsDel.put( ant.getId(), ant );

			if( old == null )
				n--;

			if( n == 0 )
				break;
		}

		needCommit = true;
	}

	/**
	 * Create an ant. This method must act as an ant factory instantiating the correct ant class,
	 * descendant of Ant depending on the species.
	 * @param id Ant identifier.
	 * @param start Ant start node.
	 */
	protected abstract Ant createAnt( String id, Node start );

	/**
	 * Commit ants add or removal. Ants are not added or removed while a step of AntCO² is running,
	 * however, the {@link #addAnt(String, Node)} can be called at any time. Therefore ant add or
	 * removal are registered in a special buffer, then really added or removed when this method is
	 * called.
	 */
	public void commit()
	{
		if( needCommit )
		{
			for( Ant ant : antsDel.values() )
			{
				Ant old = ants.remove( ant.getId() );
				assert old != null : "an ant '" + ant.getId()
				        + "' that does not exits has been removed";
			}

			for( Ant ant : antsAdd.values() )
			{
				Ant old = ants.put( ant.getId(), ant );
				assert old == null : "identifier '" + ant.getId() + "' is already registered";
			}

			antsAdd.clear();
			antsDel.clear();

			needCommit = false;
		}
	}

	/**
	 * Make all the ants of the colony run.
	 */
	public void step()
	{
		for( Ant ant : ants.values() )
		{
			ant.step();
		}
	}

	/**
	 * Called when a colony is removed. This removes all ants.
	 */
	public void removed()
	{
		for( Ant ant : ants.values() )
		{
			ant.goTo( null );
		}
	}
}