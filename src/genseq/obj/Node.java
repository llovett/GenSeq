package genseq.obj;

import genseq.midi.*;
import processing.core.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.ShortMessage;

public class Node extends DrawableObject implements MIDIConstants {

	/*** INTERNAL CONSTANTS ***/
	private static final int DEFAULT_WIDTH = 30;
	private static final int DEFAULT_HEIGHT = 30;
	private static final int DEFAULT_RED = 255;
	private static final int DEFAULT_GREEN = 100;
	private static final int DEFAULT_BLUE = 0;
	private static final int DEFAULT_STROKE_RED = 200;
	private static final int DEFAULT_STROKE_GREEN = 50;
	private static final int DEFAULT_STROKE_BLUE = 0;
	private static final int DEFAULT_STROKE_WEIGHT = 3;
	private static final int PN_RED = 100;
	private static final int PN_GREEN = 255;
	private static final int PN_BLUE = 0;
	private static final int PN_STROKE_RED = 50;
	private static final int PN_STROKE_GREEN = 200;
	private static final int PN_STROKE_BLUE = 0;
	private static final int HIGHLIGHT_RED = 255;
	private static final int HIGHLIGHT_GREEN = 255;
	private static final int HIGHLIGHT_BLUE = 100;
	private static final int HIGHLIGHT_STROKE_RED = 200;
	private static final int HIGHLIGHT_STROKE_GREEN = 200;
	private static final int HIGHLIGHT_STROKE_BLUE = 0;

	/*** INTERNAL CONTROL / ATTRIBUTES ***/
	// List of the edges incident to this node
	private ArrayList<Edge> edges;			// "edges" contains any edge into or out of this node.
	private ArrayList<Edge> inboundEdges;	// "inboundEdges" contains only edges into this node.
	private ArrayList<Edge> outboundEdges;	// "outboundEdges" contains only edges out of this node.

	private int circuitID;	// Circuit identification number
	private boolean prime;	// Whether or not the sequencer should begin playing with this node
	private ArrayList<NodeEvent> eventList;	// List of pitches that may be played


	/*** MIDI CONTROL ***/
	private static Receiver midisend;

	/**
	 * CONSTRUCTOR
	 * @param parent - PApplet that will do the rendering
	 * @param circuitID - Number identifying the circuit of nodes to which this node belongs.
	 * @param x - x-coordinate of this node, graphically
	 * @param y - y-coordinate of this node, graphically
	 */
	public Node(GenSeq parent, int circuitID, int x, int y) {
		super(parent);

		setX(x);
		setY(y);
		colorize();

		edges = new ArrayList<Edge>();
		inboundEdges = new ArrayList<Edge>();
		outboundEdges = new ArrayList<Edge>();

		prime = false;
		eventList = new ArrayList<NodeEvent>();

		// Set up MIDI receiver
		try {
			midisend = ((GenSeq)parent).getMidiDevice().getReceiver();
		} catch (Exception e) {

			// If that failed, try again with the default receiver
			try {
				midisend = MidiSystem.getReceiver();
			} catch (MidiUnavailableException e1) {
				System.err.println("Could not open the default MIDI receiver. Check that other sequencer applications "
						+ "have not reserved this device and try again.");
				e.printStackTrace();
			}

		}

		// Create an event to be played on this node. Make it a "REST"
		eventList.add(new NodeEvent());
	}

	/**
	 * setReceiver() - Change all nodes' MIDI receiver.
	 * 
	 * @param r - New MIDI receiver that will receive midi messages
	 * from all nodes.
	 */
	public static void setReceiver(Receiver r) {
		midisend = r;
	}

	/**
	 * render() - draw this object
	 *
	 **/
	public void render() {
		setupDrawPrefs();

		parent.pushMatrix();
		parent.translate(x, y);
		parent.ellipse(0, 0, w, w);
		parent.popMatrix();
	}

	/**
	 * respond(NodeEvent lastEvent) - what to do when this node is reached.
	 *
	 * @param lastEvent - The last event that was played.
	 * @return - The event that was just played.
	 *
	 * @throws InvalidMidiDataException 
	 **/
	public NodeEvent respond(NodeEvent lastEvent) throws InvalidMidiDataException {

		// What event shall we play?
		double lsum = 0.0;
		for (NodeEvent ne : eventList)
			lsum += ne.getLikelihood();

		Random rand = new Random();
		double evDecider = rand.nextDouble() * lsum;
		NodeEvent theEvent = null;

		Iterator<NodeEvent> i = eventList.iterator();
		while (evDecider > 0.0) {
			theEvent = i.next();
			evDecider -= theEvent.getLikelihood();
		}

		ArrayList<Note> notes = theEvent.getNotes();

		ShortMessage[] messages = null;

		// Check to see if the event is a REST event
		if (NodeEvent.TYPE_REST == theEvent.getType()) {
			messages = new ShortMessage[lastEvent.getNotes().size()];
			
			Iterator<Note> lei = lastEvent.getNotes().iterator();
			for (int j = 0; j<messages.length; j++) {
				Note curr = lei.next();
				
				messages[j] = new ShortMessage();
				messages[j].setMessage(ShortMessage.NOTE_OFF, 0, curr.getPitch(), curr.getVelocity());
			}
		}
		else {
			messages = new ShortMessage[notes.size()];
			//		// These will be used to calculate the timestamp on the MidiMessages.
			//		// Some amount of time is required to do all the constructing of the messages,
			//		// plus time taken by the operating system, other threads, etc. This will help
			//		// to compensate for that time taken, in order to ensure our MIDI timings come
			//		// out correctly.
			//		long[] creationTimes = new long[notes.size()];
			//		long currTime = ((GenSeq)parent).getMidiDevice().getMicrosecondPosition();
			//		
			for (int j = 0; j<messages.length; j++) {
				Note n = notes.get(j);

				messages[j] = new ShortMessage();

				// setMessage(COMMAND, MIDI CHANNEL, MIDI NOTE, VELOCITY)
				// TODO: Will we support multiple MIDI channels?
				messages[j].setMessage(ShortMessage.NOTE_ON, 0, n.getPitch(), n.getVelocity());
			}

		}

		// Two for-loops for minimum latency
		for (ShortMessage msg : messages)
			midisend.send(msg, 0);

		highlight();

		return theEvent;
		
	}

	/**
	 * stop() - what to do when the next node is reached.
	 * 
	 * This should be "the opposite" of respond. If respond did a NOTEON message,
	 * this should send a NOTEOFF message. Otherwise, the ScoreTraverser will continuously
	 * be playing notes / triggering events, and they will never cease.
	 * @throws InvalidMidiDataException 
	 * 
	 */
	public void stop() throws InvalidMidiDataException {

		// Dummy stop
		ShortMessage msg = new ShortMessage();
		msg.setMessage(ShortMessage.NOTE_OFF, 1, 60, 100);

		colorize();
	}

	/**
	 * registerEdge(Edge e) - Let this node know that edge e is incident to it.
	 * 
	 * @param e - the incident edge.
	 */
	public void registerEdge(Edge e) {
		if (e.getDestination().equals(this))
			inboundEdges.add(e);
		else
			outboundEdges.add(e);

		edges.add(e);
	}

	/**
	 * getEdges() - get all edges into or out of this node.
	 * 
	 * @return An ArrayList of edges.
	 */
	public ArrayList<Edge> getEdges() {
		return edges;
	}

	/**
	 * getInboundEdges - get the edges going into this node.
	 * 
	 * @return An ArrayList of the inbound edges
	 */
	public ArrayList<Edge> getInboundEdges() {
		return inboundEdges;
	}

	/**
	 * getOutboundEdges - get the edges coming out of this node.
	 * 
	 * @return An ArrayList of the outbound edges
	 */
	public ArrayList<Edge> getOutboundEdges() {
		return outboundEdges;
	}

	/**
	 * @return - Whether or not this node is a prime node.
	 */
	public boolean isPrimeNode() {
		return prime;
	}

	/**
	 * Set whether or not the sequencer should begin playing with this node.
	 * 
	 * @param prime - "true" means that playback will begin with this node.
	 * "false" means that playback must reach this node for this node to play.
	 */
	public void setPrime(boolean prime) {
		this.prime = prime;

		// Reset the colors for this node.
		colorize();
	}

	/**
	 * getNotes()
	 * 
	 * @return - A list of notes that this node may represent.
	 * Note that the ArrayList has a parameterized type of Object,
	 * rather than Note. This is because a node may represent a chord,
	 * which is encoded as a List. Thus, this list has to deal with
	 * both Notes and ArrayLists.
	 * 
	 */
	public ArrayList<NodeEvent> getEventList() {
		return eventList;
	}

	/**
	 * setNotes()
	 * 
	 * @param notes - New notes that this node may represent
	 */
	public void setEventList(ArrayList<NodeEvent> notes) {
		this.eventList = notes;
	}

	//	/**
	//	 * getAttributes()
	//	 * 
	//	 * @return - A hashtable containing pairs of keys (attribute names as strings)
	//	 * and values (various data structures representing the values for each respective key).
	//	 */
	//	public Hashtable<String, AttPair> getAttributes() {
	//		return attributes;
	//	}

	/**
	 * refresh()
	 * 
	 * Refreshes this node's properties after editing them inside NoteAttributesWindow
	 */
	@SuppressWarnings("unchecked")
	public void refresh() {
		// Whether this node is prime or not
		//		prime = (Boolean)attributes.get("prime").getData();
		//		notes= (ArrayList<Note>)attributes.get("notes").getData();
		//		
		colorize();
	}

	/**
	 * equals()
	 * 
	 * Returns true if this node is equal with respect to graphical position
	 * to another node.
	 * 
	 * TODO: Make sure that the attributes are equal for each node as well.
	 * 
	 * @param n - Node of comparison.
	 * @return - True if the nodes are equal w.r.t. location, false otherwise.
	 */
	public boolean equals(Node n) {
		return (getX() == n.getX() && getY() == n.getY());
	}

	public int compareTo(Node n) {
		double thisDist = Math.sqrt(Math.pow(getX(), 2) + Math.pow(getY(), 2));
		double otherDist = Math.sqrt(Math.pow(n.getX(), 2) + Math.pow(n.getY(), 2));
		return (int)(thisDist - otherDist);
	}




	/*****************
	 * PRIVATE METHODS
	 *****************/

	private void colorize() {
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		setStrokeWeight(DEFAULT_STROKE_WEIGHT);

		if (! prime) {
			setColor(DEFAULT_RED, DEFAULT_GREEN, DEFAULT_BLUE);
			setStrokeColor(DEFAULT_STROKE_RED, DEFAULT_STROKE_GREEN, DEFAULT_STROKE_BLUE);
		}
		else {
			setColor(PN_RED, PN_GREEN, PN_BLUE);
			setStrokeColor(PN_STROKE_RED, PN_STROKE_GREEN, PN_STROKE_BLUE);
		}
	}

	private void highlight() {
		setColor(HIGHLIGHT_RED, HIGHLIGHT_GREEN, HIGHLIGHT_BLUE);
		setStrokeColor(HIGHLIGHT_STROKE_RED, HIGHLIGHT_STROKE_GREEN, HIGHLIGHT_STROKE_BLUE);
	}

	//	private void setAttributes() {
	//		// Whether or not this node is prime
	//		attributes.put("prime",
	//				new AttPair<String, Boolean>("Use as prime node", 
	//						new Boolean(false)));
	//		
	//		// A list of possible pitches this node might represent
	//		attributes.put("notes",
	//				new AttPair<String, ArrayList<Note>>("Notes",
	//						new ArrayList<Note>()));
	//		
	//	}




	/*******************
	 * INTERNAL CLASSES
	 ******************/

	public class AttPair<String, V> {

		private String desc;
		private V data;

		public AttPair(String desc, V data) {
			this.desc = desc;
			this.data = data;
		}

		public String getDescription() {
			return desc;
		}

		public V getData() {
			return data;
		}

		public void setData(V data) {
			this.data = data;
		}

	}

	//	/**
	//	 * NodeAttributes
	//	 * 
	//	 * Provides a nice way to transport Node attributes.
	//	 * Makes communication with NodeAttributesWindow easier.
	//	 * 
	//	 */
	//	public class NodeAttributes {
	//		
	//		private Hashtable<String, Object> attributes;
	//		private Node parent;
	//		
	//		public NodeAttributes(Node parent) {
	//			this.parent = parent;
	//		}
	//		
	//		/**
	//		 * update() - Read attributes from parent node and store
	//		 * it in the table.
	//		 */
	//		public void update() {
	//			
	//		}
	//		
	//	}

}
