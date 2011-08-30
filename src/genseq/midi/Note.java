/***
 * 
 * Note.java
 * 
 * Stores information related to a single musical note.
 * 
 * The Note class doesn't actually perform any actions, apart from
 * accessors and mutators. This means that NOTES DO NOT PLAY THEMSELVES.
 * Some other class has to play the notes. This is usually a ScoreTraverser.
 * 
 */

package genseq.midi;

public class Note {

	private static final int DEFAULT_VELOCITY = 100;
	private static final double DEFAULT_LIKELIHOOD = 1.0;
	
	private int pitch, velocity;
	
	public Note(int pitch, int vel) {
		this.pitch = pitch;
		this.velocity = vel;
	}
	
	public Note(int pitch) {
		this(pitch,
				DEFAULT_VELOCITY);
	}
	
	public Note() {
		this(MIDIConstants.REST, DEFAULT_VELOCITY);
	}
	
	public int getPitch() {
		return pitch;
	}

	public void setPitch(int pitch) {
		this.pitch = pitch;
	}

	public int getVelocity() {
		return velocity;
	}

	public void setVelocity(int velocity) {
		this.velocity = velocity;
	}
	
}
