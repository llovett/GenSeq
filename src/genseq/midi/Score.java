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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
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
	private static final int SELECTION_ALPHA = 50;
	private static final int SELECTION_STROKE_RED = 100;
	private static final int SELECTION_STROKE_GREEN = 200;
	private static final int SELECTION_STROKE_BLUE = 0;
	private static final int SELECTION_STROKE_ALPHA = 100;
	private static final int SELECTION_STROKE_WEIGHT = 5;
	
	private static final int DEFAULT_WIDTH = 600;
	private static final int DEFAULT_HEIGHT = 600;
	
	/*** INTERNAL CONTROL ***/
	// How accurate does the user have to be with the mouse in order
	// to select an item (in pixels)?
	private static final int CLICK_ACCURACY_NODE = 20;
	private static final int CLICK_ACCURACY_EDGE = 10;
	private static NodeComparator ncomp;
	private static EdgeComparator ecomp;
	private ArrayList<Node> activeNodes;
	private ArrayList<Edge> activeEdges;
	private boolean activeNodeLock;	// Locks using activeNodes while it's still being modified
	private boolean activeEdgeLock; // Locks using activeEdges while it's still being modified
	
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
		activeEdges = new ArrayList<Edge>();
		activeEdgeLock = false;
		
		// Build drawable objects for user feedback
		sr = new SelectRect(parent);
		sr.setColor(SELECTION_RED, SELECTION_GREEN, SELECTION_BLUE);
		sr.setStrokeWeight(SELECTION_STROKE_WEIGHT);
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
		
		removeNode(target);
		
		return target;
	}
	
	/**
	 * removeNode(Node n) - Remove n from the nodes list if it exists.
	 * 
	 * @param n - Node to remove
	 * @return True if the nodes list was changed, false otherwise.
	 */
	public boolean removeNode(Node n) {
		if (null == n) return false;
		
		for (int i=n.getEdges().size()-1; i>=0; i--) {
			removeEdge(n.getEdges().get(i));
		}
		
		Collections.sort(edges, ecomp);
				
		return nodes.remove(n);
	}
	
	/**
	 * removeEdge(int x, int y) - Remove the edge that passes through the point at (x, y)
	 * 
	 * @param x - An x-coordinate of a point that the edge passes through.
	 * @param y - A y-coordinate of a point that the edge passes through.
	 * 
	 * @return The edge that was removed.
	 */
	public Edge removeEdge(int x, int y) {
		Edge e = findEdgeAtPoint(x,y);
		
		removeEdge(e);
		
		return e;
	}
	
	/**
	 * removeEdge(Node e) - Remove e from the edges list if it exists.
	 * 
	 * @param e - Edge to remove
	 * @return True if the edges list was changed, false otherwise.
	 */
	public boolean removeEdge(Edge e) {
		Node s = e.getSource();
		Node d = e.getDestination();
		s.unregisterEdge(e);
		d.unregisterEdge(e);
		
		return edges.remove(e);
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
			if (dist <= CLICK_ACCURACY_NODE && dist < closestDistance) {
				result = n;
				closestDistance = dist;
			}
		}
		return result;
	}
	
	/**
	 * findEdgeAtPoint
	 * 
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @return The edge closest to the point (x,y) such that a line perpendicular to the edge
	 * from the mouse intersects the edge.
	 */
	public Edge findEdgeAtPoint(double x, double y) {
		Edge target = null;
		
		// This will hold the slopes of the calculated lines
		double closestDistance = 100000000.0;
			
		for (Edge e : edges) {
			double ex1 = e.getSource().getX();
			double ey1 = e.getSource().getY();
			double ex2 = e.getDestination().getX();
			double ey2 = e.getDestination().getY();
			
			double dY = (ey1 - ey2);
			double dX = (ex1 - ex2);
			
			double eyi = ey1 - (dY/dX)*ex1;
			double myi = y - (-dX/dY)*x;
			
			double edgeSlope = dY/dX;
			double clickSlope = -dX/dY;
			
			double xi, yi;

			// Deal with clickSlope being zero
			if (0.0 == clickSlope) {
				xi = ex1;
				yi = y;
			}
			else {
				xi = (eyi - myi)/((1 - (edgeSlope/clickSlope))*clickSlope);
				yi = edgeSlope*xi + eyi;
			}
			
			// Check to see if (xi,yi) is actually on the edge
			if (! (xi > (ex1 < ex2? ex1 : ex2) &&
					xi < (ex1 > ex2? ex1 : ex2) &&
					yi > (ey1 < ey2? ey1 : ey2)	&&
					yi < (ey1 > ey2? ey1 : ey2)))
				continue;
			
			double dist = distance(xi,yi,x,y);
			
			if (dist < closestDistance) {
				target = e;
				closestDistance = dist;
			}
		}
		
		
		// No dice, if click is still too far away
		if (closestDistance > CLICK_ACCURACY_EDGE)
			target = null;
		
		return target;
	}
	
	/**
	 * getNodes()
	 * 
	 * @return - A list of all the nodes on this score.
	 */
	public ArrayList<Node> getNodes() {
		return nodes;
	}
	
	/**
	 * getEdges()
	 * 
	 * @return - A list of all the edges on this score.
	 */
	public ArrayList<Edge> getEdges() {
		return edges;
	}
	
	/**
	 * getSelectedNodes()
	 * 
	 * @return - A list of all the nodes that are currently selected ("active" nodes)
	 */
	public ArrayList<Node> getSelectedNodes() {
		Collections.sort(activeNodes, ncomp);
		return activeNodes;
	}
	
	/**
	 * getSelectedEdges()
	 * 
	 * @return - A list of all the edges that are currently selected ("active" edges)
	 */
	public ArrayList<Edge> getSelectedEdges() {
		return activeEdges;
	}
	
	/**
	 * clearActiveNodes()
	 * 
	 * Clears all the currently selected Nodes.
	 */
	public void clearActiveNodes() {
		for (Node n : activeNodes)
			n.deselect();
		
		activeNodes.clear();
	}
	
	/**
	 * clearActiveEdges()
	 * 
	 * Clears all the currently selected Edges.
	 */
	public void clearActiveEdges() {
		for (Edge e : activeEdges)
			e.deselect();
		
		activeEdges.clear();
	}
	
	/**
	 * selectNodes(Node n) - Select Nodes on this Score.
	 * @param n - Node to be selected.
	 */
	public void selectNodes(Node n) {
		n.select();
		activeNodes.add(n);
	}
	
	/**
	 * selectEdges(Edge e) - Select Edges on this Score.
	 * @param e - Edge to be selected.
	 */
	public void selectEdges(Edge e) {
		e.select();
		activeEdges.add(e);
	}
	
	/**
	 * selectNodes(Node n) - Select Nodes on this Score.
	 * @param n - Node to be selected.
	 */
	public void selectNodes(Collection<Node> nodes) {
		for (Node n : nodes)
			n.select();
		
		activeNodes.addAll(nodes);
	}
	
	/**
	 * selectEdges(Edge e) - Select Edges on this Score.
	 * @param e - Edge to be selected.
	 */
	public void selectEdges(Collection<Edge> edges) {
		for (Edge e : edges)
			e.select();
		
		activeEdges.addAll(edges);
	}
	
	/*******************************************************
	 * DRAW LOOP.
	 * 
	 * Called at every tick and updates graphics.
	 ******************************************************/
	
	public void render() {
		try {
			parent.background(150);
			
			// Drawable objects that need to be rendered
			sr.render();
			
			for (Edge e : edges) {
				e.render();
			}
			for (Node n : nodes) {
				n.render();
			}

			
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
	
	@Override
	public void mouseClicked(MouseEvent me) {

		if (parent.getMode() == GenSeq.CREATE_NODES) {

			if (me.getButton() == MouseEvent.BUTTON1) {

				// Clear all selected Nodes
				clearActiveNodes();

				if (distance(me.getX(), me.getY(), prevX, prevY) < CLICK_ACCURACY_NODE) {
					Node newNode = new Node(parent, me.getX(), me.getY());
					newNode.select();
					nodes.add(newNode);
					activeNodes.add(newNode);

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

		// A node that may potentially be selected
		Node selNode = findNodeAtPoint(prevX, prevY);
		// An edge that may potentially be selected
		Edge selEdge = findEdgeAtPoint(prevX, prevY);	
		
		// Clear any visual FX on the active nodes that may be happening
		if (! activeNodeLock) {
			if (null == selNode || (! selNode.isSelected()))
				clearActiveNodes();
			
			if (null != selNode) {
				// If we're in a moving sort of mood...
				if (parent.getMode() == GenSeq.MOVE_NODES || parent.mouseButton == PApplet.CENTER)
					if (! selNode.isSelected()) {
						selNode.select();
						activeNodes.add(selNode);
					}
			}
			
		} else {
			// Lift the lock
			activeNodeLock = false;
		}
		if (! activeEdgeLock) {
			// See if we should clear all selected Edges or not : 
			// we should release the edge if selEdge is null AND
			// (node we clicked is null or is not connected to a selected edge)
			boolean nodeIsIncidentToSelectedEdge = false;
			if (null != selNode) {
				for (Edge e : selNode.getEdges())
					if (e.isSelected()) {
						nodeIsIncidentToSelectedEdge = true;
						break;
					}
			}
			
			if ((null == selNode || (! nodeIsIncidentToSelectedEdge)) || (null != selEdge && (! selEdge.isSelected())))
				clearActiveEdges();
			
			// Yield to selected nodes, since it's more likely the user is trying to click a Node
			if (null != selEdge && selNode == null) {
				// If we're in a moving sort of mood...
				if (parent.getMode() == GenSeq.MOVE_NODES || parent.mouseButton == PApplet.CENTER)
					selEdge.select();
				
				activeEdges.add(selEdge);
			}
		} else {
			// Lift edge lock
			activeEdgeLock = false;
		}

	}

	@Override
	public void mouseReleased(MouseEvent me) {

		// See if we were connecting two nodes with an edge, and create that edge.
		if (distance(me.getX(), me.getY(), prevX, prevY) >= CLICK_ACCURACY_NODE) {
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
		if (parent.getMode() == GenSeq.MOVE_NODES || parent.mouseButton == PApplet.CENTER) {
			// If there are already nodes selected...
			if (null != activeNodes && activeNodes.size() > 0 && (! activeNodeLock)) {
				
				int deltaX = mX - prevX;
				int deltaY = mY - prevY;
				
				for (Node n : activeNodes) {
					n.setX(n.getX() + deltaX);
					n.setY(n.getY() + deltaY);

					// Resize the edges
					for (Edge e : n.getEdges())
						e.calculateLength();
					
				}
				
				prevX = mX;
				prevY = mY;
				
			}
			// Otherwise, if there are no nodes selected...
			// The code hereunder should be performed only for clicking & dragging the mouse to select Nodes.
			else {
				// Lock the activeNodes list, since we'll be adding an indefinite number of nodes to this list.
				activeNodeLock = true;
				
				if (! sr.isVisible()) {
					sr.setAlpha(SELECTION_ALPHA);
					sr.setStrokeAlpha(SELECTION_STROKE_ALPHA);
				}
				
				sr.setBounds(prevX, prevY, mX, mY);
				
				// Find nodes within selected area
				// TODO: This involves two O(n) operations, every time the mouse is moved.
				// Can this be faster?
				for (Node n : nodes) {
					if (n.getX() >= sr.getX() &&
							n.getX() <= sr.getX2() &&
							n.getY() >= sr.getY() &&
							n.getY() <= sr.getY2()) {
						
						if (! activeNodes.contains(n)) {
							activeNodes.add(n);
							n.select();
						}

					}
					else {
						n.deselect();
						activeNodes.remove(n);
					}
						
				}
				
				// Find edges within selected area
				// TODO: This involves two O(n) operations, every time the mouse is moved.
				// Can this be faster?
				for (Edge e : edges) {
					int lowx = (e.getSource().getX() < e.getDestination().getX()? e.getSource().getX() : e.getDestination().getX());
					int highx = (e.getSource().getX() > e.getDestination().getX()? e.getSource().getX() : e.getDestination().getX());
					int lowy = (e.getSource().getY() < e.getDestination().getY()? e.getSource().getY() : e.getDestination().getY());
					int highy = (e.getSource().getY() > e.getDestination().getY()? e.getSource().getY() : e.getDestination().getY());
					
					if (lowx >= sr.getX() &&
							highx <= sr.getX2() &&
							lowy >= sr.getY() &&
							highy <= sr.getY2()) {
						
						if (! activeEdges.contains(e)) {
							activeEdges.add(e);
							e.select();
						}

					}
					else {
						e.deselect();
						activeEdges.remove(e);
					}
						
				}
				
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
	
	private class EdgeComparator implements Comparator<Edge> {
		
		public int compare(Edge o1, Edge o2) {
			return (o1.compareTo(o2));
		}
		
	}
	
	private class NodeComparator implements Comparator<Node> {
		
		public int compare(Node o1, Node o2) {
			return (o1.compareTo(o2));
		}
		
	}
	
	@SuppressWarnings("unused")
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
			parent.rectMode(PApplet.CORNERS);
			parent.rect(x, y, x2, y2);
		}
		
	}

}
