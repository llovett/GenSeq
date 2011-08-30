package genseq.gui;

import genseq.midi.GenSeq;
import java.util.Enumeration;
import java.util.Hashtable;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Image;
import javax.imageio.*;
import java.io.File;
import java.io.IOException;
import java.awt.AWTEventMulticaster;

@SuppressWarnings("serial")
public class ToolWindow extends Frame {

	/*** BASIC LOOK & FEEL CONSTANTS ***/
	private static final int DEFAULT_WIDTH = 200;
	private static final int DEFAULT_HEIGHT = 300;
	private static final int COMPONENT_GAP = 10;
	
	/*** EXTERNAL REFERENCES ***/
	// The JFrame that hosts the primary application
	private static GenSeqWindow parent;
	// The primary application (a PApplet)
	private static GenSeq gsApplication;
	
	/*** GUI COMPONENTS ***/
	private static Hashtable<String, PictureButton> buttons;
	
	
	public ToolWindow(GenSeq gsApplication, GenSeqWindow parent) {
		super();
		
		ToolWindow.gsApplication = gsApplication;
		ToolWindow.parent = parent;
		
		buttons = new Hashtable<String, PictureButton>();
		
		init();
	}

	public void init() {
		// Allow the user to close the window
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				setVisible(false);
			}
		});
		
		// Basic window setup
		setResizable(false);
		setLayout(new FlowLayout());
		setTitle("Tools");
		
		// Add the buttons
		addButton("/Users/lukelovett/Documents/workspace/GenSeq/src/img/move.png", "move", new MoveButtonListener());
		addButton("/Users/lukelovett/Documents/workspace/GenSeq/src/img/node.png", "node", new NodeButtonListener());
		
		// Default button
		selectButton("node");
		
		pack();
	}
	
	private void addButton(String imgPath, String command, ActionListener al) {
		PictureButton newButton = new PictureButton(imgPath, command);
		newButton.addActionListener(al);
		add(newButton);
		buttons.put(command, newButton);
	}
	
	private void addButton(String imgPath, String command) {
		addButton(imgPath, command, null);
	}
	
	public static void clearButtons() {
		Enumeration<String> e = buttons.keys();
		while (e.hasMoreElements()) {
			PictureButton curr = buttons.get(e.nextElement());
			curr.deselect();
			curr.repaint();
		}
		
	}

	public static void selectButton(String command) {
		PictureButton button = buttons.get(command);
		button.select();
		button.repaint();	
	}

	/*******************
	 * INTERNAL CLASSES
	 ******************/

	private class PictureButton extends Canvas {

		private static final int BORDER_WIDTH = 8;

		private int width, height;
		private Image img;
		private String command;
		private boolean selected;
		transient ActionListener al;

		public PictureButton(String fname, String command) {
			File picFile = new File(fname);

			try {
				this.img = ImageIO.read(picFile);
			} catch (IOException e) {
				System.err.println("FATAL: Could not load image: "+fname);
				System.exit(1);
			}

			this.command = command;

			prepareImage(img, this);

			width = img.getWidth(this);
			height = img.getHeight(this);

			addMouseListener(new PictureButtonListener());

			al = null;
			selected = false;
		}
		
		public void deselect() {
			selected = false;
		}
		
		public void select() {
			selected = true;
		}

		public void paint(Graphics g) {
			paintShadow(! selected);
		}

		private void triggerActionEvent() {
			ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command);

			al.actionPerformed(ae);
		}

		protected void paintShadow(boolean raised) {
			Graphics g = getGraphics();
			Shape s = g.getClip();
			Image dbi;
			Graphics dbg;
			Color bg = getBackground();
			Dimension d = getSize();
			int dx;
			int dy;

			dbi = ImageCache.getImage(this, d.width, d.height);
			dbg = dbi.getGraphics();
			dbg.setClip(s);
			dbg.setColor(bg);
			dx = d.width - width;
			dy = d.height - height;
			dbg.clearRect(0, 0, d.width, d.height);
			dbg.fill3DRect(1, 1, d.width - 2, d.height - 2, raised);
			dbg.drawImage(img, dx / 2, dy / 2, this );
			g.drawImage(dbi, 0, 0, this );
		}

		public boolean imageUpdate(Image img, int flaginfo, int x, int y, int width, int height) {

			width = img.getWidth(this);
			height = img.getHeight(this);

			Container parent = getParent();
			if (parent != null)
				parent.doLayout();

			return super .imageUpdate(img, flaginfo, x, y, width, height);
		}

		public void addActionListener(ActionListener al) {
			this.al = AWTEventMulticaster.add(this.al, al);
		}

		@SuppressWarnings("unused")
		public void removeActionListener(ActionListener al) {
			this.al = AWTEventMulticaster.remove(this.al, al);
		}

		public Dimension getMinimumSize() {
			return new Dimension(width + BORDER_WIDTH, height + BORDER_WIDTH);
		}

		public Dimension getPreferredSize() {
			return new Dimension(width + BORDER_WIDTH, height + BORDER_WIDTH);
		}

		private class PictureButtonListener extends MouseAdapter {

			public void mousePressed(MouseEvent me) {
				paintShadow(false);
			}

			public void mouseReleased(MouseEvent me) {
				paintShadow(true);
			}

			public void mouseClicked(MouseEvent me) {
				if (null != al)
					triggerActionEvent();
			}

		}

	}

	
	/**
	 * ActionListeners used by the PictureButtons
	 */
	
	private class NodeButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent ae) {
			gsApplication.setMode(GenSeq.CREATE_NODES);
			
			// Show this button as selected and all others as deselected
			ToolWindow.clearButtons();
			ToolWindow.selectButton("node");
		}
		
	}
	
	private class MoveButtonListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			gsApplication.setMode(GenSeq.MOVE_NODES);
			
			ToolWindow.clearButtons();
			ToolWindow.selectButton("move");
		}
		
	}
}
