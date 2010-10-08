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
 * Defines the policy used to populate graph with ants.
 * @author adutot, gsavin
 *
 */
public interface PopulationPolicy
{
	/**
	 * Init the population policy.
	 * @param ctx ants context
	 */
	void init( AntContext ctx );
	
	/**
	 * Defines a step of the policy.
	 */
	void step();
	
	/**
	 * Get the ants count.
	 * @return ants count
	 */
	int getAntCount();
	
	/**
	 * Called when a node is added.
	 * @param node added node
	 */
	void nodeAdded( AntCo2Node node );
	
	/**
	 * Called when a node is removed.
	 * @param node removed node
	 */
	void nodeRemoved( AntCo2Node node );
	
	/**
	 * Called when a new colony is added.
	 * @param colony
	 */
	void colonyAdded( Colony colony );
	
	/**
	 * Called when a colony is removed.
	 * @param colony
	 */
	void colonyRemoved( Colony colony );
}
