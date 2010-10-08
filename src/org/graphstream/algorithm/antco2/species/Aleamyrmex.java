/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.graphstream.algorithm.antco2.species;

import org.graphstream.algorithm.antco2.Ant;
import org.graphstream.algorithm.antco2.AntCo2Edge;
import org.graphstream.algorithm.antco2.AntCo2Node;
import org.graphstream.algorithm.antco2.AntContext;
import org.graphstream.algorithm.antco2.Colony;

/**
 * Aleamyrmex.
 *
 * @author Antoine Dutot
 * @since 29 juil. 2005
 */
public class Aleamyrmex
	extends Ant
{
// Constructors

	public Aleamyrmex( String id, Colony colony,
			AntCo2Node startNode, AntContext context )
	{
		super( id, colony, startNode, context );
	}

// Accessors

	@Override
	public float getPheromonDrop()
	{
		return 0.01f;
	}
	
// Commands
	
	@Override
	public void step()
	{
		randomStep();
	}
	
	/**
	 * Choose the next node randomly.
	 */
	protected void randomStep()
	{
		int n = curNode.getDegree();
		int r = 0;
		
		if( n > 0 )
		{
			r = ctx.random().nextInt( n );
		
			AntCo2Edge curEdge = (AntCo2Edge) curNode.getEdge(r);
		
			assert curEdge != null : "found no edge";
			
			cross( curEdge, true );
		}
	}
}
