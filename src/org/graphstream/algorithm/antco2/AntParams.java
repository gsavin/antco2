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
public class AntParams {
	public static enum SmoothingBoxPolicy {
		IDENTITY, TIMED, RANDOM_TIMED, COHESION
	}

	/**
	 * Name of the ants colony class.
	 */
	@DefineParameter(name = "antco2.params.colonySpecies")
	protected String colonySpecies;

	/**
	 * Seed of the random instance.
	 */
	@DefineParameter(name = "antco2.params.randomSeed")
	protected long randomSeed;

	/**
	 * Pheromone persistence factor [0..1]. Evaporation is ( 1 - rho ).
	 */
	@DefineParameter(name = "antco2.params.rho", min = 0, max = 1)
	protected float rho = -1f;

	/**
	 * Threshold for the number of ants on a vertex above which the vertex is
	 * considered over populated [antsPerVertex,n[.
	 */
	@DefineParameter(name = "antco2.params.overPopulated")
	public int overPopulated = -1;

	/**
	 * Probe over population on a per-colour basis.
	 */
	@DefineParameter(name = "antco2.params.perColorOverpop")
	public boolean perColorOverpop = true;

	/**
	 * Output debugging messages?.
	 */
	@DefineParameter(name = "antco2.params.debug")
	public boolean debug = false;

	/**
	 * Exponent for edge pheromone values as perceived by ants [0,n[.
	 */
	@DefineParameter(name = "antco2.params.alpha", min = 0)
	public float alpha = -1f;

	/**
	 * Exponent for edge weights values as perceived by ants [0,n[.
	 */
	@DefineParameter(name = "antco2.params.beta", min = 0)
	public float beta = -1f;

	/**
	 * Minimal percentage of pheromone of the ant colour that must be present
	 * for the ant to keep a normal behaviour. Under this threshold, the ant
	 * becomes agoraphobic and jumps.
	 */
	@DefineParameter(name = "antco2.params.agoraphobia")
	public float agoraphobia = -1f;

	/**
	 * How to implement jumping (jumping occurs for various reasons, most
	 * notably, agoraphoby). If 0, the ant never jump, if 1, the ant jumps
	 * randomly on the current CR, if larger the ant flees without any pheromone
	 * or edge weight consideration of this number of vertices.
	 */
	@DefineParameter(name = "antco2.params.jump")
	public int jump = -1;

	/**
	 * Size of ants tabu list [0,n[.
	 */
	@DefineParameter(name = "antco2.params.mem", min = 0)
	public int mem = -1;

	/**
	 * Number of ants per vertex [multiple of colours]. This is the number of
	 * ants created when a vertex appears or deleted when a vertex disappears.
	 * This number may be modified by a CR power factor.
	 */
	@DefineParameter(name = "antco2.params.antsPerVertex", min = 0)
	public int antsPerVertex = -1;

	/**
	 * Number of ants to create per vertex for each colour.
	 */
	@DefineParameter(name = "antco2.params.antsPerVertexPerColor", min = 0)
	public int antsPerVertexPerColor = -1;

	/**
	 * Quantity of pheromone dropped by one ant at each edge traversal ]0,n[.
	 * Note that this number is not used actually since all ants consider
	 * pheromones using ratios, never direct quantitative values.
	 */
	@DefineParameter(name = "antco2.params.pheromoneDrop")
	public float pheromoneDrop = Float.NaN;

	@DefineParameter(name = "antco2.params.smoothingBoxPolicy")
	protected SmoothingBoxPolicy smoothingBoxPolicy = SmoothingBoxPolicy.IDENTITY;

	@DefineParameter(name = "antco2.params.globalFilePrefix")
	protected String globalFilePrefix = "";

	@DefineParameter(name = "antco2.params.measuresOutput")
	protected boolean measuresOutput = false;

	@DefineParameter(name = "antco2.params.computedMeasures")
	protected String computedMeasures = "";

	@DefineParameter(name = "antco2.params.outputMeasuresPath")
	protected String outputMeasuresPath = "%prefix%measures.dat";

	public float colorAttractionDecreaseFactor = 0.9f;

	public float colorAttractionFactor = 1.35f;

	// public float colorAttractionF

	public float colorAttractionThreshold = 0.75f;

	public AntParams() {
		defaults();
	}

	/**
	 * Set all parameters to their default values.
	 */
	public void defaults() {
		// randomSeed = System.currentTimeMillis();
		randomSeed = 7461358159815L;
		// colors = 4;
		antsPerVertex = 8;
		antsPerVertexPerColor = 8;
		rho = 0.86f;
		pheromoneDrop = 0.1f;
		alpha = 1.0f;
		beta = 3.0f;
		overPopulated = 16;
		mem = 3;
		agoraphobia = 0.2f;
		jump = 1;
		colonySpecies = org.graphstream.algorithm.antco2.species.ParsimoniamyrmexColony.class
				.getName();
		perColorOverpop = true;
		// powers = null;
		// tabu = 0.0001f;
		// flytox = 0;
		// outputMetrics = null;
		// ayatollahMode = false;
		// outputColorFileEvery = 0;
		// outputDGS = null;

		colorAttractionFactor = 1.25f;
		colorAttractionDecreaseFactor = 0.8f;
	}

	public void randomize() {
		randomSeed = System.currentTimeMillis();

		Random random = new Random(randomSeed);

		antsPerVertex = random.nextInt(8) + 2;
		antsPerVertexPerColor = random.nextInt(8) + 2;
		rho = 1 - random.nextFloat() * 0.25f;
		pheromoneDrop = random.nextFloat() * 0.25f + 0.01f;
		alpha = random.nextFloat() * 5;
		beta = random.nextFloat() * 5;
		overPopulated = random.nextInt(15) + 5;
		mem = random.nextInt(10);
		agoraphobia = random.nextFloat();
		jump = random.nextInt(5);
		perColorOverpop = random.nextBoolean();
	}

	public SmoothingBoxPolicy getSmoothingBoxPolicy() {
		return smoothingBoxPolicy;
	}

	public boolean isMeasuresOutput() {
		return measuresOutput;
	}

	public String getComputedMeasures() {
		return computedMeasures;
	}

	public String getOutputMeasuresPath() {
		return formatPath(outputMeasuresPath);
	}

	protected String formatPath(String path) {
		if (path.contains("%prefix%")) {
			path = path.replace("%prefix%", globalFilePrefix);
		}

		return path;
	}
}
