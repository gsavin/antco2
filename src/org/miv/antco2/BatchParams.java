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

import java.io.PrintStream;


public class BatchParams
{
// Attributes

	/**
	 * Random seed.
	 */
	public long randomSeed = -1;

	/**
	 * Suffix to add to each created file.
	 */
	public String suffix = null;

	/**
	 * Show the AntCO² algorithm GUI or work in batch mode?. If this number is 0 show the GUI, else
	 * run for this number of steps in batch mode.
	 */
	public int batch = -1;

	/**
	 * Threshold under which R1 must stay to stop the batch mode.
	 */
	public float batchR1 = -1f;

	/**
	 * Threshold above which R2 must stay to stop the batch mode.
	 */
	public float batchR2 = -1f;

	/**
	 * Number of steps of stability with batchR1 and batchR2 before leaving batch mode.
	 */
	public int batchStableSteps = -1;

	/**
	 * File name to use when dumping frames.
	 */
	public String frameName = null;

	/**
	 * AntCO² algorithm parameters.
	 */
	public AntParams antParams;

	/**
	 * File name of the graph to process.
	 */
	public String graphFileName;

	/**
	 * Speed of graph processing events compared to the speed of AntCO². If positive it is the
	 * number of graph steps to run for one AntCO² step. If negative it is the number of AntCO²
	 * steps to run for one graph step.
	 */
	public int graphSpeed = 1;

	/**
	 * Expect "step" external commands in the graph?.
	 */
	public boolean byStep = false;

// Constructors

	/**
	 * New set of parameters set to defaults.
	 * @param setToDefaults If true each parameter gets its default value.
	 */
	public BatchParams( boolean setToDefaults )
	{
		antParams = new AntParams( false );

		if( setToDefaults )
		{
			defaults();
		}
	}

// Access

	/**
	 * Create a parameter set from the command line arguments. All parameters not present on the
	 * command line are unset in the parameter set returned.
	 */
	public static BatchParams paramsFromArgs( String args[] )
	{
		BatchParams params = new BatchParams( false );

		params.defaults();

		int n = args.length;

		for( int i = 0; i < n; ++i )
		{
			// General parameters.

			if( args[i].equals( "-h" ) || args[i].equals( "--h" ) || args[i].equals( "-help" )
			        || args[i].equals( "--help" ) || args[i].equals( "help" )
			        || args[i].equals( "-?" ) || args[i].equals( "--?" ) || args[i].equals( "?" ) )
			{
				params.printUsage( System.err, "" );
			}
			else if( args[i].equals( "-rseed" ) )
			{
				i++;

				if( i >= args.length )
					params.printUsage( System.err, "-rseed requires one argument" );

				try
				{
					params.randomSeed = Long.parseLong( args[i] );
					params.antParams.randomSeed = params.randomSeed;
				}
				catch( NumberFormatException e )
				{
					params.printUsage( System.err, "bad numeric argument for -rseed: "
					        + e.getMessage() );
				}
			}
			else if( args[i].equals( "-suffix" ) )
			{
				i++;

				if( i >= args.length )
					params.printUsage( System.err, "-suffix requires an argument" );

				params.suffix = args[i];
			}
			else if( args[i].equals( "-frameDump" ) )
			{
				i++;

				if( i >= args.length )
					params.printUsage( System.err, "-frameDump takes one argument" );

				params.frameName = args[i];
			}
			else if( args[i].equals( "-graphSpeed" ) )
			{
				i++;

				if( i >= args.length )
					params.printUsage( System.err, "-graphSpeed takes one argument" );

				try
				{
					params.graphSpeed = Integer.parseInt( args[i] );
				}
				catch( NumberFormatException e )
				{
					params.printUsage( System.err, "bad numeric argument -graphSpeed: "
					        + e.getMessage() );
				}
			}
			else if( args[i].equals( "-byStep" ) )
			{
				params.byStep = true;
			}
			else if( args[i].equals( "-batch" ) )
			{
				if( i + 4 >= args.length )
					params.printUsage( System.err, "-batch requires four arguments" );

				try
				{
					params.batch = Integer.parseInt( args[++i] );
					params.batchR1 = Float.parseFloat( args[++i] );
					params.batchR2 = Float.parseFloat( args[++i] );
					params.batchStableSteps = Integer.parseInt( args[++i] );
				}
				catch( NumberFormatException e )
				{
					params
					        .printUsage( System.err, "bad numeric argument for -b: "
					                + e.getMessage() );
				}
			}

			// AntCO² parameters.

			else if( args[i].equals( "-colors" ) )
			{
				i++;

				if( i >= args.length )
					params.printUsage( System.err, "-colors requires an int" );

				try
				{
					params.antParams.colors = Integer.parseInt( args[i] );
				}
				catch( NumberFormatException e )
				{
					params.printUsage( System.err, "bad numeric argument for -colors: "
					        + e.getMessage() );
				}
			}
			else if( args[i].equals( "-ants" ) )
			{
				i++;

				if( i >= args.length )
					params.printUsage( System.err, "-ants requires an int" );

				try
				{
					params.antParams.antsPerVertex = Integer.parseInt( args[i] );
				}
				catch( NumberFormatException e )
				{
					params.printUsage( System.err, "bad numeric argument for -ants: "
					        + e.getMessage() );
				}
			}
			else if( args[i].equals( "-alpha" ) )
			{
				i++;

				if( i >= args.length )
					params.printUsage( System.err, "-alpha requires a double" );

				try
				{
					params.antParams.alpha = Float.parseFloat( args[i] );
				}
				catch( NumberFormatException e )
				{
					params.printUsage( System.err, "bad numeric argument for -alpha: "
					        + e.getMessage() );
				}
			}
			else if( args[i].equals( "-beta" ) )
			{
				i++;

				if( i >= args.length )
					params.printUsage( System.err, "-beta requires a double" );

				try
				{
					params.antParams.beta = Float.parseFloat( args[i] );
				}
				catch( NumberFormatException e )
				{
					params.printUsage( System.err, "bad numeric argument for -beta: "
					        + e.getMessage() );
				}
			}
			else if( args[i].equals( "-rho" ) )
			{
				i++;

				if( i >= args.length )
					params.printUsage( System.err, "-rho requires a double" );

				try
				{
					params.antParams.rho = Float.parseFloat( args[i] );
				}
				catch( NumberFormatException e )
				{
					params.printUsage( System.err, "bad numeric argument for -rho: "
					        + e.getMessage() );
				}
			}
			else if( args[i].equals( "-mem" ) )
			{
				i++;

				if( i >= args.length )
					params.printUsage( System.err, "-mem requires an int" );

				try
				{
					params.antParams.mem = Integer.parseInt( args[i] );
				}
				catch( NumberFormatException e )
				{
					params.printUsage( System.err, "bad numeric argument for -mem: "
					        + e.getMessage() );
				}
			}
			else if( args[i].equals( "-overpop" ) )
			{
				i++;

				if( i >= args.length )
					params.printUsage( System.err, "-overpop requires an int" );

				try
				{
					params.antParams.overPopulated = Integer.parseInt( args[i] );
				}
				catch( NumberFormatException e )
				{
					params.printUsage( System.err, "bad numeric argument for -overpop: "
					        + e.getMessage() );
				}
			}
			else if( args[i].equals( "-allColorsOverpop" ) )
			{
				params.antParams.perColorOverpop = false;
			}
			else if( args[i].equals( "-agoraphobia" ) )
			{
				i++;

				if( i >= args.length )
					params.printUsage( System.err, "-agoraphobia requires a float" );

				try
				{
					params.antParams.agoraphobia = Float.parseFloat( args[i] );
				}
				catch( NumberFormatException e )
				{
					params.printUsage( System.err, "bad numeric argument for -agoraphobia: "
					        + e.getMessage() );
				}
			}
			else if( args[i].equals( "-jump" ) )
			{
				i++;

				if( i >= args.length )
					params.printUsage( System.err, "-jump requires an int" );

				try
				{
					params.antParams.jump = Integer.parseInt( args[i] );
				}
				catch( NumberFormatException e )
				{
					params.printUsage( System.err, "bad numeric argument for -jump: "
					        + e.getMessage() );
				}
			}
			else if( args[i].equals( "-flytox" ) )
			{
				i++;

				if( i >= args.length )
					params.printUsage( System.err, "-flytox requires an int" );

				try
				{
					params.antParams.flytox = Integer.parseInt( args[i] );
				}
				catch( NumberFormatException e )
				{
					params.printUsage( System.err, "bad numeric argument for -flytox: "
					        + e.getMessage() );
				}
			}
			else if( args[i].equals( "-species" ) )
			{
				i++;

				if( i >= args.length )
					params.printUsage( System.err, "-species requires a string argument" );

				params.antParams.species = args[i];
			}
			else if( args[i].equals( "-metrics" ) )
			{
				i++;

				if( i >= args.length )
					params.printUsage( System.err, "-metrics requires an argument" );

				params.antParams.outputMetrics = args[i];
			}
			else if( args[i].equals( "-outputColorFileEvery" ) )
			{
				i++;

				if( i >= args.length )
					params.printUsage( System.err, "-outputColorFileEvery requires an int" );

				try
				{
					params.antParams.outputColorFileEvery = Integer.parseInt( args[i] );
				}
				catch( NumberFormatException e )
				{
					params.printUsage( System.err,
					        "bad numeric argument for -outputColorFileEvery: " + e.getMessage() );
				}
			}
			else if( args[i].equals( "-outputDGS" ) )
			{
				i++;

				if( i >= args.length )
					params.printUsage( System.err, "-outputDGS requires a filename" );

				params.antParams.outputDGS = args[i];
			}
			else
			{
				if( params.graphFileName == null )
					params.graphFileName = args[i];
				else
					params.printUsage( System.err,
					        "Cannot process two graph files (first given was '"
					                + params.graphFileName + "' second is '" + args[i] + "')" );
			}
		}

		return params;
	}

// Commands

	/**
	 * Reset all parameters to impossible values. Each parameter has a definition domain. This
	 * method set each parameter outside its domain to unset the parameter.
	 */
	public void clear()
	{
		antParams.clear();

		randomSeed = -1;
		suffix = null;
		frameName = null;
		batch = -1;
		batchR1 = -1f;
		batchR2 = -1f;
		batchStableSteps = -1;
	}

	/**
	 * Copy all parameters from other to this.
	 * @param other The parameters to copy.
	 */
	public void copy( BatchParams other )
	{
		if( other != this )
		{
			antParams.copy( other.antParams );

			randomSeed = other.randomSeed;
			suffix = other.suffix;
			frameName = other.frameName;
			batch = other.batch;
			batchR1 = other.batchR1;
			batchR2 = other.batchR2;
			batchStableSteps = other.batchStableSteps;
		}
	}

	/**
	 * Copy only the parameters from other that have a valid value to this.
	 * @param other The parameters to copy.
	 */
	public void copyIfSet( BatchParams other )
	{
		if( other != this )
		{
			antParams.copyIfSet( other.antParams );

			if( other.randomSeed >= 0 )
				randomSeed = other.randomSeed;
			if( other.suffix != null )
				suffix = other.suffix;
			if( other.frameName != null )
				frameName = other.frameName;
			if( other.batch >= 0 )
				batch = other.batch;
			if( other.batchR1 >= 0 )
				batchR1 = other.batchR1;
			if( other.batchR2 >= 0 )
				batchR2 = other.batchR2;
			if( other.batchStableSteps >= 0 )
				batchStableSteps = other.batchStableSteps;
		}
	}

	/**
	 * Set all parameters to their default values.
	 */
	public void defaults()
	{
		antParams.defaults();

		randomSeed = System.currentTimeMillis();
		frameName = "frame";
		suffix = "";
		batch = 0;
		batchR1 = -1f;
		batchR2 = -1f;
		batchStableSteps = -1;
	}

	/**
	 * Some parameters are tied, this method ensures they are coherent. Incoherent parameters are
	 * reset to appropriate values.
	 * @return A report of changes.
	 */
	public String ensureCoherence()
	{
		StringBuffer buffer = new StringBuffer();
		boolean error = false;
		String res;

		res = antParams.ensureCoherence();

		if( res != null )
		{
			buffer.append( res );
			error = true;
		}

		if( graphFileName == null )
		{
			buffer.append( "no graph file given to process, nothing to do!" );
			error = true;
		}

		if( error )
			return buffer.toString();

		return null;
	}

	/**
	 * Send a representation of this parameter set to the given stream.
	 * @param out The output stream.
	 */
	public void toStream( PrintStream out )
	{
		out.printf( "General parameters%n" );
		out.printf( "   Random seed ............... %d%n", randomSeed );
		out.printf( "   Frame dump prefix ......... %s%n", frameName );
		out.printf( "   File dump suffix .......... %s%n", suffix );

		out.printf( "%n" );
		antParams.toStream( out );
	}

	/**
	 * Print a command line reference on the given output stream.
	 * @param out The output stream.
	 * @param message An error message or null if none.
	 */
	public void printUsage( PrintStream out, String message )
	{
		out.printf( "%n%s:%n%n", message );
		out.printf( "Usage: antco2 [options] <file>%n" );
		out.printf( "%n" );
		out.printf( "  General options:%n" );
		out.printf( "   -h ........................................... This help screen.%n" );
		out
		        .printf( "   -rseed <seed:int> ............................ Fix the <seed> used by the random number generator.%n" );
		out
		        .printf( "   -suffix <suffix> ............................. <suffix> to add to all generated files (allow to differentiate%n" );
		out
		        .printf( "                                                  files from distinct runs).%n" );
		out
		        .printf( "   -frameDump <string> .......................... If dumping GUI frames, dump each step to a file starting with '<string>'.%n" );
		out
		        .printf( "   -graphSpeed <s:int> .......................... If <s> is positive: number of steps the graph makes for one AntCO² step.%n" );
		out
		        .printf( "                                                  If <s> is negative: number of steps AntCO² makes for one graph step.%n" );
		out
		        .printf( "   -byStep ...................................... Expect to find 'step' external commands in the graph.%n" );
		out
		        .printf( "   -batch <s:int> <r1:real> <r2:real> <t:int> ... Run in batch mode, without GUI. Batch mode stops after <s> steps or%n" );
		out
		        .printf( "                                                  when both r1 is less or equal to <r1> and r2 is greater or equal to%n" );
		out.printf( "                                                  <r2> durint <t> steps.%n" );
		out.printf( "%n" );
		out.printf( "  AntCO² algorithm options%n" );
		out
		        .printf( "   -colors <c:int> .............................. Number of colors/colonies/CR.%n" );
		out
		        .printf( "   -ants <a:int> ................................ Ant created/delete per vertex created/deleted.%n" );
		out
		        .printf( "                                                  This number must be a multiple of the number of colors.%n" );
		out.printf( "   -alpha <a:real> .............................. Pheromone exponent.%n" );
		out.printf( "   -beta <a:real> ............................... Edge weight exponent.%n" );
		out
		        .printf( "   -rho <r:real> [1:0] .......................... Pheromone preservation factor (evaporation is 1 - <rho>).%n" );
		out
		        .printf( "                                                  This number must be between 0 and 1.%n" );
		out.printf( "   -mem <m:int> ................................. Ant tabu list size.%n" );
		out
		        .printf( "   -overpop <op:int> ............................ The number <op> is compared to the number of ants <na> on each neighbour node%n" );
		out
		        .printf( "                                                  of the current ant node. Each neighbour node that have <na> greater than <op>%n" );
		out
		        .printf( "                                                  gets a penalisation factor.%n" );
		out
		        .printf( "   -allColorsOverpop ............................ Compute overpopulation taking into account all colors, not only the ant one.%n" );
		out
		        .printf( "   -agoraphobia <as:real> ....................... The number <as> is compared to the percentage of pheromone <pp> of the ant%n" );
		out
		        .printf( "                                                  color present on all edges incident to the vertex where the ant is located.%n" );
		out
		        .printf( "                                                  If <pp> is less than <as> the ant jumps.%n" );
		out
		        .printf( "   -jump <jp:int> ............................... When an ant takes the decision to jump, if <jp> is greater than 1, the ant%n" );
		out
		        .printf( "                                                  run away without any consideration of pheromone or weight along a path made%n" );
		out
		        .printf( "                                                  of <jp> nodes. If <jp> is 1, the ant jumps on a random node, but on the%n" );
		out
		        .printf( "                                                  processing resource where it currently is. If <jp> is 0 the ant never jumps.%n" );
		out
		        .printf( "   -flytox <st:int> ............................. Reset all pheromones every <st> steps.%n" );
		out
		        .printf( "   -species <string> ............................ Ant class to use ('antco2.ants.species.AgoraphoMyrmex' is a safe bet).%n" );
		out
		        .printf( "   -metrics <file> .............................. Output AntCO² metrics (R1, R2, etc.) to the given file.%n" );
		out
		        .printf( "   -ouputColorFileEvery <st:int> ................ Output a color file every <st> steps.%n" );
		out
		        .printf( "   -outputDGS <file> ............................ Output a DGS graph of the simulation (node/edges addition and removal, as well as color changes).%n" );
		out.printf( "%n" );

		System.exit( 1 );
	}
}