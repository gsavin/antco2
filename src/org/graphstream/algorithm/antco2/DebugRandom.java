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

import java.io.PrintStream;
import java.util.Random;

public class DebugRandom extends Random {
	/**
	 * 
	 */
	private static final long serialVersionUID = -633739369794868587L;

	public DebugRandom(long seed) {
		super(seed);
	}

	public int nextInt() {
		int n = super.nextInt();
		log(String.format("[nextInt()] %d", n));
		return n;
	}

	public int nextInt(int max) {
		int n = super.nextInt(max);
		log(String.format("[nextInt(%d)] %d", max, n));
		return n;
	}

	public long nextLong() {
		long n = super.nextLong();
		log(String.format("[nextLong()] %d", n));
		return n;
	}

	public float nextFloat() {
		float n = super.nextFloat();
		log(String.format("[nextFloat()] %f", n));
		return n;
	}

	public double nextDouble() {
		double n = super.nextDouble();
		log(String.format("[nextDouble()] %f", n));
		return n;
	}

	public boolean nextBoolean() {
		boolean n = super.nextBoolean();
		log(String.format("[nextBoolean()] %s", n));
		return n;
	}

	public double nextGaussian() {
		double n = super.nextGaussian();
		log(String.format("[nextGaussian()] %f", n));
		return n;
	}

	public void nextBytes(byte[] bytes) {
		super.nextBytes(bytes);
		log(String.format("[nextBytes()]"));
		;
	}

	public void setSeed(long seed) {
		super.setSeed(seed);
		log(String.format("[setSeed()] %d", seed));
	}

	protected void log(String msg) {
		Exception e = new Exception();
		StackTraceElement[] stack = e.getStackTrace();

		String callInfo = "";

		for (int i = stack.length - 3; i > 1; i--) {
			String classname = stack[i].getClassName();

			if (classname.indexOf('.') >= 0)
				classname = classname.substring(classname.lastIndexOf('.') + 1);

			callInfo = String.format("%s%s:%s:%d > ", callInfo, classname,
					stack[i].getMethodName(), stack[i].getLineNumber());
		}

		Debug.log(String.format("[random] %s%s%n", callInfo, msg));
	}
}
