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

/**
 * Graph parameters.
 * 
 * @author Antoine Dutot
 */
public abstract class GraphInfos
{
// Attributes

	/**
	 * Numerical value.
	 */
	protected float value;

// Constructors

	public GraphInfos( float value )
	{
		this.value = value;
	}

// Access

	/**
	 * Numerical value.
	 * @return Returns the value.
	 */
	public float getValue()
	{
		return value;
	}

	// Commands

	/**
	 * Change the numerical value.
	 * @param value The value to set.
	 */
	public void setValue( float value )
	{
		this.value = value;
	}

	/**
	 * Commit all changes since the previous call to this method. Commit should be called only at
	 * the end of each AntCO² step.
	 */
	public abstract void commit();
}