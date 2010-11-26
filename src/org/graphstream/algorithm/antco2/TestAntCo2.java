package org.graphstream.algorithm.antco2;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

import org.graphstream.graph.meta.MetaGraph;
import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.GridGenerator;
import org.graphstream.algorithm.generator.PointsOfInterestGenerator;
import org.graphstream.algorithm.generator.PreferentialAttachmentGenerator;
import org.graphstream.algorithm.generator.PointsOfInterestGenerator.Parameter;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.ProxyPipe;
import org.graphstream.stream.file.FileSinkDGS;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.ViewerPipe;

import static org.graphstream.algorithm.antco2.Parameter.parameter;

public class TestAntCo2 {
	public static void generatePOI(String path, int step) {
		ProgressDisplay pDisplay = new ProgressDisplay(0, step);

		PointsOfInterestGenerator gen = new PointsOfInterestGenerator();

		gen.setParameter(Parameter.INITIAL_PEOPLE_COUNT, 500);
		gen.setParameter(Parameter.ADD_PEOPLE_PROBABILITY, 0.01f);
		gen.setParameter(Parameter.DEL_PEOPLE_PROBABILITY, 0.01f);
		gen.setParameter(Parameter.INITIAL_POINT_OF_INTEREST_COUNT, 30);
		gen.setParameter(Parameter.AVERAGE_POINTS_OF_INTEREST_COUNT, 4.0f);
		gen.setParameter(Parameter.ADD_POINT_OF_INTEREST_PROBABILITY, 0.0f);
		gen.setParameter(Parameter.DEL_POINT_OF_INTEREST_PROBABILITY, 0.0f);
		gen.setParameter(Parameter.HAVE_INTEREST_PROBABILITY, 0.1f);
		gen.setParameter(Parameter.LOST_INTEREST_PROBABILITY, 0.001f);
		gen.setParameter(Parameter.LINKS_NEEDED_TO_CREATE_EDGE, 2);
		gen.setParameter(Parameter.LINK_PROBABILITY, 0.05f);

		FileSinkDGS dgs = new FileSinkDGS();

		try {
			gen.addSink(dgs);
			dgs.begin(path);

			gen.begin();

			int i = 0;

			while (i++ < step) {
				gen.nextEvents();
				pDisplay.setProgress(i);
			}

			gen.end();

			dgs.end();
		} catch (Exception e) {
			e.printStackTrace();
		}

		pDisplay.end();
	}

	public static void generate(Generator gen, String path, int step) {
		ProgressDisplay pDisplay = new ProgressDisplay(0, step);

		FileSinkDGS dgs = new FileSinkDGS();

		try {
			gen.addSink(dgs);
			dgs.begin(path);

			gen.begin();

			int i = 0;

			while (i++ < step) {
				gen.nextEvents();
				pDisplay.setProgress(i);
			}

			gen.end();

			dgs.end();
		} catch (Exception e) {
			e.printStackTrace();
		}

		pDisplay.end();
	}

	public static void main(String[] args) {
		// generate("test-poi-graph.dgs",5000);
		// antCo2("test-poi-graph.dgs");
		antCo2OnGrid(new GridGenerator(), 20, 5000);
	}

	public static void antCo2OnGrid(Generator gen, int size, int step) {
		ProgressDisplay pDisplay = new ProgressDisplay(0, step);

		Graph g = new DefaultGraph("theGraph");
		AntCo2Algorithm antco2 = new AntCo2Algorithm();

		// antco2.init(g);
		antco2.init(
				parameter("graph", g),
				parameter("antco2.params.smoothingBoxPolicy",
						AntParams.SmoothingBoxPolicy.COHESION));

		g.display(false);

		String stylesheet = "graph { " + "  fill-color: white;"
				+ "  padding: 50px;" + "}" + "node { "
				+ "  fill-mode: dyn-plain;"
				+ "  fill-color: red,green,blue,yellow,orange,pink,purple;"
				+ "  size: 20px;" + "}" + "node .membrane {" + "}" + "edge {"
				+ "  fill-color: black;" + "}";

		g.addAttribute("ui.stylesheet", stylesheet);
		g.addAttribute("ui.quality");
		// g.addAttribute( "ui.antialias" );

		g.addAttribute("antco2.resources", "+ A");
		g.addAttribute("antco2.resources", "+ B");
		g.addAttribute("antco2.resources", "+ C");
		g.addAttribute("antco2.resources", "+ D");
		g.addAttribute("antco2.resources", "+ E");

		gen.addSink(g);
		gen.begin();
		for (int i = 0; i < size; i++)
			gen.nextEvents();
		gen.end();

		for (int i = 0; i < step; i++) {
			antco2.compute();
			pDisplay.setProgress(i);

			try {
				Thread.sleep(6);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		pDisplay.end();
	}

	public static void antCo2(String path) {
		ProgressDisplay pDisplay = new ProgressDisplay(0, 1);

		Graph g = new DefaultGraph("theGraph");
		AntCo2Algorithm antco2 = new AntCo2Algorithm();

		antco2.init(g);

		g.addAttribute("antco2.resources", "+ A");
		g.addAttribute("antco2.resources", "+ B");
		g.addAttribute("antco2.resources", "+ C");
		g.addAttribute("antco2.resources", "+ D");
		g.addAttribute("antco2.resources", "+ E");

		FileSourceDGS dgs = new FileSourceDGS();

		dgs.addSink(g);
		/*
		 * g.display();
		 * 
		 * String stylesheet = "graph { " + "  fill-color: black;" +
		 * "  padding: 50px;" + "}" + "node { " + "  fill-mode: dyn-plain;" +
		 * "  fill-color: red,green,blue,yellow,orange,pink,purple;" + "}" +
		 * "node .membrane {" + "}" + "edge {" + "  fill-color: white;" + "}";
		 * 
		 * g.addAttribute( "ui.stylesheet", stylesheet ); g.addAttribute(
		 * "ui.quality" ); //g.addAttribute( "ui.antialias" );
		 */
		try {
			InputStream in = new FileInputStream(path);

			int bytes = in.available();

			pDisplay.setBornes(0, bytes);

			dgs.begin(in);

			while (dgs.nextStep()) {
				antco2.compute();
				pDisplay.setProgress(bytes - in.available());
			}

			dgs.end();
		} catch (Exception e) {
			e.printStackTrace();
		}

		pDisplay.end();
	}

	public static class ProgressDisplay extends JFrame {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1341740836835795035L;

		JProgressBar bar;

		public ProgressDisplay(int str, int end) {
			super("progress");
			bar = new JProgressBar(str, end);

			add(bar);
			pack();

			setVisible(true);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}

		public void setBornes(int min, int max) {
			bar.setMinimum(min);
			bar.setMaximum(max);
		}

		public void setProgress(int i) {
			bar.setValue(i);
			bar.repaint();
		}

		public void end() {
			setVisible(false);
			dispose();
		}
	}

}
