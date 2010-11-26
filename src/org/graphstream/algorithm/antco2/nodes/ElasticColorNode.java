package org.graphstream.algorithm.antco2.nodes;

import java.util.Arrays;

import org.graphstream.algorithm.antco2.AntCo2Node;
import org.graphstream.algorithm.antco2.AntContext;
import org.graphstream.algorithm.antco2.Colony;
import org.graphstream.graph.Graph;

public class ElasticColorNode extends AntCo2Node {
	protected float[] antco2ColorAttraction;

	protected float[] neighColorAttraction;

	public ElasticColorNode(AntContext ctx, Colony colony, Graph g, String id) {
		super(ctx, colony, g, id);

		antco2ColorAttraction = new float[ctx.getColonyCount()];
		neighColorAttraction = new float[ctx.getColonyCount()];
	}

	public void step(AntContext ctx) {
		super.step(ctx);

		for (int i = 0; i < antco2ColorAttraction.length; i++)
			antco2ColorAttraction[i] *= ctx.getAntParams().colorAttractionDecreaseFactor;
	}

	protected void submitColor(AntContext ctx, Colony newColor) {
		checkColorArraySizes(newColor.getIndex());

		antco2ColorAttraction[newColor.getIndex()] *= ctx.getAntParams().colorAttractionFactor;

		antco2ColorAttraction[newColor.getIndex()] = Math.min(1.0f,
				antco2ColorAttraction[newColor.getIndex()]);

		float max;
		int maxIndex;

		if (color == null) {
			max = antco2ColorAttraction[0];
			maxIndex = 0;
		} else {
			max = antco2ColorAttraction[color.getIndex()];
			maxIndex = color.getIndex();
		}

		for (int i = 0; i < antco2ColorAttraction.length; i++) {
			if (antco2ColorAttraction[i] > max) {
				max = antco2ColorAttraction[i];
				maxIndex = i;
			}
		}

		newColor = ctx.getColony(maxIndex);

		if (newColor != color && (membrane || newColor.getNodeCount() == 0))
			setColor(newColor);
	}

	protected void resizeArrays(int newSize) {
		super.resizeArrays(newSize);
		antco2ColorAttraction = Arrays.copyOf(antco2ColorAttraction, newSize);
	}
}
