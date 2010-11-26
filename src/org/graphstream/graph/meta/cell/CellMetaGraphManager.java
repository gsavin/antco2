package org.graphstream.graph.meta.cell;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeFactory;
import org.graphstream.graph.Node;
import org.graphstream.graph.NodeFactory;
import org.graphstream.graph.meta.MetaEdge;
import org.graphstream.graph.meta.MetaGraph;
import org.graphstream.graph.meta.MetaNode;
import org.graphstream.graph.meta.manager.OGANMetaGraphManager;
import org.graphstream.stream.Source;

public class CellMetaGraphManager extends OGANMetaGraphManager {
	@SuppressWarnings("serial")
	class CellSet extends HashMap<String, Cell> {
		String prefix;
		int currentCellId;

		public CellSet(String prefix) {
			this.prefix = prefix;
			currentCellId = 0;
		}

		public Cell newCell() {
			Cell c = (Cell) metaGraph.addNode(String.format("cell-%s-%d",
					prefix, currentCellId));
			put(c.getId(), c);
			return c;
		}
	}

	HashMap<String, String> globalMetaIndexes;
	HashMap<String, CellSet> cells;
	MetaGraph metaGraph;

	protected void nodeMetaIndexChanged(String nodeId, String metaIndex) {
		if (globalMetaIndexes.containsKey(nodeId)
				&& globalMetaIndexes.get(nodeId).equals(metaIndex))
			return;

		if (!globalMetaIndexes.containsKey(nodeId)) {
			CellSet cSet = cells.get(metaIndex);

			if (cSet == null) {
				cSet = new CellSet(metaIndex);
				cells.put(metaIndex, cSet);
			}

			if (cSet.size() == 0) {
				Cell c = cSet.newCell();
				super.nodeMetaIndexChanged(nodeId, c.getId());
			} else {

			}
		} else {

		}
	}

	protected Cell findConnectedCell(String nodeId, String metaIndex) {
		Node node = this.graph.getNode(nodeId);
		LinkedList<String> availableCells = new LinkedList<String>();

		for (Edge edge : node) {
			Node op = edge.getOpposite(node);

			if (globalMetaIndexes.containsKey(op.getId())
					&& globalMetaIndexes.get(op.getId()).equals(metaIndex))
				availableCells.add((String) op.getAttribute("meta.node"));
		}

		Cell choice;

		switch (availableCells.size()) {
		case 0:
			choice = cells.get(metaIndex).newCell();
			break;
		case 1:
			choice = ((Cell) metaGraph.getNode(availableCells.get(0)));
			break;
		default:
			choice = fusion(availableCells);
			break;
		}

		choice.addNode(nodeId);
		graph.getNode(nodeId).setAttribute("meta.node", choice.getId());

		return null;
	}

	protected String findCellId(String nodeId, String metaIndex) {
		return null;
	}

	protected Cell fusion(LinkedList<String> toFusion) {
		LinkedList<Cell> cellsToMerge = new LinkedList<Cell>();
		Cell lamama = null;

		for (String cellId : toFusion) {
			cellsToMerge.add((Cell) metaGraph.getNode(cellId));

			if (lamama == null || lamama.size() < cellsToMerge.getLast().size())
				lamama = cellsToMerge.getLast();
		}

		cellsToMerge.remove(lamama);
		toFusion.remove(lamama.getId());

		for (Cell cell : cellsToMerge) {
			for (String nodeId : cell.eachNodeId()) {
				lamama.addNode(nodeId);
				graph.getNode(nodeId).setAttribute("meta.node", lamama.getId());
			}

			for (String edgeId : cell.eachEdgeId()) {
				Edge edge = graph.getEdge(edgeId);

				lamama.addEdge(edgeId, edge.getSourceNode().getId(), edge
						.getTargetNode().getId(), edge.isDirected());
			}

			cell.clear();

			for (Edge preEdge : cell) {
				MetaEdge oldMetaEdge = (MetaEdge) preEdge;
				MetaNode opposite = (MetaNode) oldMetaEdge.getOpposite(cell);

				if (oldMetaEdge.getOpposite(cell) == lamama) {
					for (String edgeId : oldMetaEdge.eachEdgeId()) {
						Edge edge = graph.getEdge(edgeId);
						edge.removeAttribute("meta.edge");

						lamama.addEdge(edge.getId(), edge.getSourceNode()
								.getId(), edge.getTargetNode().getId(), edge
								.isDirected());
					}
				} else {
					MetaEdge metaEdge = (MetaEdge) lamama
							.getEdgeToward(opposite.getId());

					if (metaEdge == null)
						metaEdge = (MetaEdge) metaGraph.addEdge(String.format(
								"metaedge-%s-%s", lamama.getId(),
								opposite.getId()), lamama.getId(), opposite
								.getId(), false);

					for (String edgeId : oldMetaEdge.eachEdgeId()) {
						Edge edge = graph.getEdge(edgeId);
						metaEdge.addEdge(edgeId);
						edge.setAttribute("meta.edge", metaEdge.getId());
					}

					metaEdge.clear();
				}
			}
		}

		for (String metaNodeId : toFusion)
			metaGraph.removeNode(metaNodeId);

		return lamama;
	}

	protected void checkMitose(Cell cell) {
		if (cell.size() == 0)
			return;

		LinkedList<String> nodeToRetain = new LinkedList<String>();
		LinkedList<String> nodeToExplore = new LinkedList<String>();

		String startNode = null;
		for (String nodeId : cell.eachNodeId()) {
			startNode = nodeId;
			break;
		}

		nodeToExplore.add(startNode);

		while (nodeToExplore.size() > 0) {
			startNode = nodeToExplore.poll();
			nodeToRetain.add(startNode);

			Node node = graph.getNode(startNode);
			Iterator<? extends Node> ite = node.getNeighborNodeIterator();

			while (ite.hasNext()) {
				node = ite.next();

				if (!nodeToExplore.contains(node.getId())
						&& !nodeToRetain.contains(node.getId())) {
					if (cell.hasNode(startNode))
						nodeToExplore.add(startNode);
				}
			}
		}

		if (cell.size() > nodeToRetain.size())
			mitose(cell, nodeToRetain);
	}

	public void mitose(Cell cell, LinkedList<String> nodeToRetain) {
		CellSet cSet = cells.get(cell.metaIndex());
		Cell newCell = cSet.newCell();

		for (String nodeId : cell.eachNodeId()) {
			if (!nodeToRetain.contains(nodeId)) {
				Node node = graph.getNode(nodeId);

				newCell.addNode(nodeId);
				cell.removeNode(nodeId);

				node.setAttribute("meta.node", newCell.getId());
			}
		}

		for (String edgeId : cell.eachEdgeId()) {
			Edge edge = graph.getEdge(edgeId);

			if (nodeToRetain.contains(edge.getSourceNode().getId()) != nodeToRetain
					.contains(edge.getTargetNode().getId())) {
				MetaEdge metaEdge = (MetaEdge) cell.getEdgeToward(newCell
						.getId());

				if (metaEdge == null)
					metaEdge = (MetaEdge) metaGraph.addEdge(String.format(
							"metaedge-%s-%s", cell.getId(), newCell.getId()),
							cell.getId(), newCell.getId(), false);

				metaEdge.addEdge(edgeId);
				cell.removeEdge(edgeId);
			} else if (!nodeToRetain.contains(edge.getSourceNode().getId())) {
				newCell.addEdge(edgeId, edge.getSourceNode().getId(), edge
						.getTargetNode().getId(), edge.isDirected());
				cell.removeEdge(edgeId);
			}
		}

		checkMitose(newCell);
	}

	@Override
	public EdgeFactory getMetaEdgeFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeFactory getMetaNodeFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(MetaGraph metaGraph, Source source) {
		// TODO Auto-generated method stub

	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub

	}
}
