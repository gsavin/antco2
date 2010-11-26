/**
 * 
 */
package org.graphstream.graph.meta;

import org.graphstream.graph.EdgeFactory;
import org.graphstream.graph.NodeFactory;
import org.graphstream.stream.Sink;
import org.graphstream.stream.Source;

public interface MetaGraphManager extends Sink {
	public static final String metaIndexUndefined = "metanode-undefined";

	void init(MetaGraph metaGraph, Source source);

	void terminate();

	NodeFactory getMetaNodeFactory();

	EdgeFactory getMetaEdgeFactory();
}