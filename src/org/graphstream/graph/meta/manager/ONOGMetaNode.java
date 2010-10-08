package org.graphstream.graph.meta.manager;

import java.util.Iterator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.MultiNode;
import org.graphstream.graph.meta.MetaGraph;

public class ONOGMetaNode
	extends MultiNode
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
		@SuppressWarnings("all")
		public Iterator<String> iterator()
		{
			return new __element_to_id_iterator(innerGraph.getNodeIterator());
		}
	}

	private class __iterable_edges
		implements Iterable<String>
	{
		@SuppressWarnings("all")
		public Iterator<String> iterator()
		{
			return new __element_to_id_iterator( (Iterator<? extends Element>) innerGraph.getEdgeIterator());
		}
	}
	
	protected Graph innerGraph;
	
	public ONOGMetaNode( String nodeId, MetaGraph g )
	{
		super( g, nodeId );
		innerGraph = new MultiGraph(nodeId);
	}
	
	public Graph getInnerGraph()
	{
		return innerGraph;
	}
	
	public Node addNode( String nodeId )
	{
		if( MetaGraph.debug )
			System.out.printf("[meta] metanode \"%s\", addnode \"%s\"%n",getId(),nodeId);
		
		setAttribute( "ui.style", "size: " + ( innerGraph.getNodeCount() + 5 ) + "px;" );
		
		if( innerGraph.getNode(nodeId) == null )
			return innerGraph.addNode(nodeId);
		else
		{
			Node n = innerGraph.getNode(nodeId);
			
			if( isVirtual(n) )
			{
				n.removeAttribute("meta.virtual");
				/*
				for( Edge e : n )
				{
					if( ! isVirtual( e.getOpposite(n) ) )
						e.removeAttribute("meta.virtual");
				}
				*/
			}
			else
			{
				System.err.printf("something is strange ... trying to add node \"%s\"\n",nodeId);
			}
			
			return innerGraph.getNode(nodeId);
		}
	}
	
	protected Node addVirtualNode( String nodeId )
	{
		Node n = innerGraph.addNode(nodeId);
		n.setAttribute( "meta.virtual" );
		return n;
	}
	
	protected boolean isVirtualNode( String nodeId )
	{
		Node n = innerGraph.getNode(nodeId);
		
		assert n!=null : "unknown node";
		
		return isVirtual(n);
	}
	
	protected boolean isVirtual( Element e )
	{
		return e.hasAttribute("meta.virtual");
	}
	
	public void removeNode( String nodeId )
	{
		if( MetaGraph.debug )
			System.out.printf("[meta] metanode \"%s\", remove node \"%s\"%n",getId(),nodeId);
		
		setAttribute( "ui.style", "size: " + ( innerGraph.getNodeCount() + 5 ) + "px;" );
		
		innerGraph.removeNode(nodeId);
	}
	
	public Edge addEdge( String edgeId, String srcNodeId, String trgNodeId, boolean directed )
	{
		if( MetaGraph.debug )
			System.out.printf("[meta] metanode \"%s\", add edge \"%s\"%n",getId(),edgeId);
		
		Node src = innerGraph.getNode(srcNodeId);
		Node trg = innerGraph.getNode(trgNodeId);
		
		if( src == null )
			src = addVirtualNode(srcNodeId);
		
		if( trg == null )
			trg = addVirtualNode(trgNodeId);
		
		boolean virtualEdge = isVirtualNode(srcNodeId) || isVirtualNode(trgNodeId);
		
		Edge e = innerGraph.addEdge(edgeId,srcNodeId,trgNodeId,directed);
		
		if( virtualEdge )
			e.setAttribute("meta.virtual",true);
		
		return e;
	}
	
	public void removeEdge( String edgeId )
	{
		if( MetaGraph.debug )
			System.out.printf("[meta] metanode \"%s\", remove edge \"%s\"%n",getId(),edgeId);
		
		Edge e = innerGraph.getEdge(edgeId);
		
		if( e != null )
		{
			Node sourceNode = e.getSourceNode();
			Node targetNode = e.getTargetNode();
		
			innerGraph.removeEdge(edgeId);
		
			if( isVirtual(sourceNode) && sourceNode.getDegree() == 0 )
				removeNode(sourceNode.getId());
		
			if( isVirtual(targetNode) && targetNode.getDegree() == 0 )
				removeNode(targetNode.getId());
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
	
	public void clear()
	{
		innerGraph.clear();
	}
}
