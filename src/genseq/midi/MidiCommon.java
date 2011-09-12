/**
 * MidiCommon.java
 * 
 * Hosts MIDI objects and provides essential MIDI services to all classes.
 * 
 */

package genseq.midi;

import genseq.obj.Node;

import java.util.Hashtable;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

public final class MidiCommon implements MIDIConstants {

	// Hashtable for easy retrieval of pitch values per string
	private static Hashtable<String, Integer>pitches;
	
	/*** MIDI CONTROL ***/
	private static MidiDevice midiDevice;
	
	/**
	 * No instantiation allowed!
	 */
	private MidiCommon() {

	}

	private static void init() {
		pitches = new Hashtable<String, Integer>();

		int counter = 0;
		for (String pitchname : PITCHES) {
			if (pitchname.contains("REST"))
				continue;

			String[] pnd = pitchname.split(" ");
			pitches.put(pnd[0], counter);
			
			// If there was a description along with the pitchname, go ahead
			// and add the whole pitchname+description as a new key with the same pitchname.
			if (pnd.length > 1)
				pitches.put(pitchname, counter);
			
			counter++;
		}
		
	}
	
	
	/**
	 * getMidiDevice() - Get the MIDI device used by this GenSeq application
	 * 
	 * @return - A MIDI device.
	 */
	public static MidiDevice getMidiDevice() {
		return MidiCommon.midiDevice;
	}
	
	/**
	 * setMidiDevice() - Sets the MIDI device used by GenSeq.
	 * 
	 * @param m - The new MIDI device to use.
	 * @throws MidiUnavailableException 
	 */
	public static void setMidiDevice(MidiDevice m) throws MidiUnavailableException {
		MidiCommon.midiDevice = m;
		Node.setReceiver(MidiCommon.midiDevice.getReceiver());
	}
	
	public static int getPitchFromString(String s) {
		if (s.contains("REST"))
			return REST;
		
		if (null == pitches)
			init();
		
		return pitches.get(s);
	}



}