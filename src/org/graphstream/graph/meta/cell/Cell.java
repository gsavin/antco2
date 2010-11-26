package org.graphstream.graph.meta.cell;

import org.graphstream.graph.Graph;
import org.graphstream.graph.meta.MetaGraph;
import org.graphstream.graph.meta.manager.OGANMetaNode;

public class Cell extends OGANMetaNode {
	String metaIndex;

	public Cell(String metaIndex, String cellId, MetaGraph metaGraph,
			Graph sharedGraph) {
		super(cellId, metaGraph, sharedGraph);
		this.metaIndex = metaIndex;
	}

	public String metaIndex() {
		return metaIndex;
	}
}
