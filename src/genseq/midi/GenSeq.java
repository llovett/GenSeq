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
import java.util.Collections;
import java.util.LinkedList;
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
	private static PopupMenu nodePopupMenu;
	private static PopupMenu edgePopupMenu;
	private static PopupMenu generalPopupMenu;
	private static PopupMenu circuitPopupMenu;
	
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
		nodePopupMenu = new PopupMenu("Node");
		edgePopupMenu = new PopupMenu("Edge");
		generalPopupMenu = new PopupMenu("Edit");
		circuitPopupMenu = new PopupMenu("Circuit");
		
		// Node popup menu
		MenuItem pmDeleteNode = new MenuItem("Delete node");
		pmDeleteNode.addActionListener(new pmDeleteNodeActionListener());
		MenuItem pmNodeAttr	= new MenuItem("Node attributes...");
		pmNodeAttr.addActionListener(new pmAttributesActionListener());
		MenuItem pmColor = new MenuItem("Set color");
		MenuItem pmDuplicate = new MenuItem("Duplicate");
		pmDuplicate.addActionListener(new pmDuplicateActionListener());
		
		nodePopupMenu.add(pmDeleteNode);
		nodePopupMenu.add(pmNodeAttr);
		nodePopupMenu.add(pmColor);
		nodePopupMenu.add(pmDuplicate);
		
		// Edge popup menu
		MenuItem pmDeleteEdge = new MenuItem("Delete edge");
		pmDeleteEdge.addActionListener(new pmDeleteEdgeActionListener());
		MenuItem pmEdgeAttr = new MenuItem("Edge attributes...");
		
		edgePopupMenu.add(pmDeleteEdge);
		edgePopupMenu.add(pmEdgeAttr);
		edgePopupMenu.add(pmDuplicate);
		
		// Circuit popup menu
		MenuItem pmDeleteSelection = new MenuItem("Delete selection");
		pmDeleteSelection.addActionListener(new pmDeleteSelectionActionListener());
		MenuItem pmEncapulateSelection = new MenuItem("Encapsulate");
		pmEncapsulateSelection.addActionListener(new pmEncapsulateSelectionActionListener());
		
		circuitPopupMenu.add(pmDeleteSelection);
		circuitPopupMenu.add(pmDuplicate);
		
		add(nodePopupMenu);
		add(edgePopupMenu);
		add(circuitPopupMenu);
		
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
		if (me.isPopupTrigger()) {
			// If we are on a node and there are multiple nodes selected, show circuit popup menu
			if (scores.get(activeScore).getSelectedNodes().size() > 1)
				circuitPopupMenu.show(this, me.getX(), me.getY());
			// Otherwise, if we are on a node (and 1 or 0 nodes selected)
			else if (null != scores.get(activeScore).findNodeAtPoint(me.getX(), me.getY()))
				nodePopupMenu.show(this, me.getX(), me.getY());
			// Otherwise, if we are on an edge
			else if (null != scores.get(activeScore).findEdgeAtPoint(me.getX(), me.getY()))
				edgePopupMenu.show(this, me.getX(), me.getY());
			// Otherwise, show the general popup
//			else
//				generalPopupMenu.show(this, me.getX(), me.getY());
		}
		
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
			nodePopupMenu.show(this, me.getX(), me.getY());
		
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
		//System.out.println("Duration of "+SMALL_SUBDIVISION+" note: "+sdtime);
		
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
	
	/**
	 * duplicateSelection()
	 * 
	 * Duplicates (deep-copies) all the Nodes and Edges currently selected.
	 * The copies will possess all the same attributes and event lists as
	 * their originals. Copies will be placed in the same relative position,
	 * at the top-left corner of the score.
	 * 
	 */
	private void duplicateSelection() {
	
		/**
		 * 1. For each selected edge:
		 * 		a. Check to see if the source/dest already on the "copies" list using
		 * 			"resembles"
		 * 			i. If not, copy those nodes.
		 * 		b. Create a new edge from source --> dest
		 * 		c. Put edge on copiedEdges
		 * 
		 * 2. For each selected node:
		 * 		a. Check to see if it is already on the copies list using "resembles"
		 * 			i. If not, copy that node
		 * 
		 */
		
		Score score = scores.get(activeScore);
		
		ArrayList<Node> selectedNodes = score.getSelectedNodes();
		ArrayList<Edge> selectedEdges = score.getSelectedEdges();
		// Brief sanity check
		if (selectedNodes.size() < 1)
			return;
		
		// Find the leftmost node's x-coordinate
		// Nodes are sorted by ascending X-coordinate automatically
		int minX = selectedNodes.get(0).getX();	
		// Find topmost node's y-coordinate
		int minY = 100000000;
		for (Node n : selectedNodes) {
			if (n.getY() < minY)
				minY = n.getY();
		}
		
		// X and Y offsets of the nodes to be copied
		int offX = -minX;
		int offY = -minY;
		
		// Lists to store copied nodes and edges
		ArrayList<Node> copiedNodes = new ArrayList<Node>();
		ArrayList<Edge> copiedEdges = new ArrayList<Edge>();
		
		// For each selected edge ...
		for (Edge ed : selectedEdges) {
			Node s = ed.getSource();
			Node d = ed.getDestination();
			
			// These will become the source and destination nodes of the newly copied edge.
			Node newSource, newDest;
			
			// Check to see if the source and destination of the edge have already been copied.
			int copiedSource, copiedDest;
			copiedSource = copiedDest = -1;
			for (int i = 0; i<copiedNodes.size(); i++) {
				Node n = copiedNodes.get(i);
				if (n.resembles(s)) copiedSource = i;
				if (n.resembles(d)) copiedDest = i;
			}
			
			// Copy over the source and destination nodes of the edge, if necessary.
			if (copiedSource < 0) {
				newSource = s.copy();
				newSource.setX(s.getX() + offX);
				newSource.setY(s.getY() + offY);
				copiedNodes.add(newSource);
			} else
				newSource = copiedNodes.get(copiedSource);
			
			if (copiedDest < 0) {
				newDest = d.copy();
				newDest.setX(d.getX() + offX);
				newDest.setY(d.getY() + offY);
				copiedNodes.add(newDest);
			} else
				newDest = copiedNodes.get(copiedDest);
			
			Edge ned = new Edge(this, newSource, newDest);
			newSource.registerEdge(ned);
			newDest.registerEdge(ned);
			copiedEdges.add(ned);
		}
		
		// For each selected Node ...
		for (Node n : selectedNodes) {
			// Check to see if we already copied the Node.
			boolean exists = false;
			for (Node c : copiedNodes)
				if (c.resembles(n))
					exists = true;
			
			// Copy the node if necessary
			if (! exists) {
				Node newNode = n.copy();
				newNode.setX(n.getX() + offX);
				newNode.setY(n.getY() + offY);
				copiedNodes.add(newNode);
			}
		}
		
		// Update the active Score's Node and Edge lists
		score.getNodes().addAll(copiedNodes);
		score.getEdges().addAll(copiedEdges);
		
		// Clear the current selection of Nodes and Edges
		score.clearActiveEdges();
		score.clearActiveNodes();
		
		// Select the newly copied Nodes and Edges
		score.selectNodes(copiedNodes);
		score.selectEdges(copiedEdges);
		
		System.out.printf("Copied %d nodes and %d edges.\n", copiedNodes.size(), copiedEdges.size());
		
	}
	
	
	/*******************
	 * INTERNAL CLASSES
	 ******************/
	
	private class pmDeleteNodeActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			
			scores.get(activeScore).removeNode(prevX, prevY);
			
		}
		
	}
	
	private class pmDuplicateActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			duplicateSelection();
		}
		
	}
	
	private class pmDeleteEdgeActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			scores.get(activeScore).removeEdge(prevX, prevY);
		}
		
	}
	
	private class pmDeleteSelectionActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			Score s = scores.get(activeScore);
			
			for (Node n : s.getSelectedNodes())
				s.removeNode(n);
			for (Edge ed : s.getSelectedEdges())
				s.removeEdge(ed);
		}
		
	}
	
	private class pmEncapsulateSelectionActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			scores.get(activeScore).encapsulateSelection();
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