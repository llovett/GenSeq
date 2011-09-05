package genseq.obj;

import processing.core.*;
import java.util.Comparator;

public class Edge extends DrawableObject {
	private static final int DEFAULT_STROKE_RED = 0;
	private static final int DEFAULT_STROKE_GREEN = 0;
	private static final int DEFAULT_STROKE_BLUE = 0;
	private static final int DEFAULT_STROKE_WEIGHT = 2;
	private static final int SELECTED_STROKE_WEIGHT = 5;
	
	private Node from, to;
	private int length;
	private boolean selected;
	
	public Edge(PApplet parent, Node from, Node to) {
		super(parent);
		
		this.from = from;
		this.to = to;
		
		setStrokeColor(DEFAULT_STROKE_RED, DEFAULT_STROKE_GREEN, DEFAULT_STROKE_BLUE);
		setStrokeWeight(DEFAULT_STROKE_WEIGHT);
		
		calculateLength();
	}
	
//	/**
//	 * COPY CONSTRUCTOR
//	 * Provides a way to do a deep-copy.
//	 * 
//	 * @param e - Edge that is to be copied.
//	 */
//	public Edge(Edge e) {
//		super(e.parent);
//		
//		
//	}
	
	public void calculateLength() {
		length = (int)Math.sqrt(Math.pow(from.getX() - to.getX(), 2) + Math.pow(from.getY() - to.getY(), 2)) - from.getWidth()/2 - 2;
	}

	/***********
	 * ACCESSORS
	 ***********/
	
	public Node getSource() {
		return from;
	}
	
	public Node getDestination() {
		return to;
	}
	
	public int getLength() {
		return length;
	}
	
	/***********
	 * MUTATORS
	 ***********/
	
	public void setSource(Node n) {
		from = n;
		calculateLength();
	}
	
	public void setDestination(Node n) {
		to = n;
		calculateLength();
	}
	
	/**
	 * select()
	 * 
	 * Select this edge.
	 */
	public void select() {
		selected = true;
	}
	
	/**
	 * deselect()
	 * 
	 * Clear the selection on this edge.
	 */
	public void deselect() {
		selected = false;
	}
	
	/**
	 * isSelected()
	 * 
	 * @return True if this edge is selected, false otherwise.
	 */
	public boolean isSelected() {
		return selected;
	}
	
	public void render() {
		setupDrawPrefs();
	
		parent.pushMatrix();
		
		parent.translate(from.getX(), from.getY());	
		parent.rotate((float)Math.atan2((double)(to.getY() - from.getY()),
										(double)(to.getX() - from.getX())));
		
		if (selected)
			parent.strokeWeight(SELECTED_STROKE_WEIGHT);
		
		parent.line(from.getWidth()/2, 0, length, 0);
		parent.triangle(length, 0, length - 5, -5, length - 5, 5);
		
		parent.popMatrix();
	}
	
	public boolean equals(Edge e) {
		return (to.getX() == e.getDestination().getX() &&
				from.getX() == e.getSource().getX() &&
				to.getY() == e.getDestination().getY() &&
				from.getY() == e.getSource().getY());
	}
	
	public boolean resembles(Edge e) {
		//return getSource()
		return true;
	}

	public int compareTo(Edge e) {
		return length - e.getLength();
	}
}