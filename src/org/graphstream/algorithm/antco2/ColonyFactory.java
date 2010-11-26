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
 * Factory used to create colony.
 * 
 * @author adutot, gsavin
 * 
 */
public class ColonyFactory {
	/**
	 * Create a new colony. Class of the colony is given in parameters of the
	 * context.
	 * 
	 * @param actx
	 *            ants context
	 * @return a new colony
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ExceptionInInitializerError
	 * @throws SecurityException
	 */
	public static Colony newColony(AntContext actx)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, ExceptionInInitializerError,
			SecurityException {
		Class<?> clazz = Class.forName(actx.getAntParams().colonySpecies);

		Object o = clazz.newInstance();

		if (!(o instanceof Colony))
			throw new InstantiationException("The ant species given '"
					+ actx.getAntParams().colonySpecies
					+ "' is not an instance of antco2.Colony");

		return (Colony) o;
	}
}
