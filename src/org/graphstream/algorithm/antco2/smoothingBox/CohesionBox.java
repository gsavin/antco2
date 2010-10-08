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

import java.util.Arrays;
import java.util.HashMap;

import org.graphstream.algorithm.antco2.AntCo2Edge;
import org.graphstream.algorithm.antco2.AntCo2Listener;
import org.graphstream.algorithm.antco2.AntCo2Node;
import org.graphstream.algorithm.antco2.AntContext;
import org.graphstream.algorithm.antco2.Colony;
import org.graphstream.algorithm.antco2.SmoothingBox;
import org.graphstream.stream.ElementSink;

import static java.lang.Math.pow;

public class CohesionBox
	implements SmoothingBox, ElementSink, AntCo2Listener
{
	protected class CohesionData
	{
		AntCo2Node node;
		float [] coloniesAttraction;
		
		CohesionData( AntCo2Node node )
		{
			this.node = node;
			this.coloniesAttraction = new float [] { initialAttraction };
		}
		
		void checkBounds( int index )
		{
			if( index >= coloniesAttraction.length )
			{
				int s = coloniesAttraction.length;
				coloniesAttraction = Arrays.copyOf(coloniesAttraction,index+1);
				for( int i=s; i<coloniesAttraction.length; i++ )
					coloniesAttraction [i] = initialAttraction;
			}
		}
		
		void attraction( Colony c )
		{
			int index = c.getIndex();
			
			if( coloniesAttraction [index] == 0 )
				coloniesAttraction [index] = initialAttraction;
			
			coloniesAttraction [index] = (float) pow( coloniesAttraction [index], 1 - attractionFactor );
		}
		
		void neighbor( int index )
		{
			checkBounds(index);
			
			//for( int i = 0; i < coloniesAttraction.length; i++ )
			//	coloniesAttraction [i] = (float) pow( coloniesAttraction [i], i == index ? 1 - neighborAttraction : 1 + neighborPushing );
			
			//if( index == node.getColor().getIndex() )
			coloniesAttraction [index] = (float) pow( coloniesAttraction [index], 1 - neighborAttraction );
			//else
			//	coloniesAttraction [index] = (float) pow( coloniesAttraction [index], 1 + neighborPushing );
		}
		
		int colonyIndex()
		{
			int index = 0;
			
			for( int i = 1; i < coloniesAttraction.length; i++ )
				if( coloniesAttraction [i] > coloniesAttraction [index] ) index = i;
			
			return index;
		}
		
		void tension()
		{
			for( int i = 0; i < coloniesAttraction.length; i++ )
				coloniesAttraction [i] *= tension;
			
			for( AntCo2Edge edge : node.<AntCo2Edge>getEdgeSet() )
			{
				AntCo2Node ne = edge.getOpposite(node);
				
				if( ne.getColor() != null )
					neighbor( ne.getColor().getIndex() );
			}
			
			if( node.getColor() != null )
				neighbor( node.getColor().getIndex() );
		}
	}
	
	protected static float initialAttraction = 0.001f;
	protected static float attractionFactor = 0.125f;
	protected static float neighborAttraction = 0.1f;
	protected static float neighborPushing = 0.05f;
	protected static float tension = 0.99f;
	
	protected HashMap<String,CohesionData> data;
	protected AntContext ctx;
	
	public void init( AntContext ctx )
	{
		this.ctx = ctx;
		this.data = new HashMap<String,CohesionData>();
		
		ctx.getInternalGraph().addElementSink(this);
		ctx.addAntCo2Listener(this);
	}

	public void submitColor( AntCo2Node node, Colony oldColor, 
			Colony newColor )
	{
		CohesionData cd = data.get(node.getId());
		
		if( cd == null )
		{
			cd = new CohesionData(node);
			
			data.put(node.getId(),cd);
		}
		
		if( newColor == null )
			return;
		
		if( oldColor != null )
			cd.checkBounds(oldColor.getIndex());
		cd.checkBounds(newColor.getIndex());
		
		cd.attraction(newColor);
		
		node.setColor( ctx.getColony(cd.colonyIndex()) );
	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId)
	{
		data.remove(nodeId);
	}

	public void graphCleared(String sourceId, long timeId)
	{
		data.clear();
	}

	public void nodeAdded(String sourceId, long timeId, String nodeId)
	{
		
	}

	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed)
	{
		
	}

	public void edgeRemoved(String sourceId, long timeId, String edgeId)
	{
		
	}

	public void stepBegins(String sourceId, long timeId, double step)
	{
		
	}

	public void step(AntContext ctx)
	{
		for( CohesionData cd : data.values() )
			cd.tension();
	}

	public void colonyAdded(Colony c) {
		// TODO Auto-generated method stub
		
	}

	public void colonyRemoved(Colony c) {
		// TODO Auto-generated method stub
		
	}
}
