package org.graphstream.algorithm.antco2;

import org.graphstream.stream.SourceBase;

public class ManualSource
	extends SourceBase
{
	private static int manualSourceIdGenerator = 1;

	public ManualSource() {
		this(String.format("manual-source-%x", manualSourceIdGenerator++));
	}

	public ManualSource(String id) {
		super(id);
	}

	public void addNode( String nodeId ) {
		sendNodeAdded(sourceId, nodeId);
	}

	public void removeNode( String nodeId ) {
		sendNodeRemoved(sourceId, nodeId);
	}

	public void addEdge( String edgeId, String nodeIdA, String nodeIdB,
			boolean directed ) {
		sendEdgeAdded(sourceId, edgeId, nodeIdA, nodeIdB, directed);
	}

	public void removeEdge( String edgeId ) {
		sendEdgeRemoved(sourceId, edgeId);
	}

	public void nodeAddAttribute( String nodeId, String attrId, Object value ) {
		sendNodeAttributeAdded(sourceId, nodeId, attrId, value);
	}

	public void nodeChangeAttribute( String nodeId, String attrId,
			Object oldValue, Object newValue ) {
		sendNodeAttributeChanged(sourceId, nodeId, attrId, oldValue, newValue);
	}

	public void nodeRemoveAttribute( String edgeId, String attrId ) {
		sendNodeAttributeRemoved(sourceId, edgeId, attrId);
	}

	public void edgeAddAttribute( String edgeId, String attrId, Object value ) {
		sendEdgeAttributeAdded(sourceId, edgeId, attrId, value);
	}

	public void edgeChangeAttribute( String edgeId, String attrId,
			Object oldValue, Object newValue ) {
		sendEdgeAttributeChanged(sourceId, edgeId, attrId, oldValue, newValue);
	}

	public void edgeRemoveAttribute( String edgeId, String attrId ) {
		sendEdgeAttributeRemoved(sourceId, edgeId, attrId);
	}

	public void graphAddAttribute( String attrId, Object value ) {
		sendGraphAttributeAdded(sourceId, attrId, value);
	}

	public void graphChangeAttribute( String attrId,
			Object oldValue, Object newValue) {
		sendGraphAttributeChanged(sourceId, attrId, oldValue, newValue );
	}

	public void graphRemoveAttribute( String attrId ) {
		sendGraphAttributeRemoved(sourceId, attrId);
	}
}
