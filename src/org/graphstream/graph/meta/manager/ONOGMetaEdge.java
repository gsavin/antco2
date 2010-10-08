package org.graphstream.graph.meta.manager;

import java.util.HashSet;

import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.MultiEdge;
import org.graphstream.graph.meta.MetaGraph;
import org.graphstream.stream.SinkAdapter;

public class ONOGMetaEdge
	extends MultiEdge
{
	class VirtualEdgeSink
		extends SinkAdapter
	{
		public void edgeAttributeAdded( String sourceId, long timeId, 
				String edgeId, String key, Object value )
		{
			if( key.equals( "meta.virtual" ) )
				addEdge(sourceId,edgeId);
		}

		public void edgeAttributeChanged( String sourceId, long timeId,
				String edgeId, String key, Object oldValue, Object newValue )
		{
			if( key.equals( "meta.virtual" ) )
				addEdge(sourceId,edgeId);
		}
		public void edgeAttributeRemoved( String sourceId, long timeId, 
				String edgeId, String key )
		{
			if( key.equals( "meta.virtual" ) )
				edges.remove(edgeId);
		}

		protected void addEdge( String sourceId, String edgeId )
		{
			ONOGMetaNode source = null;
			ONOGMetaNode target = null;

			if( getSourceNode().getId().equals(sourceId) )
				source = (ONOGMetaNode) getSourceNode();
			else if( getTargetNode().getId().equals(sourceId) )
				source = (ONOGMetaNode) getTargetNode();

			if( source == null )
				return;

			target = (ONOGMetaNode) getOpposite(source);

			if( target.getInnerGraph().getEdge(edgeId) == null )
				return;

			if( MetaGraph.debug )
				System.out.printf("[meta] metaedge \"%s\", add edge \"%s\" from \"%s\"%n",getId(),edgeId,sourceId);

			setAttribute( "ui.style", "size: " + ( size() + 1 ) + "px;" );

			String virtualNodeId;
			Edge e = source.getInnerGraph().getEdge(edgeId);

			if( source.isVirtualNode(e.getSourceNode().getId()) )
				virtualNodeId = e.getSourceNode().getId();
			else if( source.isVirtualNode(e.getTargetNode().getId()) )
				virtualNodeId = e.getTargetNode().getId();
			else
			{
				System.err.printf("[error] non-virtual edge\n");
				return;
			}

			if( target.getInnerGraph().getNode(virtualNodeId) != null &&
					! target.isVirtualNode(virtualNodeId) )
			{
				edges.add(edgeId);
			}
			else if( target.getInnerGraph().getNode(virtualNodeId) == null )
			{
				System.err.printf("[error] cant find node \"%s\" on \"%s\"\n",virtualNodeId,target.getId());
				System.exit(0);
			}
			else System.err.printf("something strange... should not be virtual\n");
		}

		public void edgeRemoved( String sourceId, long timeId,
				String edgeId )
		{
			if( MetaGraph.debug )
				System.out.printf("[meta] metaedge \"%s\", remove edge \"%s\"%n",getId(),edgeId);

			if( ! edges.contains(edgeId) )
				return;

			edges.remove(edgeId);

			setAttribute( "ui.style", "size: " + ( size() + 1 ) + "px;" );

			ONOGMetaNode metaNode = null;

			if( sourceId.equals(getSourceNode().getId()) )
				metaNode = (ONOGMetaNode) getTargetNode();
			else if( sourceId.equals(getTargetNode().getId()) )
				metaNode = (ONOGMetaNode) getSourceNode();

			if( metaNode != null )
				metaNode.removeEdge(edgeId);
		}
	}

	HashSet<String> edges;
	VirtualEdgeSink	sink;

	public ONOGMetaEdge( String edgeId, ONOGMetaNode src, ONOGMetaNode trg,
			boolean directed )
	{
		super(edgeId,src,trg,directed);

		edges = new HashSet<String>();
		sink  = new VirtualEdgeSink();
		src.getInnerGraph().addSink(sink);
		trg.getInnerGraph().addSink(sink);
	}

	public int size()
	{
		return edges.size();
	}

	protected void unbind( String sourceId, long timeId )
	throws IllegalStateException
	{
		((ONOGMetaNode)getSourceNode()).getInnerGraph().removeSink(sink);
		((ONOGMetaNode)getTargetNode()).getInnerGraph().removeSink(sink);
		super.unbind(sourceId,timeId);
	}
}
