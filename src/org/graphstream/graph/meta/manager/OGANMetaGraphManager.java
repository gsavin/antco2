package org.graphstream.graph.meta.manager;

import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeFactory;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.NodeFactory;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.meta.MetaEdge;
import org.graphstream.graph.meta.MetaGraph;
import org.graphstream.graph.meta.MetaGraphManager;
import org.graphstream.graph.meta.MetaNode;
import org.graphstream.stream.SinkAdapter;
import org.graphstream.stream.Source;

public class OGANMetaGraphManager extends SinkAdapter implements
		MetaGraphManager {
	class MetaNodeFactory implements NodeFactory {
		public Node newInstance(String id, Graph metaGraph) {
			return new OGANMetaNode(id, (MetaGraph) metaGraph, graph);
		}
	}

	class MetaEdgeFactory implements EdgeFactory {
		public Edge newInstance(String id, Node src, Node dst, boolean directed) {
			return new OGANMetaEdge(graph, id, (OGANMetaNode) src,
					(OGANMetaNode) dst, directed);
		}
	}

	protected MetaGraph metaGraph;
	protected Graph graph;
	protected Source source;

	public EdgeFactory getMetaEdgeFactory() {
		return new MetaEdgeFactory();
	}

	public NodeFactory getMetaNodeFactory() {
		return new MetaNodeFactory();
	}

	public void init(MetaGraph metaGraph, Source source) {
		this.metaGraph = metaGraph;
		this.source = source;

		if (source instanceof Graph) {
			graph = (Graph) source;
		} else {
			graph = new MultiGraph(String.format("%s-shared-graph",
					metaGraph.getId()));
		}

		source.addSink(this);
	}

	public void terminate() {
		if (source != null)
			source.removeSink(this);

		source = null;
		graph = null;
	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		nodeMetaIndexChanged(nodeId, metaIndexUndefined);
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
			String metaNodeIndex = (String) graph.getNode(nodeId).getAttribute(
					"meta.node");

			if (metaNodeIndex != null
					&& !metaNodeIndex.equals(metaIndexUndefined))
				metaGraph.getNode(metaNodeIndex).setAttribute("ui.color",
						newValue);
		}

	}

	protected void nodeMetaIndexChanged(String nodeId, String metaIndex) {
		MetaNode metaNode = (MetaNode) metaGraph.getNode(metaIndex);
		Node node = graph.getNode(nodeId);

		if (metaNode == null)
			metaNode = (MetaNode) metaGraph.addNode(metaIndex);

		if (node == null)
			node = graph.addNode(nodeId);

		String currentMetaNodeId = (String) node.getAttribute("meta.node");

		if (currentMetaNodeId != null && currentMetaNodeId.equals(metaIndex))
			return;

		MetaNode currentMetaNode = (MetaNode) metaGraph
				.getNode(currentMetaNodeId);

		for (int i = 0; i < node.getDegree(); i++) {
			Edge e = node.getEdge(i);
			removeInnerEdge(e);
		}

		if (currentMetaNodeId != null) {
			currentMetaNode.removeNode(nodeId);

			if (currentMetaNode.size() == 0)
				metaGraph.removeNode(currentMetaNodeId);
		}

		metaNode.addNode(nodeId);

		if (MetaGraph.debug)
			System.out.printf(
					"[meta] manager, set meta.node of \"%s\" to \"%s\"%n",
					nodeId, metaIndex);

		node.setAttribute("meta.node", metaIndex);

		for (int i = 0; i < node.getDegree(); i++) {
			Edge e = node.getEdge(i);
			addInnerEdge(e);
		}
	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		Node n = graph.getNode(nodeId);
		MetaNode metaNode = (MetaNode) metaGraph.getNode((String) n
				.getAttribute("meta.node"));

		for (int i = 0; i < n.getDegree(); i++) {
			Edge e = n.getEdge(i);

			if (e.hasAttribute("meta.edge")) {
				MetaEdge metaEdge = (MetaEdge) metaGraph.getEdge((String) e
						.getAttribute("meta.edge"));
				metaEdge.removeEdge(e.getId());

				if (metaEdge.size() == 0)
					metaGraph.removeEdge(metaEdge.getId());
			} else {
				metaNode.removeEdge(e.getId());
			}
		}

		metaNode.removeNode(nodeId);

		if (metaNode.size() == 0)
			metaGraph.removeNode(metaNode.getId());
	}

	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String sourceNodeId, String targetNodeId, boolean directed) {
		Edge e = graph.getEdge(edgeId);
		addInnerEdge(e);
	}

	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
		Edge e = graph.getEdge(edgeId);
		removeInnerEdge(e);
	}

	protected void addInnerEdge(Edge e) {
		if (MetaGraph.debug)
			System.out.printf("[meta] manager, addedge \"%s\"%n", e.getId());

		String sourceMetaNodeId = (String) e.getSourceNode().getAttribute(
				"meta.node");
		String targetMetaNodeId = (String) e.getTargetNode().getAttribute(
				"meta.node");

		if (sourceMetaNodeId.equals(targetMetaNodeId)) {
			MetaNode metaNode = (MetaNode) metaGraph.getNode(sourceMetaNodeId);
			metaNode.addEdge(e.getId(), e.getSourceNode().getId(), e
					.getTargetNode().getId(), e.isDirected());
		} else {
			MetaNode metaNode = (MetaNode) metaGraph.getNode(sourceMetaNodeId);
			MetaEdge metaEdge = (MetaEdge) metaNode
					.getEdgeToward(targetMetaNodeId);

			if (metaEdge == null) {
				metaEdge = (MetaEdge) metaGraph.addEdge(String.format(
						"metaedge-%s-%s", sourceMetaNodeId, targetMetaNodeId),
						sourceMetaNodeId, targetMetaNodeId, false);

				if (MetaGraph.debug)
					System.out
							.printf("[meta] manager, add metaedge \"%s\" from \"%s\" to \"%s\"%n",
									metaEdge.getId(), sourceMetaNodeId,
									targetMetaNodeId);
			}

			metaEdge.addEdge(e.getId());

			if (MetaGraph.debug)
				System.out.printf(
						"[meta] manager, set meta.edge of \"%s\" to \"%s\"%n",
						e.getId(), metaEdge.getId());

			graph.getEdge(e.getId())
					.setAttribute("meta.edge", metaEdge.getId());
		}
	}

	protected void removeInnerEdge(Edge e) {
		if (MetaGraph.debug)
			System.out.printf("[meta] manager, removeedge \"%s\"%n", e.getId());

		if (!e.hasAttribute("meta.edge")) {
			MetaNode metaNode = (MetaNode) metaGraph.getNode((String) e
					.getSourceNode().getAttribute("meta.node"));
			metaNode.removeEdge(e.getId());
		} else {
			MetaEdge metaEdge = (MetaEdge) metaGraph.getEdge((String) e
					.getAttribute("meta.edge"));

			if (MetaGraph.debug)
				System.out.printf("[meta] meta edge of \"%s\" is \"%s\"%n",
						e.getId(), (String) e.getAttribute("meta.edge"));

			metaEdge.removeEdge(e.getId());

			if (metaEdge.size() == 0) {
				if (MetaGraph.debug)
					System.out.printf(
							"[meta] manager, remove metaedge \"%s\"%n",
							metaEdge.getId());

				metaGraph.removeEdge(metaEdge.getId());
			}

			e.removeAttribute("meta.edge");
		}
	}
}
