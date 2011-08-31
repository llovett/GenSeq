/***
 * GenSeqCanvas
 * 
 * Hosts the graphical representations of nodes and edges,
 * as well as any other kinds of objects that will be created.
 * The canvas handles zooming in and out, having multiple
 * instances open (like layers), supporting arbitrary alpha channel
 * strength. This is all controllable via the GenSeq class.
 * 
 * 
 * @author okami89
 *
 */

package genseq.midi;

import genseq.obj.*;
import processing.core.*;
import java.awt.PopupMenu;
import java.awt.AWTEvent;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;

public class Score implements ActionListener, MouseListener {

	/*** PROGRAM NAME AND VERSION ***/
	public static final String NAME = "Graphikal";
	public static final double VERSION = 0.1;
	
	/*** BASIC LOOK & FEEL CONSTANTS ***/
	private static final int DEFAULT_STROKE_RED = 200;
	private static final int DEFAULT_STROKE_GREEN = 200;
	private static final int DEFAULT_STROKE_BLUE = 255;
	private static final int DEFAULT_STROKE_WEIGHT = 2;
	
	private static final int SELECTION_RED = 200;
	private static final int SELECTION_GREEN = 255;
	private static final int SELECTION_BLUE = 100;
	private static final int SELECTION_ALPHA = 100;
	private static final int SELECTION_STROKE_RED = 100;
	private static final int SELECTION_STROKE_GREEN = 200;
	private static final int SELECTION_STROKE_BLUE = 0;
	private static final int SELECTION_STROKE_ALPHA = 100;
	
	private static final int DEFAULT_WIDTH = 600;
	private static final int DEFAULT_HEIGHT = 600;
	
	/*** INTERNAL CONTROL ***/
	// How accurate does the user have to be with the mouse in order
	// to select an item (in pixels)?
	private static final int CLICK_ACCURACY = 20;
	private static NodeComparator ncomp;
	private static EdgeComparator ecomp;
	private ArrayList<Node> activeNodes;
	private boolean activeNodeLock;	// Locks using activeNodes while it's still being modified
	
	/*** OBJECTS THAT SHOULD BE DRAWN (BUT NOT NODES/EDGES) ***/
	SelectRect sr;
	
	/*** EXTERNAL REFERENCES ***/
	private GenSeq parent;
	
	/*** GLOBAL DATA ***/
	private ArrayList<Node> nodes;
	private ArrayList<Edge> edges;
	private int prevX, prevY;
	
	
	
	/***************
	 * CONSTRUCTOR
	 * @param parent - The Processing applet that will do the actual rendering.
	 ***************/
	public Score(GenSeq parent) {
		this.parent = parent;
		
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
		
		ncomp = new NodeComparator();
		ecomp = new EdgeComparator();
		
		activeNodes = new ArrayList<Node>();
		activeNodeLock = false;
		
		// Build drawable objects for user feedback
		sr = new SelectRect(parent);
		sr.setColor(SELECTION_RED, SELECTION_GREEN, SELECTION_BLUE);
		sr.setStrokeColor(SELECTION_STROKE_RED, SELECTION_STROKE_GREEN, SELECTION_STROKE_BLUE);
		
		constructComponents();
	}
	
	
	/**
	 * constructComponents()
	 * 
	 * Allocate space for and initialize any non-Processing GUI
	 * components.
	 */
	private void constructComponents() {
		// TODO: write this method if necessary
	}
	
	/**
	 * removeNode(int x, int y) - Removes the node at (x,y)
	 * 
	 * @param x - x-coordinate of the node to be removed
	 * @param y - y-coordinate of the node to be removed
	 * @return The node that was removed from the score. null if there
	 * was no node at (x,y).
	 */
	public Node removeNode(int x, int y) {
		Node target = findNodeAtPoint(x, y);
		if (null != target) {
			// Remove the edges connected to the deleted node
			ArrayList<Edge> targetedEdges = target.getEdges();
			Collections.sort(targetedEdges, ecomp);
			
			for (Edge edge : targetedEdges) {
				edges.remove(edge);
			}
			nodes.remove(target);
			
		}
		
		return target;
	}
	
	/**
	 * findNodeAtPoint
	 * 
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @return the closest Node to the point given, if such a point exists
	 * within a radius of CLICK_ACCURACY of the point.
	 */
	public Node findNodeAtPoint(double x, double y) {
		Node result = null;
		double closestDistance = 100000000.0;
		
		// Iterate through the nodes, checking distances
		for (Node n : nodes) {
			double dist = distance(n.getX(), n.getY(), x, y); 
			if (dist <= CLICK_ACCURACY && dist < closestDistance) {
				result = n;
				closestDistance = dist;
			}
		}
		return result;
	}
	
	/**
	 * getNodes()
	 * 
	 * @return - A list of all the nodes on this score.
	 */
	public ArrayList<Node> getNodes() {
		return nodes;
	}
	
	/*******************************************************
	 * DRAW LOOP.
	 * 
	 * Called at every tick and updates graphics.
	 ******************************************************/
	
	public void render() {
		try {
			parent.background(150);

			for (Edge e : edges) {
				e.render();
			}
			for (Node n : nodes) {
				n.render();
			}
			
			// Drawable objects that need to be rendered
			sr.render();
			
		} catch (ConcurrentModificationException e) {
			// TODO: Can we prevent this from happening, rather than catching
			// the exception here?
		}
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
		
	}	
	

	@SuppressWarnings("unchecked")
	@Override
	public void mouseClicked(MouseEvent me) {

		if (parent.getMode() == GenSeq.CREATE_NODES) {

			if (me.getButton() == MouseEvent.BUTTON1) {


				if (distance(me.getX(), me.getY(), prevX, prevY) < CLICK_ACCURACY) {
					nodes.add(new Node(parent, nodes.size(), me.getX(), me.getY()));

					// Keep our lists of nodes sorted, so we can
					// search / delete & do other things quickly.
					Collections.sort(nodes, ncomp);
				}
			}

		}
	}


	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * mousePressed()
	 * 
	 * Handles mouseDown events.
	 */
	public void mousePressed(MouseEvent me) {
		prevX = me.getX();
		prevY = me.getY();
		
		// Clear any visual fx on the active nodes that may be happenening
		if (! activeNodeLock) {
			for (Node n : activeNodes)
				n.deselect();

			activeNodes.clear();
		} else {
			// Lift the lock
			activeNodeLock = false;
		}
		
		Node selNode = findNodeAtPoint(prevX, prevY);
		if (null != selNode)
			activeNodes.add(selNode);
	}

	@Override
	public void mouseReleased(MouseEvent me) {

		if (distance(me.getX(), me.getY(), prevX, prevY) >= CLICK_ACCURACY) {
			Node n1 = findNodeAtPoint(prevX, prevY);
			Node n2 = findNodeAtPoint(me.getX(), me.getY());
			
			if (null != n1 && null != n2) {
				Edge userEdge = new Edge(parent, n1, n2);
				
				// Make sure this edge doesn't already exist.
				for (Edge e : edges) {
					if (e.equals(userEdge)) return;
				}
				
				edges.add(userEdge);
				n1.registerEdge(userEdge);
				n2.registerEdge(userEdge);
				
				// Keep our lists of nodes sorted, so we can
				// search / delete & do other things quickly.
				Collections.sort(edges, ecomp);
			}
		}
		
		// Hide the selection rectangle
		sr.setVisible(false);
	}
	
	/**
	 * Called by GenSeq to let this score know that the mouse has been
	 * dragged. Using mouseDragged(MouseEvent) in GenSeq causes some
	 * rendering problems, so we use processing's native mouseDragged, which
	 * necessitates this method so GenSeq can forward mouse dragging events here.
	 * 
	 * @param mX - current x-coordinate of mouse
	 * @param mY - current y-coordinate of mouse
	 */
	public void mouseDragged(int mX, int mY) {
		
		// If we can move nodes...
		if (parent.getMode() == GenSeq.MOVE_NODES ||
				(parent.getMode() == GenSeq.CREATE_NODES && parent.mouseButton == parent.CENTER)) {
			// If there are already nodes selected...
			if (null != activeNodes && activeNodes.size() > 0 && (! activeNodeLock)) {
				
				for (Node n : activeNodes) {
					n.setX(n.getX() + (mX - prevX));
					n.setY(n.getY() + (mY - prevY));
					
					// Resize the edges
					for (Edge e : n.getEdges())
						e.calculateLength();
					
				}
				
				prevX = mX;
				prevY = mY;
				
			}
			// Otherwise, if there are no nodes selected...
			else {
				// Lock the activeNodes list, since we'll be adding an indefinite number of nodes to this list.
				activeNodeLock = true;
				
				if (! sr.isVisible()) {
					sr.setAlpha(SELECTION_ALPHA);
					sr.setStrokeAlpha(SELECTION_STROKE_ALPHA);
				}
				
				sr.setBounds(prevX, prevY, mX, mY);
				
				// Show selected nodes
				int startX = sr.getX();
				int stopX = sr.getX2();
				
				// Find nodes within selected area
				for (Node n : nodes) {
					if (n.getX() < startX) continue;
					if (n.getX() <= stopX && n.getY() >= sr.getY() && n.getY() <= sr.getY2())
						activeNodes.add(n);
					else if (n.getX() > stopX)
						break;
				}
				
				// Highlight selected nodes
				for (Node n : activeNodes)
					n.select();
				
			}
		}
		
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
	
	private class EdgeComparator implements Comparator {
		
		public int compare(Object o1, Object o2) {
			return ((Edge)o1).compareTo((Edge)o2);
		}
		
	}
	
	private class NodeComparator implements Comparator {
		
		public int compare(Object o1, Object o2) {
			return ((Node)o1).compareTo((Node)o2);
		}
		
	}
	
	private class SelectRect extends DrawableObject {
		
		private int x2, y2;
		
		public SelectRect(PApplet parent) {
			super(parent);
		}
		
		public void setX2(int x2) {
			if (x2 < x) {
				int buff = x;
				x = x2;
				this.x2 = buff;				
			}
			else
				this.x2 = x2;
		}
		
		public void setY2(int y2) {
			if (y2 < y) {
				int buff = y;
				y = y2;
				this.y2 = buff;
			}
			else
				this.y2 = y2;
		}
		
		public int getX2() {
			return x2;
		}
		
		public int getY2() {
			return y2;
		}
		
		public void setBounds(int x, int y, int x2, int y2) {
			this.x = (x < x2? x : x2);
			this.y = (y < y2? y : y2);
			this.x2 = (x2 > x? x2 : x);
			this.y2 = (y2 > y? y2 : y);
		}
		
		public int getWidth() {
			return x2 - x;
		}
		
		public int getHeight() {
			return y2 - y;
		}
		
		public void render() {
			setupDrawPrefs();
			parent.rectMode(parent.CORNERS);
			parent.rect(x, y, x2, y2);
		}
		
	}

}
