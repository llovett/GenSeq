/***
 * 
 * NoteMatrix.java
 * 
 * Gives a nice graphical interface when designing single musical events.
 * The interface allows the user to specify pitch, velocity, and likelihood
 * to play of a note upon the node's being called to respond(). 
 * 
 */

package genseq.gui;

import genseq.midi.Note;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Component;
import java.awt.PopupMenu;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class NoteMatrix extends Component {

	/*** INTERNAL CONSTANTS ***/
	private static final int DEFAULT_WIDTH = 200;	// Default width of a NoteMatrix
	private static final int DEFAULT_HEIGHT = 100;	// Default height of a NoteMatrix
	private static final int MAX_BAR_WIDTH = 30;	// Maximum width of the graphical rep. of a note 
	
	/*** PROPERTIES / ATTRIBUTES ***/
	private int width, height;
	private int barwidth;
	private ArrayList<Note> notes;
	
	/*** GUI COMPONENTS ***/
	private PopupMenu menu;
	
	public NoteMatrix(Iterable<Note> notes) {
		for (Note n : notes) {
			this.notes.add(n);
		}
		
		width = DEFAULT_WIDTH;
		height = DEFAULT_HEIGHT;
		
		adjust();
		constructComponents();
	}
	
	public NoteMatrix() {
		this(null);
	}
	
	private void constructComponents() {
		// Popup menu
		menu = new PopupMenu();
		MenuItem pmAddNote = new MenuItem("Add note");
		pmAddNote.addActionListener(new pmAddNoteListener());
		MenuItem pmRemNote = new MenuItem("Remove note");
		pmRemNote.addActionListener(new pmRemNoteListener());
		menu.add(pmAddNote);
		menu.add(pmRemNote);
	}
	
	/**
	 * adjust() - Refresh the graphical interface after construction or
	 * user interaction.
	 * 
	 */
	private void adjust() {
		barwidth = width / notes.size();
	}
	
	public void paint(Graphics g) {
		g.setColor(Color.WHITE);
		g.clearRect(0, 0, width, height);
		
		int currX = 0;
		for (Note n : notes) {
			int currH = likelihoodToHeight(n.getLikelihood());
			
			g.setColor(velToColor(n.getVelocity()));
			g.drawRect(currX, currH, barwidth, currH);
		}
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}
	
	public Dimension getMinimumSize() {
		return new Dimension(width, height);
	}
	
	
	
	
	/*****************
	 * PRIVATE METHODS
	 ****************/
	private Color velToColor(int velocity) {
		float hue = (float)(velocity/127.0);
		return new Color(Color.HSBtoRGB(hue, 0.9f, 1.0f));
	}
	
	private int likelihoodToHeight(double likelihood) {
		return (int)(height * likelihood);
	}
	

	
	
	/******************
	 * INTERNAL CLASSES
	 *****************/

	/*** An internal class to handle mouse events ***/
	class NoteMatrixListener extends MouseAdapter {
		
		public void mousePressed(MouseEvent me) {
			
		}
		
		public void mouseReleased(MouseEvent me) {
			
		}
		
		public void mouseClicked(MouseEvent me) {
			if (me.isPopupTrigger())
				menu.show(me.getComponent(), me.getX(), me.getY());
		}
		
		
	}
	
	private class pmAddNoteListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent ae) {
			// TODO Auto-generated method stub
			
		}
		
	}

	private class pmRemNoteListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent ae) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
