package genseq.midi;

import genseq.obj.NodeEvent;
import javax.sound.midi.InvalidMidiDataException;

public interface Playable {

	// This method will be called by a ScoreTraverser when a Playable Object should be played.
	public NodeEvent respond(NodeEvent lastEvent, ScoreTraverser t) throws InvalidMidiDataException;
	// This method will be called by a ScoreTraverser on a Playable Object when the Traverser
	// cannot find any further destination to go (e.g. no outbound edges on a Node)
	public void stop(ScoreTraverser t) throws InvalidMidiDataException;
	
}
