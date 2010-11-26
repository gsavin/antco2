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

import org.graphstream.algorithm.antco2.Ant;
import org.graphstream.algorithm.antco2.AntCo2Node;
import org.graphstream.algorithm.antco2.AntContext;
import org.graphstream.algorithm.antco2.AntFactory;
import org.graphstream.algorithm.antco2.Colony;

public class AgoraphomyrmexColony extends Colony {
	class AgoraphomyrmexFactory implements AntFactory {
		public Ant newAnt(String id, AntCo2Node start) {
			return new Agoraphomyrmex(id, AgoraphomyrmexColony.this, start, ctx);
		}
	}

	public AgoraphomyrmexColony() {
		antFactory = new AgoraphomyrmexFactory();
	}

	public AgoraphomyrmexColony(AntContext context, String name, int index) {
		super(context, name, index);
		antFactory = new AgoraphomyrmexFactory();
	}
}
