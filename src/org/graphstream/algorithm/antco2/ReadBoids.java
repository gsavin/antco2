package org.graphstream.algorithm.antco2;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
//import java.awt.GraphicsDevice;
//import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.ProxyPipe;
import org.graphstream.stream.file.FileSinkDGS;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.layout.springbox.SpringBox;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.swingViewer.GraphRenderer;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.basicRenderer.SwingBasicGraphRenderer;

public class ReadBoids {
	public static enum Action {
		READ_AND_ADD_COORD,
		READ_AND_ADD_COORD_3D,
		JUST_READ,
		WRITE_PICTURES,
		COUNT_EVENTS
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Action action = Action.valueOf(args[0]);
		
		switch(action) {
		case READ_AND_ADD_COORD:
			readAndAddCoord(args[1], args[2],false);
			break;
		case READ_AND_ADD_COORD_3D:
			readAndAddCoord(args[1], args[2],true);
			break;
		case JUST_READ:
			justRead();
			break;
		case WRITE_PICTURES:
			writePictures();
			break;
		case COUNT_EVENTS:
			countEvents();
			break;
		}
	}

	protected static class InnerRenderer extends SwingBasicGraphRenderer {
		protected void renderGraph(Graphics2D g) {
			super.renderGraph(g);
		}
	}

	public static void writePictures() {
		GraphicGraph g = new GraphicGraph("boids");
		FileSourceDGS dgs = new FileSourceDGS();

		dgs.addSink(g);

		InnerRenderer renderer = new InnerRenderer();
		renderer.open(g, null);

		// Colors: blue,green,magenta,yellow,red
		g.addAttribute(
				"ui.stylesheet",
				"graph { padding: 50px; fill-color: black; } node { size: 15px; fill-mode: dyn-plain; fill-color: #5782db,#90dd3e,#e069cb,#e0ce69,#e07c69; } edge { fill-color: rgba(255,255,255,100); }");
		g.addAttribute("ui.quality");
		g.addAttribute("ui.antialias");

		try {
			dgs.begin("BoidsMovie.dgs");

			while (g.getEdgeCount() == 0)
				dgs.nextStep();

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			boolean live = true;

			String screenshotPrefix = "videos/boids_";
			int screenshotCount = 1;

			BufferedImage current = new BufferedImage(1920, 1080,
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = current.createGraphics();

			long m1, m2;

			m2 = System.currentTimeMillis();

			while (live) {
				m1 = System.currentTimeMillis();

				g.computeBounds();

				Point3 lo = g.getMinPos();
				Point3 hi = g.getMaxPos();

				renderer.setBounds(lo.x, lo.y, lo.z, hi.x, hi.y, hi.z);

				renderer.render(g2d, 1920, 1080);

				current.flush();
				ImageIO.write(
						current,
						"PNG",
						new File(String.format("%s%05d.png", screenshotPrefix,
								screenshotCount++)));

				System.out.printf(
						"\033[s%d images written (%d ms/image)\033[u",
						screenshotCount - 1, System.currentTimeMillis() - m1);

				live = dgs.nextStep();
			}

			m2 = System.currentTimeMillis() - m2;
			String time = String.format("%d s", m2 / 1000);

			System.out.printf("\033[Kdone in %s%n", time);

			dgs.end();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.exit(0);
	}

	protected static class ResizableView extends DefaultView {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4534471579509816964L;

		int width, height;

		public ResizableView(Viewer viewer, String identifier,
				GraphRenderer renderer) {
			super(viewer, identifier, renderer);

			//GraphicsDevice dev = GraphicsEnvironment
			//		.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			// resize( dev.getDisplayMode().getWidth() / 2,
			// dev.getDisplayMode().getHeight() / 2 );
			resize(1920, 1080);
		}

		@Override
		public void openInAFrame(boolean on) {
			if (on) {
				if (frame == null) {
					frame = new JFrame("L2D Execution Model");
					frame.setLayout(new BorderLayout());
					frame.add(this, BorderLayout.CENTER);
					frame.setSize(width, height);
					frame.setVisible(true);
					frame.addWindowListener(this);
					frame.addKeyListener(shortcuts);
				} else {
					frame.setVisible(true);
				}
			} else {
				if (frame != null) {
					frame.removeWindowListener(this);
					frame.removeKeyListener(shortcuts);
					frame.remove(this);
					frame.setVisible(false);
					frame.dispose();
				}
			}
		}

		public void resize(int width, int height) {
			this.width = width;
			this.height = height;

			if (frame != null)
				frame.setSize(width, height);
		}
	}

	public static void countEvents() {
		FileSourceDGS dgs = new FileSourceDGS();

		try {
			dgs.begin("BoidsMovie.dgs");

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			boolean live = true;
			int count = 0;

			while (live) {
				count++;
				live = dgs.nextEvents();
			}

			System.out.printf("%d events%n", count);

			dgs.end();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.exit(0);
	}

	public static void justRead() {
		DefaultGraph g = new DefaultGraph("boids");
		FileSourceDGS dgs = new FileSourceDGS();

		dgs.addSink(g);

		// Colors: blue,green,magenta,yellow,red
		g.addAttribute(
				"ui.stylesheet",
				"graph { padding: 50px; fill-color: black; } node { fill-mode: dyn-plain; fill-color: #5782db,#90dd3e,#e069cb,#e0ce69,#e07c69; } edge { fill-color: white; }");
		g.addAttribute("ui.quality");
		g.addAttribute("ui.antialias");

		Viewer viewer;// = g.display(false);

		viewer = new Viewer(g, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);

		DefaultView view = new ResizableView(viewer, Viewer.DEFAULT_VIEW_ID,
				Viewer.newGraphRenderer());

		viewer.addView(view);
		view.openInAFrame(true);

		ProxyPipe proxy = viewer.newThreadProxyOnGraphicGraph();
		proxy.addSink(g);

		try {
			dgs.begin("/home/raziel/workspace/build/BoidsMovie.dgs");

			while (g.getEdgeCount() == 0)
				dgs.nextStep();

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			boolean live = true;

			//String screenshotPrefix = "videos/boids_";
			//int screenshotCount = 1;

			while (live) {
				//g.addAttribute("ui.screenshot", String.format("%s%05d.png",
				//		screenshotPrefix, screenshotCount++));

				//while (g.hasAttribute("ui.screenshot")) {
				//	proxy.pump();
					Thread.sleep(100);
				//}

				live = dgs.nextStep();
			}

			//g.addAttribute("ui.screenshot", String.format("%s%05d.png",
			//		screenshotPrefix, screenshotCount++));

			// while( g.hasAttribute("ui.screenshot") )
			// Thread.sleep(100);

			dgs.end();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.exit(0);
	}

	public static void readAndAddCoord(String in, String out, boolean is3d) {
		FileSourceDGS dgs = new FileSourceDGS();
		DefaultGraph g = new DefaultGraph("boids", false, true);
		SpringBox sbox = new SpringBox(is3d);

		FileSinkDGS dgsOut = new FileSinkDGS();

		dgs.addSink(g);

		g.addSink(sbox);
		g.addSink(dgsOut);

		sbox.addSink(g);

		FileInputStream fin = null;
		int size = 0;

		try {
			fin = new FileInputStream(in);
			size = fin.available();
			dgsOut.begin(out);
			dgs.begin(fin);

			while (g.getEdgeCount() == 0)
				dgs.nextStep();

		} catch (Exception e) {
			e.printStackTrace();
		}

		long timeId = 0;
		int i = 0;

		try {
			while (dgs.nextEvents()) {
				if (i % 250 == 0) {
					sbox.compute();
					System.out.printf("\033[s\033[K%d/%d (%d%%)\033[u", size
							- fin.available(), size,
							100 * (size - fin.available()) / size);
					g.stepBegins("manual", timeId++, g.getStep() + 1);
				}

				i++;
				// Thread.sleep(100);
			}

			dgs.end();
			dgsOut.end();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
