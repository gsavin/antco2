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

import java.util.HashMap;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.graphstream.algorithm.antco2.AntCo2Node;
import org.graphstream.algorithm.antco2.AntContext;
import org.graphstream.algorithm.antco2.Colony;
import org.graphstream.algorithm.antco2.SmoothingBox;
import org.graphstream.stream.ElementSink;

public class TimedBox implements SmoothingBox, ElementSink {
	protected static long getCurrentDate() {
		return System.currentTimeMillis();
	}

	protected static final TimeUnit DEFAULT_UNIT = TimeUnit.MILLISECONDS;

	protected static class Delay implements Delayed {
		TimeUnit unit;
		long date;

		public Delay() {
			this.date = getCurrentDate();
			this.unit = DEFAULT_UNIT;
		}

		public int compareTo(Delayed o) {
			return (int) Math.signum(o.getDelay(unit) - getDelay(unit));
		}

		public long getDelay(TimeUnit unit) {
			return unit.convert(getCurrentDate(), DEFAULT_UNIT)
					- unit.convert(date, this.unit);
		}

		public void reset() {
			this.date = getCurrentDate();
			this.unit = DEFAULT_UNIT;
		}

		public String toString(TimeUnit unit) {
			return String.format("%s %s", Long.toString(getDelay(unit)),
					unit.toString());
		}

		public String toString() {
			return toString(unit);
		}
	}

	HashMap<String, Delay> delays;
	long delay;
	TimeUnit unit;
	AntContext ctx;

	public TimedBox(long delay, TimeUnit unit) {
		this.delay = delay;
		this.unit = unit;
	}

	public void init(AntContext ctx) {
		this.ctx = ctx;

		if (delays != null)
			delays.clear();
		else
			delays = new HashMap<String, Delay>();

		ctx.getInternalGraph().addElementSink(this);
	}

	public void submitColor(AntCo2Node node, Colony oldColor, Colony newColor) {
		Delay delay = delays.get(node.getId());

		if (delay == null) {
			delay = new Delay();
			delays.put(node.getId(), delay);
			node.setColor(newColor);
		} else {
			if (delay.getDelay(this.unit) > this.delay) {
				node.setColor(newColor);
				delay.reset();
			}
		}
	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		delays.remove(nodeId);
	}

	public void graphCleared(String sourceId, long timeId) {
		delays.clear();
	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {

	}

	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {

	}

	public void edgeRemoved(String sourceId, long timeId, String edgeId) {

	}

	public void stepBegins(String sourceId, long timeId, double step) {

	}
}
