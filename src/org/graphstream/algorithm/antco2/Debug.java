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

import java.io.FileWriter;
import java.io.IOException;

import org.graphstream.stream.Sink;

public class Debug
	implements Sink
{
	static FileWriter out;
	static String path = "debug-trace-2";
	
	public static void log( String msg )
	{
		/*
		if( out == null ) try
		{
			out = new FileWriter(path);
			Runtime.getRuntime().addShutdownHook( new Thread() {
				public void run()
				{
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		
		try
		{
			out.write(msg);
			out.flush();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		*/
	}

	public void graphAttributeAdded(String sourceId, long timeId,
			String attribute, Object value) {
		// TODO Auto-generated method stub
		
	}

	public void graphAttributeChanged(String sourceId, long timeId,
			String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	public void graphAttributeRemoved(String sourceId, long timeId,
			String attribute) {
		// TODO Auto-generated method stub
		
	}

	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId,
			String attribute, Object value) {
		// TODO Auto-generated method stub
		
	}

	public void nodeAttributeChanged(String sourceId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	public void nodeAttributeRemoved(String sourceId, long timeId,
			String nodeId, String attribute) {
		// TODO Auto-generated method stub
		
	}

	public void edgeAttributeAdded(String sourceId, long timeId, String edgeId,
			String attribute, Object value) {
		// TODO Auto-generated method stub
		
	}

	public void edgeAttributeChanged(String sourceId, long timeId,
			String edgeId, String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	public void edgeAttributeRemoved(String sourceId, long timeId,
			String edgeId, String attribute) {
		// TODO Auto-generated method stub
		
	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		log( String.format( "[sink] node \"%s\" added%n", nodeId) );
	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		log( String.format( "[sink] node \"%s\" removed%n", nodeId) );
	}

	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		log( String.format( "[sink] edge \"%s\" added%n", edgeId) );
	}

	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
		log( String.format( "[sink] edge \"%s\" removed%n", edgeId) );
	}

	public void graphCleared(String sourceId, long timeId) {
		// TODO Auto-generated method stub
		
	}

	public void stepBegins(String sourceId, long timeId, double step) {
		log( String.format( "[sink] step%n") );
	}
}
