/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.miv.antco2;

/**
 * Listener for AntCo2 events.
 * 
 * <p>Listener reporting changes operated by AntCO² on the graph representation.
 * Changes are reported for all nodes and edges.</p>
 * 
 * <p>The informations given by
 * the two methods {@link #nodeInfos(String, NodeInfos)} and {@link #edgeInfos(String, EdgeInfos)},
 * directly give access to objects internally used by the AntCO² algorithm. This means
 * that modifying the data inside the passed {@link org.miv.antco2.NodeInfos} and
 * {@link org.miv.antco2.EdgeInfos}
 * instances will probably cause very subtle side effects in the algorithm. These instances must be
 * considered <em>read-only</em>. All data that is extracted from them must be copied.</p>
 * 
 * <p>This behaviour has been chosen because node and edge events can be numerous, and copying
 * the node and edge information would incur a large overhead.</p>
 * 
 * @author Antoine Dutot
 */
public interface AntCo2Listener
{
// Commands

	/**
	 * The Last AntCO² iteration finished.
	 * @param time AntCO² time (iteration).
	 * @param metrics Various sensors (is read-only, never change it).
	 */
	void
	stepFinished( int time, final Metrics metrics );
	
	/**
	 * When the time used to complete one step becomes large, AntCO² will
	 * send completion events that gives a percentage of completion under
	 * the form of a number between 0 and 1.
	 * @param time The current step.
	 * @param completionPercent A number between 0 and 1 indicating the completion percent.
	 */
	void
	stepCompletion( int time, float completionPercent );
	
	/**
	 * Informations on a node.
	 * @param tag Node name.
	 * @param infos Node informations (is read-only, never change it).
	 */
	void
	nodeInfos( String tag, final NodeInfos infos );

	/**
	 * Informations on an edge.
	 * @param tag Edge name.
	 * @param infos Edge informations (is read-only, never change it).
	 */
	void
	edgeInfos( String tag, final EdgeInfos infos );
	
	/**
	 * Informations on a colony.
	 * @param name The colony name.
	 * @param index The colony index.
	 * @param population Number of ants in the colony, -1 means the colony disappeared.
	 * @param colony The colony (is read-only, never change it, null means the colony disappeared).
	 */
	void
	colonyInfos( String name, int index, int population, final Colony colony );
	
	/**
	 * Report a problem. 
	 * @param warning Problem message.
	 */
	void
	warning( String warning );
	
	/**
	 * Report an error.
	 * @param error The error message.
	 * @param ex The exception that caused the error if any, else null.
	 * @param isFatal If this error fatal (after a fatal error, the AntCo2
	 * instance will not be able to ensure correct functioning).
	 */
	void
	error( String error, Exception ex, boolean isFatal );
}
