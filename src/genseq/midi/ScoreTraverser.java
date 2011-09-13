/**
 * ScoreTraverser.java
 * 
 * Traverses a GenSeqScore, starting at a prime node.
 * Each ScoreTraverser runs on its own thread. The operations
 * that a ScoreTraverser performs, in order, are the following:
 * 
 * 1. Initialize with a reference to a Prime Node n, and a pointer
 * "curr" pointing to n.
 * 2. While there are nodes incident to "curr", perform the following:
 * 		a) Ask the node at "curr" to respond.
 * 		b) Choose an edge
 *		c) Wait the appropriate amount of time on that edge. Perform
 *			any necessary MIDI CC/SYSEX messages.
 *		d) Set "curr" to the incident node.
 *
 */

package genseq.midi;

import genseq.obj.*;
import java.util.Random;
import javax.sound.midi.InvalidMidiDataException;

public class ScoreTraverser extends Thread {

	/*** INTERNAL CONSTANTS ***/
	private static final int NODE_PLAY = 0;
	private static final int EDGE_WAIT = 1;
	
	/*** EXTERNAL REFERENCES ***/
	private GenSeq parent;
	
	/*** INTERNAL CONTROL ***/
	private boolean done;
	
	// What's our status?
	private int status;
	
	// Current node and edge
	private Node currn;
	private Edge curre;
	
	// Last even that played
	private NodeEvent lastEvent;
	
	// How far (in theoretical pixels) this traverser is along an edge
	private double edgeDist;
	
	// Tool to generate random numbers
	Random rand;
	
	public ScoreTraverser(GenSeq parent, Node n) {
		// Make sure the prime node exists
		if (null == n) throw new NullPointerException();
		
		this.parent = parent;
		this.currn = n;
		this.curre = null;
		
		lastEvent = null;
		
		rand = new Random();
		
		// Get ready to play the first (prime) node.
		done = false;
		status = NODE_PLAY;
	}
	
	/**
	 * run()
	 * 
	 * The brains of the outfit.
	 */
	public void run() {
		if (done) return;
		
		// Tasks to perform at the end of an edge
		if (EDGE_WAIT == status &&
				null != curre &&
				edgeDist >= curre.getLength()) {

			// Stop playing the last node
			try {
				currn.stop(this);
			} catch (InvalidMidiDataException e) {
				System.err.println("Error playing node!");
				e.printStackTrace();
			}
			
			// Update our current node
			currn = curre.getDestination();

			edgeDist = 0.0;
			status = NODE_PLAY;
		}

		// Node tasks
		if (NODE_PLAY == status) {
			
			/** PERFORM NODE ACTIONS **/
			try {
				lastEvent = currn.respond(lastEvent, this);
			} catch (InvalidMidiDataException e) {
				System.err.println("Error playing node!");
				e.printStackTrace();
			}
			
			/** CHOOSE AN EDGE **/

			// Check to see if we are at the end of the circuit
			if (0 == currn.getOutboundEdges().size()) {
				done = true;
				
				try {
					// N.B. The user will have to end the Score with a REST; otherwise, the last
					// Note will be very short, since it will be stopped immediately after its
					// containing Node is called to respond().
					currn.stop(this);
				} catch (InvalidMidiDataException e) {
					System.err.println("Error playing node!");
					e.printStackTrace();
				}
				
				return;
			}			
			
			// TODO: This will eventually be more complicated, once edges have weighted likelihoods
			int whichEdge = rand.nextInt(currn.getOutboundEdges().size());
			curre = currn.getOutboundEdges().get(whichEdge);
			
			/** GET READY FOR EDGE TASKS **/
			status = EDGE_WAIT;
			
			
		}

		// TODO: Tasks to perform while waiting at an edge
		if (EDGE_WAIT == status) {
			
		}
	}
	
	/**
	 * tick()
	 * 
	 * This method gets used by the ScoreTraverserConductor inside of
	 * GenSeq, in order to run timing operations here.
	 * 
	 * TODO: Currently, the tick() method assumes all edges use linear
	 * interpolation for all CC/SYSEX events. Add in other funcs as well.
	 * 
	 */
	public void tick() {
		if (done) return;
		
		// Adding a constant value each time means we have a linear progression
		// along the current edge.
		if (EDGE_WAIT == status)
			edgeDist += GenSeq.TIME_EDGE_RATIO;
			
		// Update the traverser
		run();
	}
	
	/**
	 * setLocation(Node n)
	 * Changes the current location of the ScoreTraverser to some other
	 * node. This is useful for jumping into and out of MetaNodes, or for
	 * responding to MIDI input.
	 * 
	 * @param n - The new location (Node) of this ScoreTraverser
	 */
	public void setLocation(Node n) {
		currn = n;
		status = NODE_PLAY;
	}
	
	/**
	 * stopTraverse() - Stops the traversal of the score.
	 * 
	 */
	public void stopTraverse() {
		done = true;
	}
	
	/**
	 * isDone() - Check to see if this traverser has finished.
	 * 
	 * @return True if finished, false otherwise.
	 */
	public boolean isDone() {
		return done;
	}
	
}
