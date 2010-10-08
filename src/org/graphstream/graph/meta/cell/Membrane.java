package org.graphstream.graph.meta.cell;

import org.graphstream.algorithm.antco2.AntCo2Node;
import org.graphstream.algorithm.antco2.Colony;

import java.util.LinkedList;

public class Membrane
	extends LinkedList<AntCo2Node>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1253159951511368750L;

	Colony colony;
	
	public void step()
	{
		if( size() <= 0 )
			return;
		
		if( size() == 1 )
		{
			colony = get(0).getColor();
		}
		else
		{
			AntCo2Node node = null;
			
			for( int i = 0; i < size(); i++ )
			{
				if( get(i).getColor() == colony )
				{
					node = get(i);
					break;
				}
			}
			
			if( node != null )
			{
				
			}
		}
	}
}
