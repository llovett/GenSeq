/***
 * NodeEvent.java
 * 
 * A NodeEvent consists of either a single Note or an
 * ArrayList of Notes. It encapsulates what can be played
 * upon a single request for a Node to respond(). It makes
 * things convenient inside NodeAttributesWindow wrt casting
 * on the eventlist. It also makes printing Strings for events
 * much nicer.
 * 
 */

package genseq.obj;

import genseq.midi.Note;
import genseq.midi.MIDIConstants;
import java.util.ArrayList;


public class NodeEvent implements MIDIConstants {

	public static final int TYPE_SINGLE_NOTE = 0;
	public static final int TYPE_CHORD = 1;
	public static final int TYPE_REST = 2;
	
	private static final double DEFAULT_LIKELIHOOD = 1.0;
	
	private ArrayList<Note> notelist;
	private double likelihood;
	private int type;
	
	public NodeEvent(ArrayList<Note> notelist, double likelihood) {
		this.notelist = notelist;
		this.likelihood = likelihood;
		
		type = TYPE_CHORD;
	}
	
	public NodeEvent(ArrayList<Note> notelist) {
		this(notelist, DEFAULT_LIKELIHOOD);
	}
	
	public NodeEvent(Note n, double likelihood) {
		notelist = new ArrayList<Note>();
		notelist.add(n);
		this.likelihood = likelihood;
		
		type = TYPE_SINGLE_NOTE;
	}

	
	public NodeEvent(Note n) {
		this(n, DEFAULT_LIKELIHOOD);
	}
	
	/**
	 * Empty constructor for creating a "blank" event.
	 */
	public NodeEvent() {
		this(new Note(), DEFAULT_LIKELIHOOD);
	}
	
	public ArrayList<Note> getNotes() {
		return notelist;
	}
	
	public void setNotes(ArrayList<Note> notes) {
		// No empty events allowed!
		if (null == notes || notes.size() == 0)
			throw new NullPointerException();
		
		notelist = notes;
		
		if (notelist.size() > 1)
			type = TYPE_CHORD;
		else if (REST != notelist.get(0).getPitch())
			type = TYPE_SINGLE_NOTE;
		else if (REST == notelist.get(0).getPitch())
			type = TYPE_REST;
		
	}
	
	public int getType() {
		return type;
	}
	
	public double getLikelihood() {
		return likelihood;
	}
	
	public void setLikelihood(double likelihood) {
		this.likelihood = likelihood;
	}
	
	public boolean isEmpty() {
		return notelist.isEmpty();
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder("");
		
		if (notelist.size() > 1)
			result.append("(");
		
		for (Note theNote : notelist) {
			if (REST == theNote.getPitch())
				result.append("REST");
			else
				// Note the +1 here, since the first "pitch" that appears on PITCHES is "REST"
				result.append(PITCHES[theNote.getPitch() + 1]);
			
			result.append(",");
		}
		
		// Remove the last comma
		result.deleteCharAt(result.length() - 1);
		
		if (notelist.size() > 1)
			result.append(")");
		
		return result.toString();
	}
	
}
