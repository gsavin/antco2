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

package org.graphstream.algorithm.antco2.species;

import java.util.LinkedList;

import org.graphstream.algorithm.antco2.Ant;
import org.graphstream.algorithm.antco2.AntCo2Edge;
import org.graphstream.algorithm.antco2.AntCo2Node;
import org.graphstream.algorithm.antco2.AntContext;
import org.graphstream.algorithm.antco2.AntParams;
import org.graphstream.algorithm.antco2.Colony;

/**
 * Vincomyrmex.
 *
 * Changements par rapport a la fourmi décrite dans les articles.
 * 	- La fourmi ne considère que les proportions de PH sur les arcs, pas les valeurs.
 *  - La fourmi considère qu'un arc sans phéromones est très attractif: politique d'exploration.
 *  - La fourmi saute si elle détecte que tous les arcs autours d'elle sont surpeuplés.
 *
 * @author Antoine Dutot
 * @since 15 août 2005
 */
public class Vincomyrmex
	extends Ant
{
// Attributes
	
	/**
	 * True if the ant found at least one overpopulated node at the last step.
	 */
	protected boolean encounteredSurpop = false;
	
	/**
	 * Ant memory.
	 */
	protected LinkedList<AntCo2Node> mem = new LinkedList<AntCo2Node>();
	
	/**
	 * Perceived pheromone array. This should be alloced at each call to step(),
	 * but to avoid such an overhead, an over-sized array is created and only
	 * re-allocated when it is too small.
	 */
	protected float P[] = new float[30];

//Constructors

	public Vincomyrmex( String id, Colony colony, AntCo2Node startNode, AntContext context )
	{
		super( id, colony, startNode, context );
	}

//Accessors

	@Override
	public float getPheromonDrop()
	{
		return 0.1f;
	}
	
	/**
	 * Is node in the ant memory?.
	 * @param node The node to test.
	 * @return True if the ant remembers the node.
	 */
	protected boolean isRemembered( AntCo2Node node )
	{
		return mem.contains( node );
	}

	/**
	 * Multiplier for one edge importance value. The mutliplier is 1 except if
	 * the node at the other end of the edge has already been visited recently
	 * (is in the ant memory), or if the node at the other end of the edge is
	 * overpopulated. This is the Eta and Gamma values in the AntCO² algorithm.
	 * @param edge The edge to evaluate.
	 * @return The mutliplier.
	 */
	protected float correction( AntCo2Edge edge )
	{
		AntCo2Node next = (AntCo2Node) edge.getOpposite( curNode );

		if( isRemembered( next ) )
		{
			return 0.000001f;
		}
		
		if( next.getTotalAntCount() > ctx.getAntParams().overPopulated )
		{
			encounteredSurpop = true;
			return 0.000001f;
		}
		
		return 1;
	}
	
	/**
	 * Pheromon of the ant color on the given edge mutliplied by
	 * the pheromon proportion compared to other colors on this edge. 
	 * @param edge The edge to get the value for.
	 * @return The value.
	 */
	protected float correctedPheromonValue( AntCo2Edge edge )
	{
		float Dc;		// Pheromone level for the ant's color.
		float D; 		// Total pheromone level.
		float Kc;		// Proportion of the ant's phromone color.
		float ph = 1f;	// Seems to be a VERY good idea: if the pheTotal is very small, make it larger! The ant will become an explorer/conqueror!
//		float ph = 0.0001f;
	
		Dc = edge.getPheromon( colony.getIndex() );
		D  = edge.getPheromonTotal();
		
		if( D > 0.00001f )
		{
			Kc = Dc / D;
		
			//System.err.printf( "COPH Dc=%f D=%f Kc=%f %n", Dc, D, Kc );
		
		//	ph = Kc * Dc;// + Kc * getPheromonDrop();
			ph = Kc;
			
			if( Float.isInfinite( ph ) )
			{
				System.err.printf( "INFINITE PH (Dc=%f D=%f Kc=%f ph=%f)%n", Dc, D, Kc, ph );
				//System.exit( 1 );
				return ph = 0.0001f;
			}
		}
		
		return ph;
	}
	
//Commands

	@Override
	public void step()
	{
		encounteredSurpop = false;
	
		int   nArcs  = curNode.getDegree();
		float totalP = 0;
		float totalD = 0;
		float totalC = 0;
		int   totalS = 0;	// Total surpop.
		AntCo2Edge  next   = null;
		AntParams params = ctx.getAntParams();

		if( P.length <= nArcs )
			P = new float[nArcs];
		
//		int curPop   = ((NodeInfos)curNode.getAttribute( NodeInfos.ATTRIBUTE_NAME )).getAntCountForColor( color );
//		boolean jump = false;
//
//		if( curPop > params.overPopulated )
//		{
//			jump = ( ctx.random.nextFloat() > 0.8 ) ? true : false;
//		}
		
		if( nArcs <= 0 )
		{
			jumpRandomly();
		}
//		else if( jump )
//		{
//			if( params.jump > 1 )
//			     jumpFarAway( params.jump );
//			else jumpRandomly();		
//		}
		else
		{
			if( params.debug )
				System.out.printf( "Node %s:%n", curNode.getId() );
			
			for( int i=0; i<nArcs; i++ )
			{
				AntCo2Edge      edge  = (AntCo2Edge) curNode.getEdge( i );
				
				P[i] = (float) Math.pow( correctedPheromonValue( edge ), params.alpha )
				     * (float) Math.pow( edge.getValue(), params.beta )
				     * correction( edge );
			
				if( params.debug )
				{
					System.out.printf( "    P[%d]=%f (cor=%f ph=%f, w=%f)%n", i, P[i], correction( edge ),
							(float) Math.pow( correctedPheromonValue( edge ), params.alpha ),
							(float) Math.pow( edge.getValue(), params.beta ) );
				}
				
				if( params.perColorOverpop )
				     totalS += ((AntCo2Node)edge.getOpposite( curNode )).getAntCountForColor( colony );
				else totalS += ((AntCo2Node)edge.getOpposite( curNode )).getTotalAntCount();
				totalP += P[i];
				totalD += edge.getPheromon( colony.getIndex() );
				totalC += edge.getPheromonTotal();
			}
			
			if( params.perColorOverpop )
			     totalS += ((AntCo2Node)curNode).getAntCountForColor( colony );
			else totalS += ((AntCo2Node)curNode).getTotalAntCount();
			
			totalS /= ( nArcs + 1 );
			
			float KK = totalD / totalC;
			
			if( params.jump > 0 && KK < params.agoraphobia )
			{
				if( params.debug )
					System.out.printf( "    -> JUMP[%s] (KK=%f (%f/%f) AGORAPH=%f)%n", getId(), KK, totalD, totalC, params.agoraphobia );
				
				if( params.jump > 1 )
				     jumpFarAway( params.jump );
				else jumpRandomly();
			}
			else if( totalS > nArcs * params.overPopulated )
			{
				if( params.debug )
					System.err.printf( "    -> JUMP[%s] for overpopulation%n", getId() );
				
				if( params.jump > 1 )
				     jumpFarAway( params.jump );
				else jumpRandomly();
				//jumpFarAway( 5 );
			}
			else
			{
				float rp = ctx.random().nextFloat();
				float ct = 0;
				float pa = 0;
				
				if( totalP <= 0 )
				{
					if( params.debug )
						System.out.printf( "    -> no pheromone -> JUMP%n" );
					
					//jumpRandomly();
					jumpFarAway( 1 );	// Not so far.
				}
				else
				{
					int choosed = -1;
					
					for( int i=0; i<nArcs; i++ )
					{
						AntCo2Edge edge = (AntCo2Edge) curNode.getEdge( i );
						pa        = P[i] / totalP;
						ct       += pa;
						
						if( ct >= rp )
						{
							choosed = i;
							next = edge;
							break;
						}
					}
					
					if( next == null )
					{
						if( ct < rp )
							next = (AntCo2Edge) curNode.getEdge( nArcs - 1 );
					}

					//assert ct == 1 : "sum of all edge probabilities is not 1!! (" + ct + ")";
					
					if( next == null )
						throw new RuntimeException( "AntCO² [step="+ctx.getCurrentStep()+"]: no edge choosen above the " + nArcs + " possible edges! (totalP="+totalP+" rand="+rp+")" );

					if( params.debug )
						System.out.printf( "    -> choosed edge %d (P=%f)%n", choosed, P[choosed] );
					
					cross( next, true );
				}			
			}
		}
		
		if( encounteredSurpop )
		{
			ctx.incrSurpop();
		}
	}
	
	/**
	 * Redefine the method to add the new current node in the ant memory (if memory is enabled).
	 * @param edge The edge to cross.
	 * @param depositPheromon Does pheromon is to be deposited on the crossed edge?.
	 */
	public void cross( AntCo2Edge edge, boolean depositPheromon )
	{
		super.cross( edge, depositPheromon );
		remember( curNode );
	}

	/**
	 * Add the given node to the ant memory.
	 * @param node The node to remember.
	 */
	protected void remember( AntCo2Node node )
	{
		mem.addLast( node );
		
		if( mem.size() > ctx.getAntParams().mem )
			mem.removeFirst();
	}
}