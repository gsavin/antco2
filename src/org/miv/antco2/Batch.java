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

import book.*;

import org.graphstream.algorithm.antco2.policy.ProportionalPopulationPolicy;
import org.miv.graphstream.io.*;

/**
 * Run AntCO² in batch mode.
 * 
 * @author Antoine Dutot
 * @since 24 juil. 2005
 */
public class Batch implements AntCo2Listener, GraphReaderListener
{
// Attributes

	/**
	 * AntCO² parameters.
	 */
	protected BatchParams params;

	/**
	 * The algorithm.
	 */
	protected AntCo2 antco2;

	/**
	 * Stop the algorithm as soon as this is false.
	 */
	protected boolean loop;

	/**
	 * Graph processing is finished.
	 */
	protected boolean graphFinished;

	/**
	 * Reader for the graph.
	 */
	protected GraphReader graphInput;

	/**
	 * Random number generator.
	 */
	protected Random random;

	/**
	 * If the graph speed factor is negative, how many AntCO² steps it remains to wait before the
	 * next graph step.
	 */
	public int graphNextStep = 0;

	/**
	 * Used when reading a graph having "step" external commands to delimit steps.
	 */
	public boolean step = false;

// Constructors

	/**
	 * Run the AntCO² batch.
	 */
	public static void main( String args[] )
	{
		new Batch( args );
	}

	/**
	 * New AntCO² batch.
	 * @param args Command-line parameters.
	 */
	public Batch( String args[] )
	{
		params = BatchParams.paramsFromArgs( args );
		random = new Random( params.randomSeed );

		String err = params.ensureCoherence();

		if( err != null )
		{
			System.err.printf( "Errors:%n%s%n", err );
			System.err.printf( "Exiting...%n" );
			// System.exit( 1 );
		}

		params.toStream( System.out );

		antco2 = new AntCo2( params.antParams, this, new ProportionalPopulationPolicy() );

		initGraph();
		run();
	}

// Access

	/**
	 * Randomly select a colony in the set of known colonies.
	 */
	protected Colony getRandomColor()
	{
		int n = antco2.getColorCount();
		int r = random.nextInt( n );
		Colony c = null;

		if( n <= 0 )
			throw new RuntimeException( "no colonies!!!" );

		while( c == null )
			c = antco2.getColor( r );

		return c;
	}

// Commands

	/**
	 * Report a fatal error and exit.
	 * @param message Error message.
	 */
	protected void fatalError( String message )
	{
		System.err.printf( "%s%n", message );
		System.exit( 1 );
	}

	/**
	 * Report an error.
	 * @param message Error message.
	 */
	protected void error( String message )
	{
		System.err.printf( "%s%n", message );
	}

	protected void initGraph()
	{
		try
		{
			graphInput = GraphReaderFactory.readerFor( params.graphFileName );

			if( graphInput instanceof GraphReaderGML )
			{
				GraphReaderGML gml = (GraphReaderGML) graphInput;
				gml.addAttributeClass( "weight", "org.miv.antco2.Weight" );
			}

			graphInput.addGraphReaderListener( this );
			graphInput.begin( params.graphFileName );
		}
		catch( NotFoundException e )
		{
			fatalError( "cannot find graph file '" + params.graphFileName + "': " + e.getMessage() );
		}
		catch( GraphParseException e )
		{
			fatalError( "parse error while reading graph file '" + params.graphFileName + "': "
			        + e.getMessage() );
		}
		catch( IOException e )
		{
			fatalError( "I/O error while reading graph file '" + params.graphFileName + "': "
			        + e.getMessage() );
		}
		/*
		 * try { graphInput = new GraphReaderGML(); graphInput.addAttributeClass( "weight",
		 * "org.miv.antco2.Weight" ); graphInput.begin( params.graphFileName, this ); } catch(
		 * NotFoundException e ) { fatalError( "cannot find graph file '" + params.graphFileName +
		 * "': " + e.getMessage() ); } catch( GraphParseException e ) { fatalError( "parse error
		 * while reading graph file '" + params.graphFileName + "': " + e.getMessage() ); } catch(
		 * IOException e ) { fatalError( "I/O error while reading graph file '" +
		 * params.graphFileName + "': " + e.getMessage() ); }
		 */
	}

	/**
	 * Run the AntCO² algorithm.
	 */
	protected void run()
	{
		loop = true;

		while( loop )
		{
			readGraphStep();
			antco2.step();
		}
	}

	/**
	 * Read the graph.
	 */
	protected void readGraphStep()
	{
		int steps = 1;

		if( !graphFinished )
		{
			if( params.byStep )
			{
				step = false;

				do
				{
					readGraph();
					steps++;

					if( steps % 10000 == 0 )
						System.out.printf( "%d graph events read%n", steps );

					if( graphFinished )
						break;
				}
				while( step == false );
			}
			else
			{
				if( params.graphSpeed > 0 )
				{
					for( int i = 0; i < params.graphSpeed; ++i )
					{
						readGraph();

						if( graphFinished )
							break;
					}
				}
				else
				{
					if( graphNextStep > 0 )
					{
						graphNextStep--;
					}
					else
					{
						readGraph();
						graphNextStep = -params.graphSpeed;
					}
				}
			}
		}
	}

	/**
	 * Read one minimal set of graph events.
	 */
	protected void readGraph()
	{
		try
		{
			if( !graphInput.nextEvents() )
			{
				graphFinished = true;
				graphInput.end();
			}
		}
		catch( GraphParseException e )
		{
			fatalError( "parse error while reading graph '" + params.graphFileName + "': "
			        + e.getMessage() );
		}
		catch( IOException e )
		{
			fatalError( "I/O error while reading graph '" + params.graphFileName + "': "
			        + e.getMessage() );
		}
	}

	// Commands -- AntCO2 events.

	public void stepFinished( int step, Metrics metrics )
	{
		// System.out.printf( "%nStep %d -----------------------------%n", step );
		// System.out.printf( " ants ........................ %d%n", metrics.antCount );
		// System.out.printf( " graph ....................... nodes=%d, edges=%d, finished=%s%n",
		// metrics.nodeCount, metrics.edgeCount, graphFinished ? "yes" : "no" );
		// System.out.printf( " Weights ..................... cut=%d, cutWeight=%f, weight=%f,
		// R1=%f%n", metrics.cutEdges, metrics.cutWeight, metrics.weight, metrics.r1 );
		// System.out.printf( " Partitions .................. min=%d, max=%d, R2=%f%n",
		// metrics.minPart, metrics.maxPart, metrics.r2 );
		// System.out.printf( " Jump ........................ count=%d, r3=%f%n", metrics.jumpCount,
		// metrics.r3 );
		// System.out.printf( " Overpop ..................... count=%d, r4=%f%n",
		// metrics.surpopCount, metrics.r4 );

		System.out.printf( " [%5d] R1=%1.3f R2=%1.3f R3=%1.3f R4=%1.3f ", step, metrics.r1,
		        metrics.r2, metrics.r3, metrics.r4 );

		System.out.printf( "[" );
		int scale = 40;
		for( int i = 0; i < scale; ++i )
		{
			if( ( (int) ( metrics.r1 * scale ) ) == i )
				System.out.printf( "|" );
			else
				System.out.printf( "-" );
		}
		System.out.printf( "] [" );
		for( int i = 0; i < scale; ++i )
		{
			if( ( (int) ( metrics.r2 * scale ) ) == i )
				System.out.printf( "|" );
			else
				System.out.printf( "-" );
		}
		System.out.printf( "]%n" );

		if( params.batch > 0 && params.batch <= step )
		{
			System.out.printf( "%nMax steps reached, exiting...%n%n" );
			loop = false;
		}
	}

	public void stepCompletion( int time, float completionPercent )
	{
	}

	public void edgeInfos( String tag, EdgeInfos infos )
	{

	}

	public void nodeInfos( String tag, NodeInfos infos )
	{
		// if( infos.getColor() != infos.getOldColor() )
		// System.out.printf( "*** Node %s changed color %s -> %s ***%n", tag,
		// infos.getOldColor().getName(), infos.getColor().getName() );
	}

	public void colonyInfos( String name, int index, int population, Colony colony )
	{
		// NOP
	}

	public void warning( String message )
	{
		error( message );
	}

	public void error( String message, Exception ex, boolean isFatal )
	{
		if( isFatal )
		{
			fatalError( message );
		}
		else
		{
			error( message );
		}
	}

	// Commands -- Graph reader events

	public void nodeAdded( String id, Map<String,Object> attributes ) throws GraphParseException
	{
		antco2.addNode( id, 1, getRandomColor() );
	}

	public void nodeChanged( String id, Map<String,Object> attributes ) throws GraphParseException
	{
		// TODO
		System.err.printf( "TODO: node changed" );
	}

	public void nodeRemoved( String id ) throws GraphParseException
	{
		antco2.removeNode( id );
	}

	public void edgeAdded( String id, String from, String to, boolean directed,
	        Map<String,Object> attributes ) throws GraphParseException
	{
		antco2.addEdge( id, from, to, 1 );
	}

	public void edgeChanged( String id, Map<String,Object> attributes ) throws GraphParseException
	{
		Object o = attributes.get( "weight" );

		if( o instanceof Number )
		{
			antco2.changeEdgeValue( id, ( (Number) o ).floatValue() );
		}
		else
		{
			throw new RuntimeException( "invalid weight type" );
		}
	}

	public void edgeRemoved( String id ) throws GraphParseException
	{
		antco2.removeEdge( id );
	}

	public void graphChanged( Map<String,Object> arg0 ) throws GraphParseException
	{
	}

	public void stepBegins( double time ) throws GraphParseException
	{
		step = true;
	}

	public void unknownEventDetected( String unknown ) throws GraphParseException
	{
		if( unknown.startsWith( "addColony" ) )
		{
			System.err.println( "TODO addColony external command" );
		}
		else if( unknown.startsWith( "removeColony" ) )
		{
			System.err.println( "TODO removeColony external command" );
		}
		else if( unknown.equals( "STEP" ) || unknown.equals( "step" ) )
		{
			step = true;
		}
		else
		{
			error( "unimplemented external command found in graph file: '" + unknown + "'" );
		}
	}
}