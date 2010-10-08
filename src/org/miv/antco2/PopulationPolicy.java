/*
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */

package org.miv.antco2;

import org.miv.graphstream.graph.*;

/**
 * Policy for population management.
 * 
 * <p>
 * A population policy tells when adding and removing ants. It gives indications on the global
 * number of ants, the number of ants per colour, etc.
 * </p>
 * 
 * <p>
 * The policy acts as a listener for an {@link AntCo2} instance that will send it events. In return,
 * the policy creates or deletes ants.
 * </p>
 * 
 * @author Antoine Dutot
 * @since 29 juil. 2005
 */
public abstract class PopulationPolicy
{
// Attributes

	/**
	 * AntCO² instance to manage.
	 */
	protected AntCo2 antco2;

	// Constructors

	/**
	 * New population policy for the given AntCO² instance. The policy is automatically registered
	 * in the given AntCO² instance.
	 */
	public PopulationPolicy()
	{
	}

// Access

	/**
	 * Managed AntCO² instance.
	 * @return An {@link AntCo2} instance.
	 */
	public AntCo2 getAntCo2()
	{
		return antco2;
	}

// Commands

	/**
	 * Manage the population of the given AntCO² instance. This method is called automatically by
	 * the {@link AntCo2} instance which is given a population policy.
	 * @param antco2 The AntCO² instance to manage.
	 */
	protected void setAntCo2( AntCo2 antco2 )
	{
		this.antco2 = antco2;
	}

	// Commands Event interface.

	/**
	 * A node has been added.
	 * @param node The node.
	 * @param infos Node informations.
	 */
	public abstract void nodeAdded( Node node, NodeInfos infos );

	/**
	 * A node is about to be removed.
	 * @param node The node.
	 * @param infos Node informations.
	 */
	public abstract void nodeRemoved( Node node, NodeInfos infos );

	/**
	 * A colony has been added.
	 * @param color The colony.
	 */
	public abstract void colorAdded( Colony color );

	/**
	 * A colony is about to be removed.
	 * @param color The colony.
	 */
	public abstract void colorRemoved( Colony color );

	/**
	 * A AntCO² step finished.
	 * @param time The finished time step.
	 * @param metrics Various statistics and results.
	 */
	public abstract void stepFinished( int time, Metrics metrics );
}