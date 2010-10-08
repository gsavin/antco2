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

//import java.util.Arrays;
//import java.util.Map;
import java.util.Random;

/**
 * Parameters of the antco2 algorithm.
 * 
 * @author adutot, gsavin
 *
 */
public class AntParams
{
	/*
	public static abstract class Parameter<T>
	{
		T value;
		T defaultValue;
		
		public Parameter( T defaultValue )
		{
			this.defaultValue = defaultValue;
			this.value = defaultValue;
		}
		
		public void setToDefault()
		{
			this.value = defaultValue;
		}
		
		public abstract void randomize( Random random );
	}
	
	public static class FloatParameter
		extends Parameter<Float>
	{
		Float min, max;
		
		public FloatParameter( float def, float min, float max )
		{
			super( def );
			
			this.min = Math.min(min,max);
			this.max = Math.max(min,max);
		}
		
		public void randomize( Random random )
		{
			value = random.nextFloat() * ( max - min ) + min;
		}
	}
	
	public static class IntegerParameter
		extends Parameter<Integer>
	{
		Integer min, max;
		
		public IntegerParameter( int def, int min, int max )
		{
			super( def );
			
			this.min = Math.min(min,max);
			this.max = Math.max(min,max);
		}
		
		public void randomize( Random random )
		{
			value = random.nextInt( max - min ) + min;
		}
	}
	
	public static class BooleanParameter
		extends Parameter<Boolean>
	{
		public BooleanParameter( boolean def )
		{
			super( def );
		}
		
		public void randomize( Random random )
		{
			value = random.nextBoolean();
		}
	}
	
	public static class ArrayParameter<T>
		extends Parameter<T>
	{
		T [] objects;
		
		public ArrayParameter( T def, T ... objects )
		{
			super( def );
			
			if( objects != null )
				this.objects = Arrays.copyOf( objects, objects.length );
		}
		
		public void randomize( Random random )
		{
			if( objects != null )
				value = objects [random.nextInt(objects.length)];
		}
	}
	
	protected Map<String,Parameter<?>> parameters;
	*/
	/**
	 * Name of the ants colony class.
	 */
	protected String colonySpecies;
	
	/**
	 * Seed of the random instance.
	 */
	protected long randomSeed;

	/**
	 * Pheromone persistence factor [0..1]. Evaporation is ( 1 - rho ).
	 */
	public float rho = -1f;

	/**
	 * Threshold for the number of ants on a vertex above which the vertex is considered
	 * over populated [antsPerVertex,n[.
	 */
	public int overPopulated = -1;

	/**
	 * Probe over population on a per-colour basis.
	 */
	public boolean perColorOverpop = true;

	/**
	 * Output debugging messages?.
	 */
	public boolean debug = false;

	/**
	 * Exponent for edge pheromone values as perceived by ants [0,n[.
	 */
	public float alpha = -1f;

	/**
	 * Exponent for edge weights values as perceived by ants [0,n[.
	 */
	public float beta = -1f;

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
	 * Size of ants tabu list [0,n[.
	 */
	public int mem = -1;

	/**
	 * Number of ants per vertex [multiple of colours]. This is the number of ants created when a
	 * vertex appears or deleted when a vertex disappears. This number may be modified by a CR power
	 * factor.
	 */
	public int antsPerVertex = -1;

	/**
	 * Number of ants to create per vertex for each colour.
	 */
	public int antsPerVertexPerColor = -1;

	/**
	 * Quantity of pheromone dropped by one ant at each edge traversal ]0,n[. Note that this number
	 * is not used actually since all ants consider pheromones using ratios, never direct
	 * quantitative values.
	 */
	public float pheromoneDrop = -1f;

	public float colorAttractionDecreaseFactor = 0.9f;
	
	public float colorAttractionFactor = 1.35f;
	
	//public float colorAttractionF
	
	public float colorAttractionThreshold = 0.75f;
	
	public AntParams()
	{
		defaults();
	}
	
	/**
	 * Set all parameters to their default values.
	 */
	public void defaults()
	{
		//randomSeed = System.currentTimeMillis();
		randomSeed = 7461358159815L;
		//colors = 4;
		antsPerVertex = 8;
		antsPerVertexPerColor = 8;
		//powers = null;
		//tabu = 0.0001f;
		rho = 0.86f;
		pheromoneDrop = 0.1f;
		alpha = 1.0f;
		beta = 3.0f;
		overPopulated = 16;
		mem = 3;
		agoraphobia = 0.2f;
		jump = 1;
		//flytox = 0;
		colonySpecies = org.graphstream.algorithm.antco2.species.FrankenmyrmexColony.class.getName();
		//outputMetrics = null;
		//ayatollahMode = false;
		perColorOverpop = true;
		//outputColorFileEvery = 0;
		//outputDGS = null;
		
		colorAttractionFactor = 1.25f;
		colorAttractionDecreaseFactor = 0.8f;
	}
	
	public void randomize()
	{
		randomSeed = System.currentTimeMillis();
		
		Random random = new Random( randomSeed );
		
		antsPerVertex = random.nextInt( 8 ) + 2;
		antsPerVertexPerColor = random.nextInt( 8 ) + 2;
		rho = 1 - random.nextFloat() * 0.25f;
		pheromoneDrop = random.nextFloat() * 0.25f + 0.01f;
		alpha = random.nextFloat() * 5;
		beta = random.nextFloat() * 5;
		overPopulated = random.nextInt( 15 ) + 5;
		mem = random.nextInt( 10 );
		agoraphobia = random.nextFloat();
		jump = random.nextInt( 5 );
		perColorOverpop = random.nextBoolean();
	}
}
