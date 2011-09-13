/**
 * MetaNode.java
 * 
 * A MetaNode is a special kind of Node that contains other Nodes.
 * When a ScoreTraverser reaches a MetaNode, the MN redirects the
 * Traverser to one of its prime Nodes contained within. The Traverser
 * then continues its path inside of the MN. When the Traverser reaches
 * a Node that has no outbound edges, then the MN redirects the Traverser
 * back outside the MN along one if its own outbound edges.
 * 
 * Who is in charge of managing the Nodes/Edges inside of the MN?
 * How do we render MetaNodes? IDEAS:
 * 	1. Double-click a MN. This shows only the contents of the MN on the canvas and nothing else.
 * 		Have some way of "zooming back out"
 * 	2. Open contents of MN in a new window w/ canvas
 * 	3. 
 * 
 */

package genseq.obj;

import genseq.midi.GenSeq;
import genseq.midi.Playable;
import genseq.midi.ScoreTraverser;
import javax.sound.midi.InvalidMidiDataException;
import java.util.ArrayList;
import java.util.Random;

public class MetaNode extends Node {

	private ArrayList<Node> nodes;
	private ArrayList<Edge>	edges;
	private Node currNode;
	private int metaID;
	
	public MetaNode(GenSeq parent, int metaID, int x, int y) {
		super(parent, x, y);
		this.metaID = metaID;
		currNode = null;
	}

	public MetaNode(Node n) {
		super(n);
		
	}
	
	public ArrayList<Node> getNodes() {
		return nodes;
	}

	public ArrayList<Edge> getEdges() {
		return edges;
	}
	
	public int getMetaID() {
		return metaID;
	}
	
	public void setMetaID(int metaID) {
		this.metaID = metaID;
	}
	
	/**
	 * respond()
	 * 
	 * What to do when called on to play.
	 * 
	 */
	public NodeEvent respond(NodeEvent lastEvent, ScoreTraverser t) throws InvalidMidiDataException {
		/*
		 * 1. Check to see if we have a currNode, if not,
		 * set it to be one of the prime nodes and set the Traverser.
		 * 2. Call on the current Node to respond
		 */
		
		if (null == currNode) {
			// Select a Prime node as our "entrance"
			ArrayList<Node> primeNodes = new ArrayList<Node>();
			for (Node n : nodes) {
				if (n.isPrimeNode())
					primeNodes.add(n);
			}
			Random rand = new Random();
			int whichPrime = rand.nextInt(primeNodes.size());
			currNode = primeNodes.get(whichPrime);
		
			// Create a new ScoreTraverser within this MetaNode
			ScoreTraverser st = new ScoreTraverser((GenSeq)parent, currNode);
			// Interrupt the old Traverser
			try {
				t.interrupt();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			st.start();
			t = st;
		}
		
		/*
		 * At this point, the ScoreTraverser is already on the right track. It will correctly
		 * choose Nodes and Edges that succeed its current position. However, we do need to
		 * know when/if the Traverser needs to exit this MN. 
		 */
		
		// Return the last NodeEvent, which could still be useful to the Node inside of this MetaNode.
		return lastEvent;
	}
	
	/**
	 * stop()
	 * The ScoreTraverser has reached a Node inside of this MetaNode with
	 * no outbound Edges.
	 * 
	 * @param t - The Traverser
	 * @throws InvalidMidiDataException
	 */
	public void stop(ScoreTraverser t) throws InvalidMidiDataException {
		// Create a new ScoreTraverser at this Node
		ScoreTraverser st = new ScoreTraverser((GenSeq)parent, this);
		// Interrupt the old Traverser
		try {
			t.interrupt();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		st.start();
		t = st;
	}
	
	public boolean equals(MetaNode mn) {
		return (mn.getMetaID() == this.getMetaID());
	}
}
