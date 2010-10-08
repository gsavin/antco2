package org.graphstream.graph.meta.manager;

import java.util.Iterator;
import java.util.LinkedList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiNode;
import org.graphstream.graph.meta.MetaGraph;
import org.graphstream.graph.meta.MetaNode;

public class OGANMetaNode
	extends MultiNode implements MetaNode
{
	private static class __element_to_id_iterator
		implements Iterator<String>
	{
		Iterator<? extends Element> ite;
		
		public __element_to_id_iterator( Iterator<? extends Element> ite )
		{
			this.ite = ite;
		}
		
		public boolean hasNext()
		{
			return ite.hasNext();
		}
		
		public String next()
		{
			Element e = ite.next();
			
			if( e != null )
				return e.getId();
			
			return null;
		}
		
		public void remove()
		{
			
		}
	}
	
	private class __iterable_nodes
		implements Iterable<String>
	{
		public Iterator<String> iterator()
		{
			return new __element_to_id_iterator(nodes.iterator());
		}
	}
	
	private class __iterable_edges
		implements Iterable<String>
	{
		public Iterator<String> iterator()
		{
			return new __element_to_id_iterator(edges.iterator());
		}
	}
	
	protected Graph				sharedGraph;
	protected LinkedList<Node>	nodes;
	protected LinkedList<Edge>	edges;
	
	public OGANMetaNode( String nodeId, MetaGraph metaGraph, Graph sharedGraph )
	{
		super(metaGraph,nodeId);
		
		nodes = new LinkedList<Node>();
		edges = new LinkedList<Edge>();
		
		this.sharedGraph = sharedGraph;
	}

	public int size()
	{
		return nodes.size();
	}
	
	public Edge addEdge(String edgeId, String sourceNodeId,
			String targetNodeId, boolean directed)
	{
		if( MetaGraph.debug )
			System.out.printf("[meta] metanode \"%s\", addedge \"%s\" from \"%s\" to \"%s\"%n",getId(),edgeId,sourceNodeId,targetNodeId);
		
		Edge e = sharedGraph.getEdge(edgeId);
		
		if( e == null )
			e = sharedGraph.addEdge(edgeId,sourceNodeId,targetNodeId,directed);
		
		if( ! edges.contains(e) )
			edges.add(e);
		
		return null;
	}

	public Node addNode(String nodeId)
	{
		if( MetaGraph.debug )
			System.out.printf("[meta] metanode \"%s\", addnode \"%s\"%n",getId(),nodeId);
		
		Node n = sharedGraph.getNode(nodeId);
		
		if( n == null )
			n = sharedGraph.addNode(nodeId);
		
		if( ! nodes.contains(n) )
			nodes.add(n);
		
		updateNodeSize();
		
		return n;
	}

	public void removeEdge(String edgeId)
	{
		if( MetaGraph.debug )
			System.out.printf("[meta] metanode \"%s\", removeedge \"%s\"%n",getId(),edgeId);
		
		Edge e = sharedGraph.getEdge(edgeId);
		
		if( e != null )
		{
			edges.remove(e);
		}
	}

	public void removeNode(String nodeId)
	{
		if( MetaGraph.debug )
			System.out.printf("[meta] metanode \"%s\", removenode \"%s\"%n",getId(),nodeId);
		
		Node n = sharedGraph.getNode(nodeId);
		
		if( n != null )
		{
			nodes.remove(n);
			updateNodeSize();
		}
	}
	
	private Iterable<String> iterableNodes = new __iterable_nodes();
	private Iterable<String> iterableEdges = new __iterable_edges();
	
	public Iterable<String> eachNodeId()
	{
		return iterableNodes;
	}

	public Iterable<String> eachEdgeId()
	{
		return iterableEdges;
	}
	
	public boolean hasNode( String nodeId )
	{
		return nodes.contains(nodeId);
	}
	
	public boolean hasEdge( String edgeId )
	{
		return edges.contains(edgeId);
	}
	
	public void clear()
	{
		nodes.clear();
		edges.clear();
	}
	
	protected void updateNodeSize()
	{
		setAttribute( "ui.style", "size: " + ( nodes.size() + 5 ) + "px;" );
	}
}
