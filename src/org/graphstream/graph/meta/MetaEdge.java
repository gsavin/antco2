package org.graphstream.graph.meta;

import org.graphstream.graph.Edge;

public interface MetaEdge
	extends Edge
{
	void addEdge( String edgeId );
	
	void removeEdge( String edgeId );
	
	int size();
	
	Iterable<String> eachEdgeId();
	
	void clear();
}
