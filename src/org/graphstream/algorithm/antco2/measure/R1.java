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
package org.graphstream.algorithm.antco2.measure;

import org.graphstream.algorithm.antco2.AntCo2Edge;
import org.graphstream.algorithm.antco2.Measure;
import org.graphstream.graph.Graph;

/**
 * This measure defines the communication load of the graph.
 * 
 * @author Guilhelm Savin
 * 
 */
public class R1 implements Measure {
	/**
	 * The computed measure.
	 */
	double r1;

	/**
	 * The graph on which measure is computed.
	 */
	Graph graph;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init(Graph graph) {
		this.graph = graph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	public void compute() {
		double dA = 0;
		double dE = 0;

		for (AntCo2Edge e : graph.<AntCo2Edge> getEachEdge()) {
			if (e.isCutEdge())
				dA += e.getValue();

			dE += e.getValue();
		}

		r1 = dA / dE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.antco2.Measure#getValue()
	 */
	public double getValue() {
		return r1;
	}
}
