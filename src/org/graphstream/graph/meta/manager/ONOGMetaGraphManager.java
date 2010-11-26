package org.graphstream.graph.meta.manager;

import java.util.HashMap;
import java.util.LinkedList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeFactory;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.NodeFactory;
import org.graphstream.graph.meta.MetaGraph;
import org.graphstream.graph.meta.MetaGraphManager;
import org.graphstream.stream.SinkAdapter;
import org.graphstream.stream.Source;

public class ONOGMetaGraphManager extends SinkAdapter implements
		MetaGraphManager {
	protected static class EdgeData {
		String edgeId;
		String sourceNodeId;
		String targetNodeId;
		boolean directed;

		public EdgeData(Edge e) {
			edgeId = e.getId();
			sourceNodeId = e.getSourceNode().getId();
			targetNodeId = e.getTargetNode().getId();
			directed = e.isDirected();
		}
	}

	class MetaNodeFactory implements NodeFactory {
		public Node newInstance(String id, Graph graph) {
			return new ONOGMetaNode(id, (MetaGraph) graph);
		}
	}

	class MetaEdgeFactory implements EdgeFactory {
		public Edge newInstance(String id, Node src, Node dst, boolean directed) {
			return new ONOGMetaEdge(id, (ONOGMetaNode) src, (ONOGMetaNode) dst,
					directed);
		}
	}

	HashMap<String, String> metaIndexes;
	HashMap<String, String> edgeSourceIndexes;
	MetaGraph metaGraph;
	Source source;

	public ONOGMetaGraphManager() {
		metaIndexes = new HashMap<String, String>();
		edgeSourceIndexes = new HashMap<String, String>();
	}

	public void init(MetaGraph metaGraph, Source source) {
		terminate();

		this.metaGraph = metaGraph;
		this.source = source;

		source.addSink(this);
	}

	public void terminate() {
		if (source != null)
			source.removeSink(this);

		source = null;
		metaGraph = null;
		metaIndexes.clear();
		edgeSourceIndexes.clear();
	}

	public NodeFactory getMetaNodeFactory() {
		return new MetaNodeFactory();
	}

	public EdgeFactory getMetaEdgeFactory() {
		return new MetaEdgeFactory();
	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		if (MetaGraph.debug)
			System.out.printf("[meta] metagraph, nodeAdded \"%s\"%n", nodeId);

		nodeMetaIndexChanged(nodeId, metaIndexUndefined);
	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		if (MetaGraph.debug)
			System.out.printf("[meta] metagraph, nodeRemoved \"%s\"%n", nodeId);

		if (metaIndexes.containsKey(nodeId)) {
			ONOGMetaNode metaNode = (ONOGMetaNode) metaGraph
					.getNode(metaIndexes.get(nodeId));
			metaNode.removeNode(nodeId);

			metaIndexes.remove(nodeId);

			if (metaNode.getInnerGraph().getNodeCount() == 0)
				metaGraph.removeNode(metaNode.getId());
		}
	}

	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId,
			String attributeId, Object value) {
		if (value == null)
			value = metaIndexUndefined;

		if (attributeId.equals("meta.index"))
			nodeMetaIndexChanged(nodeId, value.toString());
	}

	public void nodeAttributeChanged(String sourceId, long timeId,
			String nodeId, String attributeId, Object olValue, Object newValue) {
		if (newValue == null)
			newValue = metaIndexUndefined;

		if (attributeId.equals("meta.index"))
			nodeMetaIndexChanged(nodeId, newValue.toString());
		else if (attributeId.equals("ui.color")) {
			if (metaIndexes.containsKey(nodeId)
					&& !metaIndexes.get(nodeId).equals(metaIndexUndefined))
				metaGraph.getNode(metaIndexes.get(nodeId)).setAttribute(
						"ui.color", newValue);
		}

	}

	protected void nodeMetaIndexChanged(String nodeId, String metaIndex) {
		if (MetaGraph.debug)
			System.out
					.printf("[meta] metagraph, metaindex of \"%s\" changed from \"%s\" to \"%s\"%n",
							nodeId, metaIndexes.get(nodeId), metaIndex);

		if (metaGraph.getNode(metaIndex) == null) {
			metaGraph.addNode(metaIndex);

			if (metaIndex.equals(metaIndexUndefined))
				metaGraph.getNode(metaIndexUndefined).addAttribute("ui.class",
						"undefined");

			if (MetaGraph.debug)
				System.out.printf("[meta] metagraph, new meta node \"%s\"\n",
						metaIndex);
		}

		if (metaIndexes.containsKey(nodeId)) {
			if (metaIndexes.get(nodeId).equals(metaIndex))
				return;

			moveNode(nodeId, metaIndex);
		} else {
			ONOGMetaNode metaNode = (ONOGMetaNode) metaGraph.getNode(metaIndex);
			metaNode.addNode(nodeId);

			metaIndexes.put(nodeId, metaIndex);
		}
	}

	protected void moveNode(String nodeId, String metaIndex) {
		ONOGMetaNode current = (ONOGMetaNode) metaGraph.getNode(metaIndexes
				.get(nodeId));
		Node n = current.getInnerGraph().getNode(nodeId);
		LinkedList<EdgeData> edgesToRecreate = new LinkedList<EdgeData>();

		while (n.getDegree() > 0) {
			edgesToRecreate.add(new EdgeData(n.getEdge(0)));
			removeInnerEdge(n.getEdge(0).getId());
		}

		current.removeNode(nodeId);

		if (current.getInnerGraph().getNodeCount() == 0) {
			if (MetaGraph.debug)
				System.out.printf("[meta] metagraph, delete metanode \"%s\"%n",
						current.getId());
			metaGraph.removeNode(current.getId());
		}

		ONOGMetaNode metaNode = (ONOGMetaNode) metaGraph.getNode(metaIndex);
		metaNode.addNode(nodeId);

		metaIndexes.put(nodeId, metaIndex);

		while (edgesToRecreate.size() > 0) {
			EdgeData ed = edgesToRecreate.poll();

			addInnerEdge(ed.edgeId, ed.sourceNodeId, ed.targetNodeId,
					ed.directed);
		}
	}

	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String sourceNodeId, String targetNodeId, boolean directed) {
		if (MetaGraph.debug)
			System.out
					.printf("[meta] metagraph, edgeAdded \"%s\" from \"%s\" to \"%s\"%n",
							edgeId, sourceNodeId, targetNodeId);

		addInnerEdge(edgeId, sourceNodeId, targetNodeId, directed);
	}

	protected void addInnerEdge(String edgeId, String sourceNodeId,
			String targetNodeId, boolean directed) {
		if (MetaGraph.debug)
			System.out
					.printf("[meta] metagraph, addEdge \"%s\" from \"%s\" to \"%s\"%n\tfrom meta \"%s\" to meta \"%s\"%n",
							edgeId, sourceNodeId, targetNodeId,
							metaIndexes.get(sourceNodeId),
							metaIndexes.get(targetNodeId));

		ONOGMetaNode sourceMetaNode = (ONOGMetaNode) metaGraph
				.getNode(metaIndexes.get(sourceNodeId));
		ONOGMetaNode targetMetaNode = (ONOGMetaNode) metaGraph
				.getNode(metaIndexes.get(targetNodeId));

		edgeSourceIndexes.put(edgeId, sourceMetaNode.getId());

		if (sourceMetaNode == targetMetaNode) {
			sourceMetaNode
					.addEdge(edgeId, sourceNodeId, targetNodeId, directed);
		} else {
			if (sourceMetaNode.getEdgeToward(targetMetaNode.getId()) == null) {
				String metaEdgeId = String.format("metaedge-%s-%s",
						sourceMetaNode.getId(), targetMetaNode.getId());

				metaGraph.addEdge(metaEdgeId, sourceMetaNode.getId(),
						targetMetaNode.getId(), false);

				if (MetaGraph.debug)
					System.out.printf(
							"[meta] metagraph, add metaedge \"%s\"%n",
							metaEdgeId);
			}

			sourceMetaNode
					.addEdge(edgeId, sourceNodeId, targetNodeId, directed);
			targetMetaNode
					.addEdge(edgeId, sourceNodeId, targetNodeId, directed);
		}
	}

	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
		removeInnerEdge(edgeId);
	}

	public void removeInnerEdge(String edgeId) {
		if (MetaGraph.debug)
			System.out.printf("[meta] metagraph, edgeRemoved \"%s\"%n", edgeId);

		if (edgeSourceIndexes.containsKey(edgeId)) {
			ONOGMetaNode metaNode = (ONOGMetaNode) metaGraph
					.getNode(edgeSourceIndexes.get(edgeId));

			String op = metaNode.getInnerGraph().getEdge(edgeId)
					.getTargetNode().getId();

			if (metaNode != null)
				metaNode.removeEdge(edgeId);

			if (!metaNode.getId().equals(metaIndexes.get(op))) {
				ONOGMetaEdge metaEdge = (ONOGMetaEdge) metaNode
						.getEdgeToward(metaIndexes.get(op));
				if (metaEdge.size() == 0) {
					if (MetaGraph.debug)
						System.out.printf(
								"[meta] metagraph, remove metaedge \"%s\"%n",
								metaEdge.getId());
					metaGraph.removeEdge(metaEdge.getId());
				}
			}

			edgeSourceIndexes.remove(edgeId);
		}
	}
}
