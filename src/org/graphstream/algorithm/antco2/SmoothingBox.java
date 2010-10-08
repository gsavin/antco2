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

/**
 * Add a mechanism between AntCo2 and the graph to smooth the result of
 * the algorithm, avoiding oscillation of entities from one organization
 * to another.
 * 
 * @author Guilhelm Savin
 *
 */
public interface SmoothingBox
{
	public static enum BoxType
	{
		IDENTITY_BOX,
		TIMED_BOX
	}
	
	/**
	 * Initialization of the box.
	 * 
	 * @param algo
	 */
	void init( AntContext ctx );

	/**
	 * Submit a color change for a node.
	 * 
	 * @param node
	 *            the node which trying to change its color
	 * @param oldColor
	 *            old color of the node
	 * @param newColor
	 *            new color of the node
	 */
	void submitColor( AntCo2Node node, Colony oldColor, Colony newColor );
}
