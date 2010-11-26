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

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Locale;

import org.graphstream.algorithm.antco2.measure.Data;
import org.graphstream.algorithm.antco2.measure.R1;
import org.graphstream.algorithm.antco2.measure.R2;
import org.graphstream.algorithm.antco2.measure.R3;

public class Measures {
	public static enum KnownMeasure {
		R1, R2, R3, DATA
	}

	LinkedList<Measure> measures;

	PrintStream out;

	int step;

	public Measures() {
		measures = new LinkedList<Measure>();
		measures.add(new R1());
		measures.add(new R2());
		measures.add(new R3());
		measures.add(new Data());
	}

	public void init(AntContext ctx) {
		measures.clear();

		String measuresComputed = ctx.getAntParams().getComputedMeasures()
				.trim();

		if (measuresComputed.length() > 0) {
			String[] computedMeasures = measuresComputed.split(",");

			if (computedMeasures != null) {
				for (String m : computedMeasures) {
					m = m.trim();
					KnownMeasure km = KnownMeasure.valueOf(m);

					switch (km) {
					case R1:
						measures.add(new R1());
						break;
					case R2:
						measures.add(new R2());
						break;
					case R3:
						measures.add(new R3());
						break;
					case DATA:
						measures.add(new Data());
						break;
					}
				}
			}
		}

		for (Measure m : measures)
			m.init(ctx.internalGraph);

		if (out != null)
			out.close();

		out = null;

		if (ctx.getAntParams().isMeasuresOutput()) {
			try {
				out = new PrintStream(ctx.getAntParams()
						.getOutputMeasuresPath());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public void step() {
		for (Measure m : measures)
			m.compute();

		if (out != null)
			output();

		step++;
	}

	public void output() {
		out.printf("%d", step);

		for (Measure m : measures)
			out.printf(Locale.ROOT, "\t%f", m.getValue());

		out.printf("%n");
		out.flush();
	}
}
