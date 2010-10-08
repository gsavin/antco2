package org.graphstream.graph.meta.manager;

import java.util.Iterator;
import java.util.LinkedList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiEdge;
import org.graphstream.graph.meta.MetaEdge;
import org.graphstream.graph.meta.MetaGraph;

public class OGANMetaEdge
	extends MultiEdge implements MetaEdge
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

	private class __iterable_edges
		implements Iterable<String>
	{
		public Iterator<String> iterator()
		{
			return new __element_to_id_iterator( edges.iterator() );
		}
	}
	
	protected Graph 			sharedGraph;
	protected LinkedList<Edge>	edges;
	
	public OGANMetaEdge( Graph sharedGraph, String edgeId,
			OGANMetaNode source, OGANMetaNode target, boolean directed )
	{
		super(edgeId,source,target,directed);
		
		edges = new LinkedList<Edge>();
		
		this.sharedGraph = sharedGraph;
	}
	
	public int size()
	{
		return edges.size();
	}
	
	public void addEdge( String edgeId )
	{
		if( MetaGraph.debug )
			System.out.printf("[meta] metaedge \"%s\", addedge \"%s\"%n",getId(),edgeId);
		
		Edge e = sharedGraph.getEdge(edgeId);
		
		if( e != null )
		{
			edges.add(e);
			updateEdgeSize();
		}
	}
	
	public void removeEdge( String edgeId )
	{
		if( MetaGraph.debug )
			System.out.printf("[meta] metaedge \"%s\", removeedge \"%s\"%n",getId(),edgeId);
		
		Edge e = sharedGraph.getEdge(edgeId);
		
		if( e != null )
		{
			edges.remove(e);
			updateEdgeSize();
		}
	}
	
	private Iterable<String> iterableEdges = new __iterable_edges();
	
	public Iterable<String> eachEdgeId()
	{
		return iterableEdges;
	}
	
	public void clear()
	{
		edges.clear();
	}
	
	protected void updateEdgeSize()
	{
		setAttribute( "ui.style","size: " + ( edges.size() + 1 ) + "px;" );
	}
}
