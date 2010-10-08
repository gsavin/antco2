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
import java.nio.IntBuffer;
import java.nio.FloatBuffer;

/**
 * Parameter set for AntCO².
 * 
 * @author Antoine Dutot
 * @since 20050419
 */
public class AntParams
{
// Attributes

	/**
	 * Random seed.
	 */
	public long randomSeed = -1;

	/**
	 * Number of colours/CR/colonies [1,n[.
	 */
	public int colors = -1;

	/**
	 * Number of ants per vertex [multiple of colours]. This is the number of ants created when a
	 * vertex appears or deleted when a vertex disappears. This number may be modified by a CR power
	 * factor.
	 */
	public int antsPerVertex = -1;

	/**
	 * Number of ants to create per vertex for each colour.
	 */
	public IntBuffer antsPerVertexPerColor = null;

	/**
	 * Processor powers.
	 */
	public FloatBuffer powers = null;

	/**
	 * Tabu factor applied to all edges that are in an ant tabu list [0,n[. This is also used to
	 * implement over population.
	 */
	public float tabu = -1f;

	/**
	 * Pheromone persistence factor [0..1]. Evaporation is ( 1 - rho ).
	 */
	public float rho = -1f;

	/**
	 * Quantity of pheromone dropped by one ant at each edge traversal ]0,n[. Note that this number
	 * is not used actually since all ants consider pheromones using ratios, never direct
	 * quantitative values.
	 */
	public float pheromoneDrop = -1f;

	/**
	 * Exponent for edge pheromone values as perceived by ants [0,n[.
	 */
	public float alpha = -1f;

	/**
	 * Exponent for edge weights values as perceived by ants [0,n[.
	 */
	public float beta = -1f;

	/**
	 * Threshold for the number of ants on a vertex above which the vertex is considered
	 * over populated [antsPerVertex,n[.
	 */
	public int overPopulated = -1;

	/**
	 * Size of ants tabu list [0,n[.
	 */
	public int mem = -1;

	/**
	 * Minimal percentage of pheromone of the ant colour that must be present for the ant to keep a
	 * normal behaviour. Under this threshold, the ant becomes agoraphobic and jumps.
	 */
	public float agoraphobia = -1f;

	/**
	 * How to implement jumping (jumping occurs for various reasons, most notably, agoraphoby). If
	 * 0, the ant never jump, if 1, the ant jumps randomly on the current CR, if larger the ant
	 * flees without any pheromone or edge weight consideration of this number of vertices.
	 */
	public int jump = -1;

	/**
	 * Reset all pheromones to zero every flytox steps [0,n[. This is an experimental setting, as
	 * its name suggests you should not use it. The value 0 means it is disabled.
	 */
	public int flytox = -1;

	/**
	 * Ant species. This ant class loading facility will be used for several experiments with
	 * genetically generated ants.
	 */
	public String species = null;

	/**
	 * Output debugging messages?.
	 */
	public boolean debug = false;

	/**
	 * Output metrics in a file?. Name of the file where metrics are output. Null means do not
	 * output metrics.
	 */
	public String outputMetrics = null;

	/**
	 * Probe over population on a per-colour basis.
	 */
	public boolean perColorOverpop = true;

	/**
	 * Mode that verify strictly each event, doubles nodes throw an exception, edges that connect
	 * non declared mode also, etc.
	 */
	public boolean ayatollahMode = false;

	/**
	 * Output a colour file every n steps.
	 */
	public int outputColorFileEvery = 0;

	/**
	 * Output a dynamic graph during during the whole simulation. The graph is started when AntCO²
	 * is first created, and each new AntCO² step will produce new graph events (not a whole new
	 * graph), like changes in the attributes of edges, for example.
	 */
	public String outputDGS = null;

// Constructors

	/**
	 * New AntCO² algorithm parameters set to defaults.
	 * @param setToDefaults If true each parameter gets its default value.
	 */
	public AntParams( boolean setToDefaults )
	{
		if( setToDefaults )
			defaults();
	}

// Access

// Commands

	/**
	 * Reset all parameters to impossible values. Each parameter has a definition domain. This
	 * method set each parameter outside its domain to unset the parameter.
	 */
	public void clear()
	{
		randomSeed = -1;
		colors = -1;
		antsPerVertex = -1;
		antsPerVertexPerColor = null;
		powers = null;
		tabu = -1f;
		rho = -1f;
		pheromoneDrop = -1f;
		alpha = -1f;
		beta = -1f;
		overPopulated = -1;
		mem = -1;
		agoraphobia = -1f;
		jump = -1;
		flytox = -1;
		species = null;
		outputMetrics = null;
		ayatollahMode = false;
		perColorOverpop = true;
		outputColorFileEvery = -1;
		outputDGS = null;
	}

	/**
	 * Set all parameters to their default values.
	 */
	public void defaults()
	{
		randomSeed = System.currentTimeMillis();
		colors = 4;
		antsPerVertex = 8;
		antsPerVertexPerColor = null;
		powers = null;
		tabu = 0.0001f;
		rho = 0.86f;
		pheromoneDrop = 0.1f;
		alpha = 1.0f;
		beta = 3.0f;
		overPopulated = 16;
		mem = 3;
		agoraphobia = 0.2f;
		jump = 1;
		flytox = 0;
		species = org.graphstream.algorithm.antco2.species.AgoraphomyrmexColony.class.getName();
		outputMetrics = null;
		ayatollahMode = false;
		perColorOverpop = true;
		outputColorFileEvery = 0;
		outputDGS = null;
	}

	/**
	 * Copy all parameters from other to this.
	 * @param other The parameters to copy.
	 */
	public void copy( AntParams other )
	{
		if( other != this )
		{
			randomSeed = other.randomSeed;
			colors = other.colors;
			antsPerVertex = other.antsPerVertex;
			tabu = other.tabu;
			rho = other.rho;
			pheromoneDrop = other.pheromoneDrop;
			alpha = other.alpha;
			beta = other.beta;
			overPopulated = other.overPopulated;
			mem = other.mem;
			agoraphobia = other.agoraphobia;
			jump = other.jump;
			flytox = other.flytox;
			species = other.species;
			outputMetrics = other.outputMetrics;
			ayatollahMode = other.ayatollahMode;
			perColorOverpop = other.perColorOverpop;
			outputColorFileEvery = other.outputColorFileEvery;
			outputDGS = other.outputDGS;

			if( other.antsPerVertexPerColor != null )
			{
				antsPerVertexPerColor = IntBuffer.allocate( other.antsPerVertexPerColor.capacity() );
				antsPerVertexPerColor.put( other.antsPerVertexPerColor );
			}
			else
			{
				antsPerVertexPerColor = null;
			}

			if( other.powers != null )
			{
				powers = FloatBuffer.allocate( other.powers.capacity() );
				powers.put( other.powers );
			}
			else
			{
				powers = null;
			}
		}
	}

	/**
	 * Copy only the parameters from other that have a valid value to this.
	 * @param other The parameters to copy.
	 */
	public void copyIfSet( AntParams other )
	{
		if( other != this )
		{
			if( other.colors >= 0 )
				colors = other.colors;
			if( other.antsPerVertex >= 0 )
				antsPerVertex = other.antsPerVertex;
			if( other.tabu >= 0 )
				tabu = other.tabu;
			if( other.rho >= 0f )
				rho = other.rho;
			if( other.pheromoneDrop >= 0f )
				pheromoneDrop = other.pheromoneDrop;
			if( other.alpha >= 0f )
				alpha = other.alpha;
			if( other.beta >= 0f )
				beta = other.beta;
			if( other.overPopulated >= 0 )
				overPopulated = other.overPopulated;
			if( other.mem >= 0 )
				mem = other.mem;
			if( other.agoraphobia >= 0 )
				agoraphobia = other.agoraphobia;
			if( other.jump >= 0 )
				jump = other.jump;
			if( other.flytox >= 0 )
				flytox = other.flytox;
			if( other.species != null )
				species = other.species;
			if( other.randomSeed >= 0 )
				randomSeed = other.randomSeed;
			if( other.outputMetrics != null )
				outputMetrics = other.outputMetrics;
			if( other.outputColorFileEvery >= 0 )
				outputColorFileEvery = other.outputColorFileEvery;
			if( other.outputDGS != null )
				outputDGS = other.outputDGS;
			
			if( other.antsPerVertexPerColor != null )
			{
				antsPerVertexPerColor = IntBuffer.allocate( other.antsPerVertexPerColor.capacity() );
				antsPerVertexPerColor.put( other.antsPerVertexPerColor );
			}
			
			if( other.powers != null )
			{
				powers = FloatBuffer.allocate( other.powers.capacity() );
				powers.put( other.powers );
			}

			ayatollahMode = other.ayatollahMode;
			perColorOverpop = other.perColorOverpop;
		}
	}

	/**
	 * Some parameters are tied, this method ensures they are coherent. Incoherent parameters are
	 * reset to appropriate values.
	 * @return A report of changes.
	 */
	public String ensureCoherence()
	{
		StringBuffer errors = new StringBuffer();
		boolean error = false;

		int na = ( antsPerVertex / colors ) * colors;

		if( antsPerVertex < colors )
		{
			errors.append( String.format( "- The number of ants per vertex is less than the number of colors.%n" ) );
			// errors.append( " The number of ants per vertex is reset to " );
			// errors.append( colors );
			// errors.append( ".\n" );

			// antsPerVertex = colors;
			error = true;
		}
		else if( na < antsPerVertex )
		{
			if( na <= 0 )
				na = colors;

			errors.append( String.format( "- The number of ants per vertex is not a multiple of the number of colors.%n" ) );
			errors.append( String.format( "  The number of ants per vertex is reset to %d.%n", na ) );

			antsPerVertex = na;
			error = true;
		}

		if( overPopulated >= 0 && overPopulated <= antsPerVertex )
		{
			errors.append( String.format( "- The overpopulation thresold %d is less or equal to the number of ants per vertex %d.%n",
			                overPopulated, antsPerVertex ) );
			errors.append( String.format( "  This is proably bad, usually the overpopulation thresold must be set to 1.5 or%n" ) );
			errors.append( String.format( "  2 time the number of ants per vertex.%n" ) );
			error = true;
		}

		if( error )
			return errors.toString();

		if( antsPerVertexPerColor != null )
		{
			if( antsPerVertexPerColor.capacity() < colors )
			{
				errors.append( String.format( "- The list of powers for each processor contains fewer elements than the number of processors%n" ) );
				error = true;
			}
			else if( antsPerVertexPerColor.capacity() > colors )
			{
				errors.append( String.format( "- The list of powers for each processor contains more elements than the number of processors.%n" ) );
				error = true;
			}
		}

		return null;
	}

	/**
	 * Send a representation of this parameter set to the given stream.
	 * @param out The output stream.
	 */
	public void toStream( PrintStream out )
	{
		out.printf( "AntCO² parameters%n" );
		out.printf( "   Random seed ............... %d%n", randomSeed );
		out.printf( "   Colors .................... %d%n", colors );
		out.printf( "   Ants per vertex ........... %d%n", antsPerVertex );

		if( antsPerVertexPerColor != null )
		{
			out.printf( "   Ants per vertex ........... [" );
			for( int i = 0; i < antsPerVertexPerColor.capacity(); ++i )
			{
				out.printf( " %d", antsPerVertexPerColor.get( i ) );
			}
			out.printf( " ]%n" );
		}

		if( antsPerVertexPerColor != null )
		{
			out.printf( "   Processor powers .......... [" );
			for( int i = 0; i < powers.capacity(); ++i )
			{
				out.printf( " %f", powers.get( i ) );
			}
			out.printf( " ]%n" );
		}

		out.printf( "   Alpha ..................... %f%n", alpha );
		out.printf( "   Beta ...................... %f%n", beta );
		out.printf( "   Rho ....................... %f%n", rho );
		if( mem == 0 )
			out.printf( "   Tabu list size ............ none%n" );
		else if( mem == 1 )
			out.printf( "   Tabu list size ............ 1 vertex%n" );
		else
			out.printf( "   Tabu list size ............ %d vertices%n", mem );
		out.printf( "   Tabu factor ............... %f%n", tabu  );
		out.printf( "   Overpopulation thresold ... over %d%n", overPopulated );
		out.printf( "   Per color overpopulation .. %s%n", perColorOverpop ? "true" : "false" );
		out.printf( "   Agoraphobia thresold ...... under %f%n", agoraphobia );
		if( jump == 0 )
			out.printf( "   Jump ...................... no jumps%n" );
		else if( jump == 1 )
			out.printf( "   Jump ...................... random (on current computing resource)%n" );
		else
			out.printf( "   Jump ...................... %d (flee radius)%n", jump );
		if( flytox == 0 )
			out.printf( "   Flytox .................... no%n" );
		else
			out.printf( "   Flytox .................... every %d steps%n", flytox );
		out.printf( "   Species ................... %s%n", species );
		out.printf( "   Per color over-pop ........ %s%n", perColorOverpop ? "on" : "off" );
		out.printf( "   Ayatollah mode ............ %s%n", ayatollahMode ? "on" : "off" );
		out.printf( "   Debug ..................... %s%n", debug ? "on" : "off" );
		out.printf( "   Output metrics ............ %s%n", outputMetrics == null ? "no"
		        : "to file '" + outputMetrics + "'" );
		out.printf( "   Output a color file each .. %d step%n", outputColorFileEvery );
		out.printf( "   Output a dynamic graph .... %s%n", outputDGS == null ? "no" : "to file '"
		        + outputDGS + "'" );
		out.printf( "%n" );
	}
	
// Access / Modification

	public long getRandomSeed()
    {
    	return randomSeed;
    }

	public void setRandomSeed( long randomSeed )
    {
    	this.randomSeed = randomSeed;
    }

	public int getColors()
    {
    	return colors;
    }

	public void setColors( int colors )
    {
    	this.colors = colors;
    }

	public int getAntsPerVertex()
    {
    	return antsPerVertex;
    }

	public void setAntsPerVertex( int antsPerVertex )
    {
    	this.antsPerVertex = antsPerVertex;
    }

	public IntBuffer getAntsPerVertexPerColor()
    {
    	return antsPerVertexPerColor;
    }

	public void setAntsPerVertexPerColor( IntBuffer antsPerVertexPerColor )
    {
    	this.antsPerVertexPerColor = antsPerVertexPerColor;
    }

	public FloatBuffer getPowers()
    {
    	return powers;
    }

	public void setPowers( FloatBuffer powers )
    {
    	this.powers = powers;
    }

	public float getTabu()
    {
    	return tabu;
    }

	public void setTabu( float tabu )
    {
    	this.tabu = tabu;
    }

	public float getRho()
    {
    	return rho;
    }

	public void setRho( float rho )
    {
    	this.rho = rho;
    }

	public float getPheromoneDrop()
    {
    	return pheromoneDrop;
    }

	public void setPheromoneDrop( float pheromoneDrop )
    {
    	this.pheromoneDrop = pheromoneDrop;
    }

	public float getAlpha()
    {
    	return alpha;
    }

	public void setAlpha( float alpha )
    {
    	this.alpha = alpha;
    }

	public float getBeta()
    {
    	return beta;
    }

	public void setBeta( float beta )
    {
    	this.beta = beta;
    }

	public int getOverPopulated()
    {
    	return overPopulated;
    }

	public void setOverPopulated( int overPopulated )
    {
    	this.overPopulated = overPopulated;
    }

	public int getMem()
    {
    	return mem;
    }

	public void setMem( int mem )
    {
    	this.mem = mem;
    }

	public float getAgoraphobia()
    {
    	return agoraphobia;
    }

	public void setAgoraphobia( float agoraphobia )
    {
    	this.agoraphobia = agoraphobia;
    }

	public int getJump()
    {
    	return jump;
    }

	public void setJump( int jump )
    {
    	this.jump = jump;
    }

	public int getFlytox()
    {
    	return flytox;
    }

	public void setFlytox( int flytox )
    {
    	this.flytox = flytox;
    }

	public String getSpecies()
    {
    	return species;
    }

	public void setSpecies( String species )
    {
    	this.species = species;
    }

	public boolean isDebug()
    {
    	return debug;
    }

	public void setDebug( boolean debug )
    {
    	this.debug = debug;
    }

	public String getOutputMetrics()
    {
    	return outputMetrics;
    }

	public void setOutputMetrics( String outputMetrics )
    {
    	this.outputMetrics = outputMetrics;
    }

	public boolean isPerColorOverpop()
    {
    	return perColorOverpop;
    }

	public void setPerColorOverpop( boolean perColorOverpop )
    {
    	this.perColorOverpop = perColorOverpop;
    }

	public boolean isAyatollahMode()
    {
    	return ayatollahMode;
    }

	public void setAyatollahMode( boolean ayatollahMode )
    {
    	this.ayatollahMode = ayatollahMode;
    }

	public int getOutputColorFileEvery()
    {
    	return outputColorFileEvery;
    }

	public void setOutputColorFileEvery( int outputColorFileEvery )
    {
    	this.outputColorFileEvery = outputColorFileEvery;
    }

	public String getOutputDGS()
    {
    	return outputDGS;
    }

	public void setOutputDGS( String outputDGS )
    {
    	this.outputDGS = outputDGS;
    }
}