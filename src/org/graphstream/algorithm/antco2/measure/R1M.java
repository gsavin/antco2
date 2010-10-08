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

import static java.lang.Math.max;

public class R1M
	extends R1
{
	double m;
	double weight = 3;
	
	public void compute()
	{
		super.compute();
		
		if( graph instanceof AntCo2Graph )
		{
			AntCo2Graph ag = (AntCo2Graph) graph;
			
			double s = 0;
			
			for( Colony c : ag.getAntContext().eachColony() )
				s = max( s, (double) c.getMigrationCountForThisStep() / (double) c.getNodeCountAtStepBeginning() );
			
			m = s;
		}
		else throw new ClassCastException("graph is not a AntCo2Graph");
	}
	
	public double getValue()
	{
		return Math.pow( r1, Math.pow(1-m,weight) );
	}
}
