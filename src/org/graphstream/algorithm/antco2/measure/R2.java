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

import org.graphstream.algorithm.antco2.AntCo2Graph;
import org.graphstream.algorithm.antco2.Colony;
import org.graphstream.algorithm.antco2.Measure;
import org.graphstream.graph.Graph;

public class R2 implements Measure {
	double r2;

	Graph graph;

	public void init(Graph graph) {
		this.graph = graph;
	}

	public void compute() {
		if (graph instanceof AntCo2Graph) {
			AntCo2Graph ag = (AntCo2Graph) graph;

			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;

			for (Colony c : ag.getAntContext().eachColony()) {
				min = Math.min(min, c.getNodeCount());
				max = Math.max(max, c.getNodeCount());
			}

			r2 = (double) min / (double) max;
		} else
			throw new ClassCastException("graph is not a AntCo2Graph");
	}

	public double getValue() {
		return r2;
	}

}
