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

import org.graphstream.algorithm.DynamicAlgorithm;
import org.graphstream.graph.Graph;
import org.graphstream.stream.SinkAdapter;
import org.graphstream.stream.thread.ThreadProxyPipe;

public class AntCo2Algorithm
	extends SinkAdapter implements DynamicAlgorithm
{
	AntContext context;
	ThreadProxyPipe	proxy;
	
	Graph registeredGraph;
	
	
	public AntCo2Algorithm()
	{
		context   = new AntContext();
	}

	public AntContext getContext()
	{
		return context;
	}
	
	public Graph getRegisteredGraph()
	{
		return registeredGraph;
	}
	
	public void addAntCo2Listener( AntCo2Listener listener )
	{
		context.addAntCo2Listener(listener);
	}
	
	public void removeAntCo2Listener( AntCo2Listener listener )
	{
		context.removeAntCo2Listener(listener);
	}
	
	public void init(Graph graph)
	{
		proxy = new ThreadProxyPipe(graph);
		proxy.addSink( context.internalGraph );
		
		registeredGraph = graph;
		
		graph.addAttributeSink(this);
		
		context.init();
	}

	public void compute()
	{
		proxy.pump();
		
		context.step();
		publishColor();
		
		proxy.pump();
	}
	
	public void terminate() {
		if( registeredGraph != null )
			registeredGraph.removeSink(proxy);
		
		proxy.removeSink(context.internalGraph);
	}
	
	public void publishColor()
	{
		if( registeredGraph != null )
		{
			context.lock();
			
			Colony c0, c1;
			
			for( AntCo2Node n : context.eachNode() )
			{
				c0 = n.getOldColor();
				c1 = n.getColor();
				//Cell c = n.getCell();
				
				// registeredGraph.nodeAttributeChanged( context.sourceId(), context.timeId(), n.getId(), 
				//		"ui.label", c0 == null ? null : c0.getName(), c1 == null ? null : c1.getName() );
				registeredGraph.nodeAttributeChanged( context.sourceId(), context.timeId(), n.getId(), 
						"meta.index", null, c0 == null ? null : c0.getIndex() );
				//n.setAttribute("meta.index", c0 == null ? null : c0.getIndex());
				registeredGraph.nodeAttributeChanged( context.sourceId(), context.timeId(), n.getId(), 
						"ui.color", c0 == null ? null : c0.getIndex() / (float) context.getColonyCount(), c1 == null ? null : c1.getIndex() / (float) context.getColonyCount() );
				
				if( n.isMembrane() )
				{
					registeredGraph.nodeAttributeChanged( context.sourceId(), context.timeId(), n.getId(),
							"ui.class", null, "membrane" );
				}
				else
				{
					registeredGraph.nodeAttributeRemoved( context.sourceId(), context.timeId(), n.getId(), "ui.class" );
				}
			}
			
			context.unlock();
		}
	}

	public void graphAttributeAdded(String sourceId, long timeId,
			String attribute, Object value) {
		if( attribute.equals("antco2.resources") )
			resourcesHandler( value.toString() );
	}

	public void graphAttributeChanged(String sourceId, long timeId,
			String attribute, Object oldValue, Object newValue) {
		if( attribute.equals("antco2.resources") )
			resourcesHandler( newValue.toString() );
	}
	
	public void resourcesHandler( String s )
	{
		s = s.trim();
		if( s.matches( "^(\\+|-)\\s+.*$" ) )
		{
			String resource = s.substring(2).trim();
			
			if( s.startsWith("+") )
			{
				context.lock();
				System.out.printf("add colony: %s%n", resource );
				context.addColony(resource);
				context.unlock();
			}
			else
			{
				context.lock();
				System.out.printf("del colony: %s%n", resource );
				context.removeColony(resource);
				context.unlock();
			}
		}
	}
}
