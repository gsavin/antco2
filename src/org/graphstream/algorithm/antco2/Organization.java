/*
 * This file is part of AntCo2.
 * 
 * AntCo2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * AntCo2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with AntCo2.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2009 - 2010
 * 	Antoine Dutot
 * 	Guilhelm Savin
 */
package org.graphstream.algorithm.antco2;

import java.util.HashSet;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.Sink;

public class Organization implements Sink {
	String organizationAttribute;
	String organizationComponentAttribute;

	HashSet<String> entities;
	Graph graph;
	Object organizationTag;
	Object organizationComponentTag;

	public void entityTagChanged(String nodeId, Object newTag) {
		//
		// If entities is already in the organization ...
		//
		if (entities.contains(nodeId)) {
			//
			// ... and if its organization tag is not its of the organization,
			// so entity is leaving the organization.
			//
			if (!organizationTag.equals(newTag))
				entityLeavesOrganization(nodeId);
		}
		//
		// ... or if entity is not in the organization but
		// its tag is that of the organization ...
		//
		else if (organizationTag.equals(newTag)) {
			//
			// ... we have to check is this entity is connected to a member of
			// this organization ...
			//
			Node node = graph.getNode(nodeId);
			boolean flag = false;

			for (Edge e : node.getEdgeSet()) {
				if (entities.contains(e.getOpposite(node).getId())) {
					flag = true;
					break;
				}
			}
			//
			// ... in this case, entity joins the organization.
			//
			if (flag)
				entityJoinsOrganization(nodeId);
		}
	}

	public void entityLeavesOrganization(String nodeId) {

	}

	public void entityJoinsOrganization(String nodeId) {

	}

	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId,
			String attribute, Object value) {
		if (attribute.equals(organizationAttribute))
			entityTagChanged(nodeId, value);
	}

	public void nodeAttributeChanged(String sourceId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
		if (attribute.equals(organizationAttribute))
			entityTagChanged(nodeId, newValue);
	}

	public void nodeAttributeRemoved(String sourceId, long timeId,
			String nodeId, String attribute) {
		if (attribute.equals(organizationAttribute))
			entityTagChanged(nodeId, null);
	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		if (entities.contains(nodeId)) {

		}
	}

	public void edgeRemoved(String sourceId, long timeId, String edgeId) {

	}

	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {

	}

	public void graphAttributeAdded(String sourceId, long timeId,
			String attribute, Object value) {
		// TODO Auto-generated method stub

	}

	public void graphAttributeChanged(String sourceId, long timeId,
			String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub

	}

	public void graphAttributeRemoved(String sourceId, long timeId,
			String attribute) {
		// TODO Auto-generated method stub

	}

	public void edgeAttributeAdded(String sourceId, long timeId, String edgeId,
			String attribute, Object value) {
		// TODO Auto-generated method stub

	}

	public void edgeAttributeChanged(String sourceId, long timeId,
			String edgeId, String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub

	}

	public void edgeAttributeRemoved(String sourceId, long timeId,
			String edgeId, String attribute) {
		// TODO Auto-generated method stub

	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		// TODO Auto-generated method stub

	}

	public void graphCleared(String sourceId, long timeId) {
		// TODO Auto-generated method stub

	}

	public void stepBegins(String sourceId, long timeId, double step) {
		// TODO Auto-generated method stub

	}
}
