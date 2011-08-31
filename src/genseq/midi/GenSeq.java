/***
 * GenSeq.java
 * 
 * Handles I/O, does the rendering for GenSeqScores, plays
 * MIDI events.
 * 
 * 
 */

package genseq.midi;

import genseq.gui.*;
import genseq.obj.*;
import processing.core.*;
import java.awt.AWTEvent;
import java.awt.PopupMenu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;

@SuppressWarnings("serial")
public class GenSeq extends PApplet implements ActionListener, MouseListener, MouseMotionListener {
	
	/*** PROGRAM NAME AND VERSION ***/
	public static final String NAME = "Graphikal";
	public static final double VERSION = 0.1;
			
	/*** BASIC LOOK & FEEL CONSTANTS ***/
	private static final int DEFAULT_STROKE_RED = 200;
	private static final int DEFAULT_STROKE_GREEN = 200;
	private static final int DEFAULT_STROKE_BLUE = 255;
	private static final int DEFAULT_STROKE_WEIGHT = 2;
	private static final int DEFAULT_WIDTH = 600;
	private static final int DEFAULT_HEIGHT = 600;
		
	/*** GUI COMPONENTS ***/
	private static PopupMenu contextMenu;
	
	/*** INTERNAL CONTROL ***/
	// How accurate does the user have to be with the mouse in order
	// to select an item (in pixels)?
	private static final int CLICK_ACCURACY = 20;
	// Tells us which score is currently active / being worked on.
	private static int activeScore;
	// Various editing modes that can be selected in GenSeqToolWindow
	private static int currentMode;
	public static final int CREATE_NODES = 0;
	public static final int MOVE_NODES = 1;
	// List of scores the user can draw on.
	private static ArrayList<Score> scores;
	// Previous x, y position of a mouse click, respectively
	private static int prevX, prevY;
	
	
	/*** MIDI CONTROL ***/
	public static final double TEMPO = 120.0;
	public static final int SMALL_SUBDIVISION = 64;
	// number_small_subdivisions * TIME_EDGE_RATIO = edge_pixels_traversed
	// 12.5 indicates sixteen 64th notes (1 quarter note) is 200 pixels.
	// 3.125 indicates sixteen 64th notes (1 quarter note) is 50 pixels.
	public static final double TIME_EDGE_RATIO = 3.125; 
	private static ArrayList<ScoreTraverser> traversers;
	private static Timer timer;
	
	/*** EXTERNAL REFERENCES ***/
	// Reference to the Frame hosting this PApplet
	@SuppressWarnings("unused")
	private static GenSeqWindow parent;
		
	
	/*************
	 * Init. stuff
	 *************/
	
	/**
	 * CONSTRUCTOR.
	 * 
	 * Do any allocation of globally-accessible memory necessary.
	 */
	public GenSeq(GenSeqWindow parent) {
		GenSeq.parent = parent;
		
		constructComponents();
	}

	/**
	 * constructComponents()
	 * 
	 * Allocate space for and initialize any non-Processing GUI
	 * components.
	 */
	private void constructComponents() {
		scores = new ArrayList<Score>();
		
		Score gsc = new Score(this);
		scores.add(gsc);
		
		activeScore = 0;
		
		// Set up GUI components (non-Processing)
		contextMenu = new PopupMenu("Edit");
		
		MenuItem pmDelete = new MenuItem("Delete node");
		pmDelete.addActionListener(new pmDeleteActionListener());
		MenuItem pmAttr	= new MenuItem("Node attributes...");
		pmAttr.addActionListener(new pmAttributesActionListener());
		MenuItem pmColor = new MenuItem("Set color");
		
		contextMenu.add(pmDelete);
		contextMenu.add(pmAttr);
		contextMenu.add(pmColor);
		
		add(contextMenu);
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
	}
	
	/**
	 * setup()
	 * 
	 * Do any setup necessary for starting the Processing applet.
	 */
	public void setup() {
		size(DEFAULT_WIDTH,
				DEFAULT_HEIGHT,
				JAVA2D);
		
		smooth();
	}
	
	/**
	 * setMode(int mode) - change the current editing mode to `mode'
	 * This is called from GenSeqToolWindow.
	 * 
	 * @param mode - one of the mode constants defined on GenSeq
	 */
	public void setMode(int mode) {
		currentMode = mode;
	}
	
	public int getMode() {
		return currentMode;
	}
	
	
	/***********
	 * LISTENERS
	 ***********/
	
	/**
	 * actionPerformed(ActionEvent e)
	 * 
	 * Handles operations on the context menus.
	 */
	public void actionPerformed(ActionEvent e) {
		
		// Forward the event to the active score
		scores.get(activeScore).actionPerformed(e);
		
	}

	/**
	 * mouseClicked()
	 * 
	 * Handles click events.
	 */
	public void mouseClicked(MouseEvent me) {
		
		scores.get(activeScore).mouseClicked(me);
		
	}
	
	/**
	 * mousePressed()
	 * 
	 * Handles mouseDown events.
	 */
	public void mousePressed(MouseEvent me) {
		
		// Mouse triggers can happen at the time of the mouse being
		// pressed or released on different platforms. We check it in both.
		if (me.isPopupTrigger())
			contextMenu.show(this, me.getX(), me.getY());
		
		prevX = me.getX();
		prevY = me.getY();
		
		scores.get(activeScore).mousePressed(me);
	}
	
	/**
	 * mouseReleased()
	 * 
	 * Handles mouse released events.
	 */
	public void mouseReleased(MouseEvent me) {
		
		// Mouse triggers can happen at the time of the mouse being
		// pressed or released on different platforms. We check it in both.
		if (me.isPopupTrigger())
			contextMenu.show(this, me.getX(), me.getY());
		
		scores.get(activeScore).mouseReleased(me);
		
	}
	
	/**
	 * mouseDragged()
	 * 
	 * This version seems to work better for PApplets. This one just
	 * draws lines between nodes when the mouse is being dragged.
	 */
	public void mouseDragged() {
		
		if (currentMode == CREATE_NODES && mouseButton == LEFT) {
			if (distance(mouseX, mouseY, prevX, prevY) >= CLICK_ACCURACY) {	
				strokeWeight(DEFAULT_STROKE_WEIGHT);
				stroke(DEFAULT_STROKE_RED, DEFAULT_STROKE_GREEN, DEFAULT_STROKE_BLUE);

				line(prevX, prevY, mouseX, mouseY);
			}
		}
		
		scores.get(activeScore).mouseDragged(mouseX, mouseY);
		
	}
	

	
	
	
	/*******************************************************
	 * DRAW LOOP.
	 * 
	 * Called at every tick and updates graphics.
	 ******************************************************/
	public void draw() {		

		scores.get(activeScore).render();
	
	}

	
	/**************************
	 * 
	 * MIDI CONTROL / PLAYBACK
	 * 
	 **************************/
	
	
	/**
	 * play() - Begin playback of the generative score.
	 **/
	public void playScore() {
		// Calculate time duration of the smallest subdivision (milliseconds)
		long sdtime = (long)((60.0 / TEMPO) / (SMALL_SUBDIVISION / 4.0) * 1000);
		
		// Dubugging message
		System.out.println("Duration of "+SMALL_SUBDIVISION+" note: "+sdtime);
		
		traversers = new ArrayList<ScoreTraverser>();
		ArrayList<Node> primeNodes = new ArrayList<Node>();
		
		// Find all the prime nodes
		for (Node n : scores.get(activeScore).getNodes()) {
			if (n.isPrimeNode())
				primeNodes.add(n);
		}
		
		// TODO: Create a ScoreTraverser for each prime node
		// Each ScoreTraverser runs on its own thread. Is this a good idea?
		for (Node p : primeNodes) {
			ScoreTraverser t = new ScoreTraverser(this, p);
			t.start();
			traversers.add(t);
		}
		
		// Create the timer
		timer = new Timer();
		timer.schedule(new ScoreTraverserConductor(), 0, sdtime);
		
	}
	
	/**
	 * stop() - Stop all playback.
	 **/
	public void stopScore() {

		try {
			for (ScoreTraverser t : traversers) {
				t.interrupt();

			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		timer.cancel();
		traversers.clear();
		
		for (Node n : scores.get(activeScore).getNodes())
			n.refresh();
		
	}

	
	
	/*****************
	 * PRIVATE METHODS
	 *****************/
	
	private double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 -y2, 2));
	}
	
	
	
	
	/*******************
	 * INTERNAL CLASSES
	 ******************/
	
	private class pmDeleteActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			
			scores.get(activeScore).removeNode(prevX, prevY);
			
		}
		
	}
	
	private class pmAttributesActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			Node target = scores.get(activeScore).findNodeAtPoint(prevX, prevY);
			
			if (null != target) {
				PApplet nawParent = (PApplet)((PopupMenu)((MenuItem)e.getSource()).getParent()).getParent();
				NodeAttributesWindow naw = new NodeAttributesWindow(nawParent, target);
			}
		}
	}

	private class ScoreTraverserConductor extends TimerTask {
		
		public void run() {
			
			// Sanity check
			if (null != traversers) {
				for (ScoreTraverser t : traversers) {
					t.tick();
				}
			}
			
		}
		
	}
	
}