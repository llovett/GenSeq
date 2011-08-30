package genseq.obj;

import processing.core.*;

public abstract class DrawableObject {

	// Member responsible for doing the actual rendering.
	protected PApplet parent;

	// Width, height, x, y
	protected int w, h, x, y;

	// Stroke width, bgcolor, stroke color
	protected int strokeWeight;
	protected int r,g,b;
	protected int sr,sg,sb;

	public DrawableObject(PApplet parent) {
		this.parent = parent;
		r = g = b = sr = sg = sb = 0;
		w = h = x = y = 0;
		strokeWeight = 0;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return w;
	}

	public int getHeight() {
		return h;
	}

	public int getStrokeWidth() {
		return strokeWeight;
	}

	public int[] getColor() {
		return new int[]{ r, g, b };
	}

	public int[] getStrokeColor() {
		return new int[]{ sr, sg, sb };
	}

	/******************
	 * MUTATOR METHODS *
	 ******************/

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setWidth(int w) {
		this.w = w;
	}

	public void setHeight(int h) {
		this.h = h;
	}

	public void setSize(int w, int h) {
		this.w = w;
		this.h = h;
	}

	public void setStrokeWeight(int strokeWeight) {
		this.strokeWeight = strokeWeight;
	}

	public void setColor(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}

	//     public void setColor(parent.color c) {
	// 	this.r = (c >> 16) & 0xFF;
	// 	this.g = (c >> 8) & 0xFF;
	// 	this.b = c & 0xFF;
	//     }

	public void setStrokeColor(int sr, int sg, int sb) {
		this.sr = sr;
		this.sg = sg;
		this.sb = sb;
	}

	//     public void setStrokeColor(Object c) {
	// 	if (! (c instanceof processing.core.color)) return;
	// 	this.sr = (c >> 16) & 0xFF;
	// 	this.sg = (c >> 8) & 0xFF;
	// 	this.sb = c & 0xFF;
	//     }

	/**
	 * setupDrawPrefs()
	 *
	 * Set up the drawing environment for rendering this
	 * object properly.
	 **/
	public void setupDrawPrefs() {
		parent.fill(r, g, b);
		parent.stroke(sr, sg, sb);
		parent.strokeWeight(strokeWeight);
	}
	
	
	/*******************
	 * ABSTRACT METHODS
	 ******************/
	
	public abstract void render();

}