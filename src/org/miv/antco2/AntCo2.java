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

import book.*;
import book.set.FixedArrayList;
import book.gfx.*;

import org.miv.graphstream.graph.*;
import org.miv.graphstream.io.*;

import java.io.*;
import java.util.*;

/**
 * AntCO� implementation.
 * 
 * <p>
 * The AntCO� implementation acts as a black box, taking as input informations on graph representing
 * an interaction network, and outputting informations on a partioning of this graph in
 * "organisations", each organisation being of a distinct colour.
 * </p>
 * 
 * <p>
 * The input interface is composed of methods:
 * <ol>
 * 		<li>{@link #addNode(String, float, Colony)}</li>
 * 		<li>{@link #changeNodeValue(String, float)}</li>
 * 		<li>{@link #removeNode(String)}</li>
 * 		<li>{@link #addEdge(String, String, String, float)}</li>
 * 		<li>{@link #changeEdgeValue(String, float)}</li>
 * 		<li>{@link #removeEdge(String)}</li>
 * 		<li>{@link #addColony(String, float, float, float)}</li>
 * 		<li>{@link #removeColony(String)}</li>
 * 		<li>{@link #flytoxx(float)}</li>
 * 		<li>{@link #step()}</li>
 * </ol>
 * These methods allow to input the graph representing the problem and to iterate the ant algorithm.
 * They also allow to add or remove whole colours/colonies to the algorithm. Note that if non-zero
 * in the AntParams, the {@link org.miv.antco2.AntParams#colors} field is used to add initial
 * colonies with default names.
 * </p>
 * 
 * <p>
 * The output interface is a listener: {@link org.miv.antco2.AntCo2Listener}. It defines methods to
 * know when:
 * <ol>
 * <li>a step finished {@link org.miv.antco2.AntCo2Listener#stepFinished(int, Metrics)}</li>
 * <li>a node changed {@link org.miv.antco2.AntCo2Listener#nodeInfos(String, NodeInfos)}</li>
 * <li>an edge changed {@link org.miv.antco2.AntCo2Listener#edgeInfos(String, EdgeInfos)}</li>
 * <li>an error occurred {@link org.miv.antco2.AntCo2Listener#error(String, Exception, boolean)}</li>
 * </ol>
 * This listener is called inside the {@link #step()} method, however changes to the AntCo2 instance
 * are not possible while this method is active (you cannot add/remove/change nodes, edges and
 * colonies).
 * </p>
 * 
 * @author Antoine Dutot
 */
public class AntCo2
{
// Attributes

	/**
	 * Parameters.
	 */
	protected AntParams params;

	/**
	 * Ant shared informations.
	 */
	protected AntContext ctx;

	/**
	 * Listener for AntCO� events.
	 */
	protected AntCo2Listener listener;

	/**
	 * All the known colonies, indexed by their name.
	 */
	protected HashMap<String,Colony> colonyMap = new HashMap<String,Colony>();

	/**
	 * All the known colonies sorted by their index. A colony never changes its index.
	 */
	protected FixedArrayList<Colony> colonies = new FixedArrayList<Colony>();

	/**
	 * Used to avoid modifying this object while the ants are running.
	 */
	protected boolean locked = false;

	/**
	 * Population policy.
	 */
	protected PopulationPolicy popol;

	/**
	 * Set to true at the end of each step if an event tells that the graph changed.
	 */
	protected boolean graphChanged = false;

	/**
	 * Graph writer, allowing to output this simulation events.
	 */
	protected GraphWriterDGS dgsOut = null;

	/**
	 * Map of attributes to output a DGS graph if needed.
	 */
	protected HashMap<String,Object> attrMap = null;

// Constructors

	/**
	 * New empty ant nest.
	 * @param params The AntCO� parameter set.
	 * @param listener The initial listener on the AntCO� algorithm.
	 * @param popol The population policy, how ants are allocated, etc.
	 */
	public AntCo2( AntParams params, AntCo2Listener listener, PopulationPolicy popol )
	{
		this.params   = params;
		this.listener = listener;
		this.ctx      = new AntContext( params, null );
		this.popol    = popol;

		popol.setAntCo2( this );
		initColonies();
		initDgsOutput();
	}

	/**
	 * Create a first set of colonies according to the number of colours defined in the AntParams.
	 */
	protected void initColonies()
	{
		int n = params.colors;

		params.colors = 0;

		for( int i = 0; i < n; ++i )
		{
			Rgba color;
			float red, green, blue;
			String name;

			if( i < Colony.DEFAULT_NAMES.length )
			{
				name = Colony.DEFAULT_NAMES[i].getName();
				color = Colony.DEFAULT_NAMES[i].getColor();
				red = color.red;
				green = color.green;
				blue = color.blue;
			}
			else
			{
				name = Colony.generateName();
				red = ctx.random.nextFloat();
				green = ctx.random.nextFloat();
				blue = ctx.random.nextFloat();
			}

			try
			{
				addColony( name, red, green, blue );
			}
			catch( Exception e )
			{
				listener.error( "Error when creating colony '" + name + "': " + e.getMessage(), e,
				        true );
			}
		}
	}

	/**
	 * Open, if needed an output for a DGS graph that will contain all events occurring during this
	 * simulation.
	 */
	protected void initDgsOutput()
	{
		if( params.outputDGS != null )
		{
			try
			{
				GraphWriterDGS dgs = new GraphWriterDGS();
				dgs.begin( params.outputDGS, params.outputDGS );

				attrMap = new HashMap<String,Object>();
				dgsOut = dgs;
			}
			catch( IOException e )
			{
				// Failed to open the output, but do not stop.
				// Then, to decide if we must output something, we will
				// test if dgsOut is non-null instead of testing if
				// params.outputDGS is non-null.

				e.printStackTrace();
			}
		}
	}

// Access

	/**
	 * AntCO� parameters.
	 */
	public AntParams getAntParams()
	{
		return params;
	}

	/**
	 * Informations tied to a node.
	 * @param tag Node name.
	 * @return Informations or null if no node matches the given name.
	 */
	public NodeInfos getNodeInfos( String tag )
	{
		Node node = ctx.graph.getNode( tag );

		if( node != null )
		{
			NodeInfos infos = (NodeInfos) node.getAttribute( NodeInfos.ATTRIBUTE_NAME,
			        NodeInfos.class );
			assert infos != null : "All nodes should have a NodeInfo attribute";
			return infos;
		}

		return null;
	}

	/**
	 * Informations tied to an edge.
	 * @param tag Edge name.
	 * @return Informations or null if no edge matches the given name.
	 */
	public EdgeInfos getEdgeInfos( String tag )
	{
		Edge edge = ctx.graph.getEdge( tag );

		if( edge != null )
		{
			EdgeInfos infos = (EdgeInfos) edge.getAttribute( EdgeInfos.ATTRIBUTE_NAME,
			        EdgeInfos.class );
			assert infos != null : "All edges should have a EdgeInfos attribute";
			return infos;
		}

		return null;
	}

	/**
	 * Number of colonies.
	 * @return The number of colours/colonies.
	 */
	public int getColorCount()
	{
		assert colonies.size() == params.colors : "discrepancy between the number of colonies and params.colors";
		return colonies.size();
	}

	/**
	 * Get a colony by its name.
	 * @param name Colony name.
	 * @return The colony or null if no colony matches the given name.
	 */
	public Colony getColor( String name )
	{
		return colonyMap.get( name );
	}

	/**
	 * Get a colony by its invariant index. Indices are only valid on one host. To identify a colony
	 * on the whole system use its name.
	 * @param index The colony index.
	 * @return The colony at the given index or null if no colony has this index.
	 */
	public Colony getColor( int index )
	{
		if( index < 0 || index >= colonies.size() )
			return null;

		return colonies.get( index );
	}

	/**
	 * Colony set.
	 * @return An iterable collection of colours.
	 */
	public Collection<Colony> getColonies()
	{
		return colonies;
	}

	/**
	 * Index that will be used if a new colony is inserted.
	 * @return The index.
	 */
	public int getNextColonyIndex()
	{
		return colonies.getNextAddIndex();
	}

	/**
	 * Ant environment. Graph used by the ants to store pheromone. Each node of this graph has a
	 * {@link NodeInfos} attribute under the name {@link NodeInfos#ATTRIBUTE_NAME}, and each edge
	 * has a {@link EdgeInfos} attribute under the name {@link EdgeInfos#ATTRIBUTE_NAME}.
	 * @return The ant graph.
	 */
	public Graph getGraph()
	{
		return ctx.graph;
	}

	/**
	 * Various statistics and results.
	 * @return AntCO� metrics.
	 */
	public Metrics getMetrics()
	{
		return ctx.metrics;
	}

	/**
	 * Ant context.
	 * @return The ant context.
	 */
	public AntContext getContext()
	{
		return ctx;
	}
	
	/**
	 * The random number generator used by AntCO�.
	 * @return An instance of the 'Random' class.
	 */
	public Random getRandomNumberGenerator()
	{
		return ctx.random;
	}

// Commands

	/**
	 * Add a new colony.
	 * @param name Colony name (shared between hosts).
	 * @param red Colony colour red component.
	 * @param green Colony colour green component.
	 * @param blue Colony colour blue component.
	 * @throws SingletonException If a colony by that name already exists.
	 * @throws ClassNotFoundException If the ant species given in the AntCO� parameters is not in
	 *         the class path.
	 * @throws InstantiationException If the ant species cannot be created.
	 * @throws IllegalAccessException If the ant species is not accessible.
	 * @throws SecurityException You known why.
	 */
	public void addColony( String name, float red, float green, float blue )
	        throws SingletonException, ClassNotFoundException, InstantiationException,
	        IllegalAccessException, SecurityException
	{
		if( locked )
			throw new IllegalStateException( "cannot add a colony while AntCo2.step() is running" );

		try
		{
			int idx = colonies.getNextAddIndex();
			Colony clr = Colony.newColony( ctx, name, idx, red, green, blue );
			Colony old = colonyMap.put( name, clr );

			if( old != null && params.ayatollahMode )
			{
				colonyMap.put( name, old );
				throw new SingletonException( "a colony named '" + name
				        + "' already exists, cannot add a new one" );
			}
			else
			{
				colonies.add( clr );

				assert colonies.getLastIndex() == idx : "discrepancy between indices in the FixedArrayList";
			}

			params.colors++;

			popol.colorAdded( clr );
			listener.colonyInfos( name, idx, clr.getAntCount(), clr );
		}
		catch( ExceptionInInitializerError e )
		{
			throw new InstantiationException( "Cannot instantiate colony: " + e.getMessage() );
		}
	}

	/**
	 * Remove a colony. All the colony ants are removed.
	 * @param name Colony name.
	 */
	public void removeColony( String name )
	{
		if( locked )
			throw new IllegalStateException(
			        "cannot remove a colony while AntCo2.step() is running" );

		Colony color = colonyMap.remove( name );

		if( color != null )
		{
			popol.colorRemoved( color );
			color.removed();
			colonies.remove( color.getIndex() );
			params.colors--;
			listener.colonyInfos( name, color.getIndex(), -1, null );
		}
	}

	/**
	 * Declare a new node.
	 * @param tag Node name.
	 * @param value Node value.
	 * @param initialColor Node start colour.
	 * @throws SingletonException If a node with the same tag already exists.
	 */
	public void addNode( String tag, float value, Colony initialColor ) throws SingletonException,
	        NotFoundException
	{
		if( locked )
			throw new IllegalStateException( "cannot add a node while AntCo2.step() is running" );

		try
		{
			Node node = ctx.graph.addNode( tag );
			NodeInfos infos = new NodeInfos( value, initialColor, params, node );
			node.addAttribute( NodeInfos.ATTRIBUTE_NAME, infos );

			// if( label != null )
			// node.addAttribute( "label", label );

			popol.nodeAdded( node, infos );
			graphChanged = true;

			if( dgsOut != null )
			{
				try
				{
					attrMap.clear();
					attrMap.put( "color", infos.color.getName() );
					dgsOut.addNode( tag, attrMap );
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
		catch( SingletonException e )
		{
			if( params.ayatollahMode )
				throw e;
		}
	}

	/**
	 * Change the node value.
	 * @param tag Node name.
	 * @param value New node value.
	 */
	public void changeNodeValue( String tag, float value )
	{
		if( locked )
			throw new IllegalStateException( "cannot change a node while AntCo2.step() is running" );

		Node node = ctx.graph.getNode( tag );

		if( node != null )
		{
			NodeInfos infos = (NodeInfos) node.getAttribute( NodeInfos.ATTRIBUTE_NAME,
			        NodeInfos.class );
			infos.setValue( value );
		}
	}

	/**
	 * Remove a node.
	 * @param tag Node name.
	 * @return The parameters of the node or null if no node matched the given tag.
	 */
	public NodeInfos removeNode( String tag )
	{
		if( locked )
			throw new IllegalStateException( "cannot remove a node while AntCo2.step() is running" );

		Node node = ctx.graph.removeNode( tag );

		if( node != null )
		{
			NodeInfos infos = (NodeInfos) node.getAttribute( NodeInfos.ATTRIBUTE_NAME,
			        NodeInfos.class );
			assert infos != null : "All nodes should have a NodeInfos attribute";
			popol.nodeRemoved( node, infos );

			if( dgsOut != null )
			{
				try
				{
					dgsOut.delNode( tag );
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}

			return infos;
		}

		graphChanged = true;
		return null;
	}

	/**
	 * Declare a new edge. If the nodes referenced by the new edge are not yet added, they are
	 * automatically added, their value is 0 and their initial colour is the first colour.
	 * @param tag Edge name.
	 * @param from Source node name.
	 * @param to Destination node name.
	 * @param value Edge value.
	 * @throws SingletonException If an edge with the same tag already exists or if an edge between
	 *         the same nodes exists.
	 */
	public void addEdge( String tag, String from, String to, float value )
	        throws SingletonException
	{
		if( locked )
			throw new IllegalStateException( "cannot add an edge while AntCo2.step() is running" );

		if( !params.ayatollahMode )
		{
			if( ctx.graph.getNode( from ) == null )
				addNode( from, 0, colonies.get( 0 ) );

			if( ctx.graph.getNode( to ) == null )
				addNode( to, 0, colonies.get( 0 ) );
		}

		Edge edge = ctx.graph.addEdge( tag, from, to, false );
		edge.addAttribute( EdgeInfos.ATTRIBUTE_NAME, new EdgeInfos( edge, value, ctx ) );
		graphChanged = true;

		if( dgsOut != null )
		{
			try
			{
				attrMap.clear();
				dgsOut.addEdge( tag, from, to, false, attrMap );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Change the edge value.
	 * @param tag Edge name.
	 * @param value New edge value.
	 */
	public void changeEdgeValue( String tag, float value )
	{
		if( locked )
			throw new IllegalStateException( "cannot change an edge while AntCo2.step() is running" );

		Edge edge = ctx.graph.getEdge( tag );

		if( edge != null )
		{
			EdgeInfos infos = (EdgeInfos) edge.getAttribute( EdgeInfos.ATTRIBUTE_NAME,
			        EdgeInfos.class );
			infos.setValue( value );
		}
	}

	/**
	 * Remove an edge.
	 * @param tag Edge name.
	 * @return The parameters of the edge.
	 */
	public EdgeInfos removeEdge( String tag )
	{
		if( locked )
			throw new IllegalStateException( "cannot remove an edge while AntCo2.step() is running" );

		Edge edge = ctx.graph.removeEdge( tag );

		if( edge != null )
		{
			EdgeInfos infos = (EdgeInfos) edge.getAttribute( EdgeInfos.ATTRIBUTE_NAME,
			        EdgeInfos.class );

			if( dgsOut != null )
			{
				try
				{
					dgsOut.delEdge( tag );
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}

			return infos;
		}

		graphChanged = true;
		return null;
	}

	/**
	 * Reset all pheromones to the given value.
	 * @param value The pheromone absolute value.
	 */
	public void flytoxx( float value )
	{
		if( locked )
			throw new IllegalStateException( "cannot flytoxx while AntCo2.step() is running" );

		Iterator<? extends Edge> edges = ctx.graph.getEdgeIterator();

		while( edges.hasNext() )
		{
			Edge edge = edges.next();
			EdgeInfos info = (EdgeInfos) edge.getAttribute( EdgeInfos.ATTRIBUTE_NAME );
			assert info != null : "each edge must have an EdgeInfos attribute";

			info.setPheromon( null, value );
		}
	}

	/**
	 * Reset all pheromones of a given colour to the given value.
	 * @param color The pheromone colour.
	 * @param value The pheromone absolute value.
	 */
	public void flytoxx( Colony color, float value )
	{
		if( locked )
			throw new IllegalStateException( "cannot flytoxx while AntCo2.step() is running" );

		Iterator<? extends Edge> edges = ctx.graph.getEdgeIterator();

		while( edges.hasNext() )
		{
			Edge edge = edges.next();
			EdgeInfos info = (EdgeInfos) edge.getAttribute( EdgeInfos.ATTRIBUTE_NAME );
			assert info != null : "each edge must have an EdgeInfos attribute";

			info.setPheromon( color, value );
		}
	}

	/**
	 * Output the internal graph as a GML file with colours. If the given filename has no extension
	 * (does not contain a dot), a ".gml" is added. Be careful this is a snapshot, not the whole
	 * graph modification history.
	 * @param fileName The graph to output.
	 * @throws SingletonException Raised if the file already exists.
	 * @throws IOException Raised for any output error.
	 */
	public void outputGml( String fileName ) throws SingletonException, IOException
	{
		// TODO: create a GML output in GraphStream
		ctx.graph.write( new GraphWriterDGS(), fileName );
	}

	/**
	 * Output the internal graph as a DGS file with colours. If the given filename has no extension
	 * (does not contain a dot), a ".dgs" is added. Be careful this is a snapshot not the whole
	 * graph modification history.
	 * @param fileName The graph to output.
	 * @throws SingletonException Raised if the file already exists.
	 * @throws IOException Raised for any output error.
	 */
	public void outputDgs( String fileName ) throws SingletonException, IOException
	{
		ctx.graph.write( new GraphWriterDGS(), fileName );
	}

	/**
	 * Output a map of node names to colours. The map will have a line for each node of the internal
	 * graph. The line will be of the form "nodeName: colorName red green blue". The red/green/blue
	 * values are real numbers between 0 and 1 included. If the filename has no extension (does not
	 * contain a dot), a ".antclr" is added.
	 * @param fileName Name of the file to output.
	 * @param overwrite If false and the file already exists a SingletonException is raised.
	 * @throws SingletonException Raised if overwrite is false and the file already exists.
	 * @throws IOException Raised for any output error.
	 */
	public void outputColors( String fileName, boolean overwrite ) throws SingletonException,
	        IOException
	{
		if( fileName.indexOf( '.' ) < 0 )
			fileName = fileName + ".antclr";

		File file = new File( fileName );

		if( !overwrite && file.exists() )
			throw new SingletonException( "file '" + fileName
			        + "' already exists (while generating a color map on disk)" );

		PrintStream out = new PrintStream( file );
		Iterator<? extends Node> nodes = ctx.graph.getNodeIterator();

		while( nodes.hasNext() )
		{
			Node node = nodes.next();
			NodeInfos infos = (NodeInfos) node.getAttribute( NodeInfos.ATTRIBUTE_NAME );
			assert infos != null : "all nodes should have a NodeInfos attribute";
			Colony color = infos.getColor();

			out.printf( "%s:%s %d %d %d%n", node.getId(), color.getName(),
			        (int) ( color.getColor().red * 255 ), (int) ( color.getColor().green * 255 ),
			        (int) ( color.getColor().blue * 255 ) );
		}

		out.flush();
		out.close();
	}

	/**
	 * Output the state of each node and edge, their colour, pheromone values, ant count, etc. Each
	 * node is printed on one line of the form "N <tag>: <totalAntCount> <mainColorName>
	 * <mainColorRed> <mainColorGreen> <mainColorBlue> [ <antCountOfColor1> <antCountOfColor2> etc. ]"
	 * Each edge is printed on one line of the form "E <tag> <sourceNodeTag> <targetNodeTag>:
	 * <pheomoneTotal> [ <pheromoneOfColor1> <pheromoneOfColor2> etc. ]"
	 * @param fileName The output file name.
	 * @param overwrite Of true and the file already exists, it is overwritten.
	 * @throws SingletonException Raised if overwrite is false and the file already exits.
	 * @throws IOException Raised for any output error.
	 */
	public void outputState( String fileName, boolean overwrite ) throws SingletonException,
	        IOException
	{
		if( fileName.indexOf( '.' ) < 0 )
			fileName = fileName + ".antco2";

		File file = new File( fileName );

		if( !overwrite && file.exists() )
			throw new SingletonException( "file '" + fileName
			        + "' already exists (while generating the AntCO� state on disk)" );

		PrintStream out = new PrintStream( file );
		Iterator<? extends Node> nodes = ctx.graph.getNodeIterator();

		int colorCount = colonyMap.size();

		while( nodes.hasNext() )
		{
			Node node = nodes.next();
			NodeInfos infos = (NodeInfos) node.getAttribute( NodeInfos.ATTRIBUTE_NAME );
			assert infos != null : "all nodes should have a NodeInfos attribute";
			Colony color = infos.getColor();

			// Node <tag>: <antCount> <colorName> <red> <green> <blue>
			out.printf( "N %s: %d %s %d %d %d", node.getId(), infos.totalAntCount, color.name,
			        (int) ( color.getColor().red * 255 ), (int) ( color.getColor().green * 255 ),
			        (int) ( color.getColor().blue * 255 ) );

			out.printf( " [" );

			for( int i = 0; i < colorCount; ++i )
				out.printf( " %d", infos.antCountsPerColor.get( i ) );

			out.printf( " ] %f %s%n", infos.getColorRatio(), infos.getSecondColor().getName() );
		}

		Iterator<? extends Edge> edges = ctx.graph.getEdgeIterator();

		while( edges.hasNext() )
		{
			Edge edge = edges.next();
			EdgeInfos infos = (EdgeInfos) edge.getAttribute( EdgeInfos.ATTRIBUTE_NAME );
			assert infos != null : "all edges should have a EdgeInfos attribute";

			out.printf( "E %s %s %s: %f", edge.getId(), edge.getSourceNode().getId(), edge
			        .getTargetNode().getId(), infos.getPheromonTotal() );

			out.printf( " [" );

			for( int i = 0; i < colorCount; ++i )
				out.printf( " %f", infos.getPheromon( i ) );

			out.printf( " ]%n" );
		}
	}

	/**
	 * Output all the colour changes to the current dynamic graph for the current step.
	 * @throws IOException If the output fails.
	 */
	protected void outputDgs() throws IOException
	{
		Iterator<? extends Node> nodes = ctx.graph.getNodeIterator();

		attrMap.clear();

		while( nodes.hasNext() )
		{
			Node node = nodes.next();
			NodeInfos infos = (NodeInfos) node.getAttribute( NodeInfos.ATTRIBUTE_NAME );
			assert infos != null : "all nodes should have a NodeInfos attribute";

			if( infos.oldColor != infos.color )
			{
				attrMap.put( "color", infos.color.getName() );
				dgsOut.changeNode( node.getId(), attrMap );
			}
		}
	}

// Commands -- Run

	long lastTime = 0;

	/**
	 * Run one step of the algorithm.
	 */
	public void step()
	{
		if( dgsOut != null )
		{
			try
			{
				dgsOut.step( ctx.time );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}

		if( lastTime > 1000 || lastTime == 0 )
		{
			// System.err.println( "AntCO�: new step" );
		}

		long t1 = System.currentTimeMillis();
		locked = true;
		commitColonies();
		long t2 = System.currentTimeMillis();
		stepAnts();
		long t3 = System.currentTimeMillis();
		stepGraph();
		long t4 = System.currentTimeMillis();
		stepColorOutput();
		locked = false;
		long t5 = System.currentTimeMillis();
		ctx.step( colonies, graphChanged );
		listener.stepFinished( ctx.time, ctx.metrics );
		long t6 = System.currentTimeMillis();

		lastTime = t6 - t1;

		if( lastTime > 1000 )
		{
			System.out.printf( "AntCo� step longer than a second (%fs):%n", lastTime / 1000f );
			System.out.printf( "     commit colonies: %d ms%n", ( t2 - t1 ) );
			System.out.printf( "     ants:            %d ms%n", ( t3 - t2 ) );
			System.out.printf( "     graph:           %d ms%n", ( t4 - t3 ) );
			System.out.printf( "     output:          %d ms%n", ( t5 - t4 ) );
			System.out.printf( "     ctx:             %d ms%n", ( t6 - t5 ) );
		}

		graphChanged = false;
	}

	/**
	 * Commit all changes to the colonies (ant add and removal).
	 */
	protected void commitColonies()
	{
		for( Colony color : colonies )
		{
			color.commit();
		}
	}

	/**
	 * Run all the ants. This makes only buffered changes to the environment, however when reading
	 * the environment the values are unchanged.
	 */
	protected void stepAnts()
	{
		assert locked;

		if( lastTime > 1000 )
		{
			System.err.printf( "[" );

			for( Colony color : colonies )
			{
				System.err.printf( " %s ", color.getName() );
				color.step();
			}

			System.err.printf( "]%n" );
		}
		else
		{
			for( Colony color : colonies )
				color.step();
		}
		/*
		 * Iterator<? extends Ant> k = colonies.get(0).ants.values().iterator();
		 * 
		 * if( k.hasNext() ) { Ant a = k.next();
		 * 
		 * System.err.printf( "Ant goes to %s%n", a.curNode.getTag() ); }
		 */}

	/**
	 * Run all graph active elements (nodes, and edges). This is here, for example, that pheromone
	 * evaporation takes place.
	 */
	protected void stepGraph()
	{
		assert locked;

		// First run all edges. This setup the pheromone values.

		Iterator<? extends Edge> edges = ctx.graph.getEdgeIterator();

		while( edges.hasNext() )
		{
			Edge edge = edges.next();
			EdgeInfos info = (EdgeInfos) edge.getAttribute( EdgeInfos.ATTRIBUTE_NAME );

			assert info != null : "each edge must have a EdgeInfos attribute";

			info.step( this );
		}

		// Then run each node. This computes the colour changes.

		Iterator<? extends Node> nodes = ctx.graph.getNodeIterator();

		while( nodes.hasNext() )
		{
			Node node = nodes.next();
			NodeInfos info = (NodeInfos) node.getAttribute( NodeInfos.ATTRIBUTE_NAME );

			assert info != null : "each node must have a NodeInfos attribute";

			info.step( this );
		}
	}

	protected void stepColorOutput()
	{
		if( params.outputColorFileEvery > 0 )
		{
			if( ctx.time % params.outputColorFileEvery == 0 )
			{
				try
				{
					outputColors( "antco2.clr", true );
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
		else if( dgsOut != null )
		{
			try
			{
				outputDgs();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
	}

// Nested classes

	/**
	 * AntCo2 runner.
	 * 
	 * The runner is a thread that execute colonies in parallel.
	 * 
	 * @author Antoine Dutot
	 * @since 7 janv. 2006
	 */
	class AntCo2Runner extends Thread
	{
	// Attributes

		public ArrayList<Colony> colonies = new ArrayList<Colony>();

	// Constructors

		public AntCo2Runner( String id )
		{
			super( id );
		}

	// Access

	// Commands

		public void addColony( Colony c )
		{
			colonies.add( c );
		}

		public void run()
		{
		}
	}

	// End
}