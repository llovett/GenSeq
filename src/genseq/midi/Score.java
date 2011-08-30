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
	private static final int DEFAULT_WIDTH = 600;
	private static final int DEFAULT_HEIGHT = 600;
	
	/*** INTERNAL CONTROL ***/
	// How accurate does the user have to be with the mouse in order
	// to select an item (in pixels)?
	private static final int CLICK_ACCURACY = 20;
	private static NodeComparator ncomp;
	private static EdgeComparator ecomp;
	private Node activeNode;
	
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
		
		activeNode = null;
		
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
			parent.background(255);

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
		
		//if (parent.getMode() == GenSeq.MOVE_NODES) {
			activeNode = findNodeAtPoint(prevX, prevY);
		//}
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
		
		if (parent.getMode() == GenSeq.MOVE_NODES ||
				(parent.getMode() == GenSeq.CREATE_NODES && parent.mouseButton == parent.CENTER)) {
			if (null != activeNode) {
				activeNode.setX(mX);
				activeNode.setY(mY);
				
				// Resize the edges
				for (Edge e : activeNode.getEdges())
					e.calculateLength();
			}
		}
		
	}

}
