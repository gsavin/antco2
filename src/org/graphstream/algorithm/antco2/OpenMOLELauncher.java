package org.graphstream.algorithm.antco2;

import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.PreferentialAttachmentGenerator;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.ui.swingViewer.Viewer;

public class OpenMOLELauncher {
	public static class Args {
		boolean display;
		boolean randomize;
		int size;
		int iteration;
		int colonies;

		public Args() {
			display = false;
			randomize = true;
			size = 1000;
			iteration = 10000;
			colonies = 3;
		}

		public void parse(String... args) {
			if (args == null)
				return;

			for (int i = 0; i < args.length; i++) {
				if (args[i].matches("^--(enable|disable)-(display|randomize)$")) {
					boolean on = args[i].startsWith("--enable");

					if (args[i].endsWith("display"))
						display = on;
					else if (args[i].endsWith("randomize"))
						randomize = on;
				} else if (args[i]
						.matches("^--(graph-size|iteration|colonies)=\\d+$")) {
					int k = Integer.parseInt(args[i].substring(args[i]
							.indexOf('=') + 1));

					if (args[i].startsWith("--graph-size"))
						size = k;
					else if (args[i].startsWith("--iteration"))
						iteration = k;
					else if (args[i].startsWith("--colonies"))
						colonies = k;
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] toParse) {
		Args args = new Args();
		args.parse(toParse);

		System.out.printf("========%n AntCo2 %n========%n");
		System.out.printf("Usage: java %s options%n",
				OpenMOLELauncher.class.getName());
		System.out
				.printf("where options is : %n\t--(enable|disable)-(display|randomize)%n\t--(graph-size|iteration|colonies)=integer%n");
		System.out
				.printf("display:    %s%nrandomize:  %s%ngraph size: %d%niterations: %d%ncolonies:   %d%n=========%n",
						args.display, args.randomize, args.size,
						args.iteration, args.colonies);

		Generator gen = new PreferentialAttachmentGenerator();
		DefaultGraph g = new DefaultGraph("openmole");
		AntCo2Algorithm antco2 = new AntCo2Algorithm();

		if (args.randomize)
			antco2.context.params.randomize();

		gen.addSink(g);
		antco2.init(g);

		gen.begin();

		String stylesheet = "graph { " + "  fill-color: rgb(40,40,40);"
				+ "  padding: 50px;" + "}" + "node { " + "  size: 15px;"
				+ "  fill-mode: dyn-plain;"
				+ "  fill-color: red,green,blue,yellow,orange,pink,purple;"
				+ "}" + "node .membrane {" + "  size: 15px;" + "}" + "edge {"
				+ "  fill-color: white;" + "}";

		if (args.display) {
			g.addAttribute("ui.stylesheet", stylesheet);
			g.display(true).setCloseFramePolicy(Viewer.CloseFramePolicy.EXIT);
		}

		for (int i = 0; i < args.colonies; i++)
			g.addAttribute("antco2.resources",
					String.format("+ colonies-%03x", i));

		int ite = args.iteration;
		int size = args.size;

		while (size-- > 0) {
			gen.nextEvents();
			antco2.compute();
			ite--;

			if (args.display) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		gen.end();

		while (ite-- > 0) {
			antco2.compute();

			if (args.display) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
