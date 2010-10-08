package org.graphstream.graph.meta;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

public interface MetaNode
	extends Node
{
	Node addNode( String nodeId );
	
	void removeNode( String nodeId );
	
	Edge addEdge( String edgeId, String sourceNodeId, String targetNodeId, boolean directed );
	
	void removeEdge( String edgeId );
	
	int size();
	
	void clear();
	
	Iterable<String> eachNodeId();
	
	Iterable<String> eachEdgeId();
	
	boolean hasNode( String nodeId );
	
	boolean hasEdge( String edgeId );
}
