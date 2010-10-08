package org.graphstream.graph.meta;

import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.meta.manager.OGANMetaGraphManager;
import org.graphstream.stream.Source;

public class MetaGraph
	extends MultiGraph
{
	public static boolean debug = false;
	
	MetaGraphManager		metaGraphManager;
	
	public MetaGraph( String graphId, Source source )
	{
		super(graphId);
		
		metaGraphManager = new OGANMetaGraphManager();
		
		nodeFactory = metaGraphManager.getMetaNodeFactory();
		edgeFactory = metaGraphManager.getMetaEdgeFactory();
		
		metaGraphManager.init(this,source);
	}
	
	public Edge removeEdge( String edgeId )
	{
		if( debug )
		{
			System.out.printf("[meta] metagraph, remove metaedge \"%s\"%n", edgeId );
			//Exception e = new Exception();
			//e.printStackTrace();
		}
		
		return super.removeEdge(edgeId);
	}
}
