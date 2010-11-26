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
package org.graphstream.algorithm.antco2.smoothingBox;

import java.util.concurrent.TimeUnit;

import org.graphstream.algorithm.antco2.AntCo2Node;
import org.graphstream.algorithm.antco2.Colony;

public class RandomTimedBox extends TimedBox {
	double randomize;

	public RandomTimedBox(long delay, TimeUnit unit, double randomize) {
		super(delay, unit);
		this.randomize = randomize;
	}

	public void submitColor(AntCo2Node node, Colony oldColor, Colony newColor) {
		Delay delay = delays.get(node.getId());

		if (delay == null) {
			delay = new Delay();
			delays.put(node.getId(), delay);
			node.setColor(newColor);
		} else {
			double delta = randomize * this.delay;

			if (delay.getDelay(this.unit) > this.delay
					+ (delta * ctx.random().nextDouble() - delta / 2.0)) {
				node.setColor(newColor);
				delay.reset();
			}
		}
	}
}
