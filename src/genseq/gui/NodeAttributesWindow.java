package genseq.gui;

import genseq.midi.MIDIConstants;
import genseq.midi.MidiCommon;
import genseq.midi.Note;
import genseq.obj.NodeEvent;
import genseq.obj.Node;
import java.awt.AWTEvent;
import java.awt.AWTEventMulticaster;
import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Component;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Checkbox;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import processing.core.PApplet;

@SuppressWarnings("serial")
public class NodeAttributesWindow extends Frame implements MIDIConstants {

	/*** BASIC LOOK & FEEL CONSTANTS ***/
	private static final int DEFAULT_WIDTH = 500;
	private static final int DEFAULT_HEIGHT = 600;
	private static final int COMPONENT_GAP = 10;	// The amount of space (px) between components

	// How wide "Remove", "Insert", and "Save" buttons are at bottom of the Frame
	private static final int NL_BUTTONS_WIDTH = 400;

	/*** EXTERNAL REFERENCES ***/
	private PApplet parent;
	private Node node;

	/*** INTERNAL REFERENCES / CONTROL ***/
	private ArrayList<NodeEvent> nodeEventList;
	private int currEvent;

	/*** GUI COMPONENTS ***/
	private Checkbox primeBox, sonBox; // sonBox = "stop other notes box"
	private Label pitchLabel;
	private List pitchList;
	private ValueSlider velSlider, lhoodSlider;
	private Label velDisp, lhoodDisp;
	private Button remButton, insButton, saveButton;
	private Button forwardButton, backwardButton;
	private Label whichEvent;
	private Button closeButton;

	public NodeAttributesWindow(PApplet parent, Node node) {
		this.parent = parent;
		this.node = node;
		
		nodeEventList = node.getEventList();

		constructComponents();
	}

	private void constructComponents() {
		// Allow the user to close the window
		// When the window closes, save all preferences for this node.
		addWindowListener(new WindowAdapter() {
			
			public void windowClosing(WindowEvent we) {
				// Update our node
				updateNode();
			}
			
		});
		
//		// How this window should behave per usage of the keyboard
//		addKeyListener(new KeyListener() {
//
//			@Override
//			public void keyPressed(KeyEvent e) {
//				System.out.println("NAW : Key pressed!");
//				
//				switch (e.getKeyCode()) {
//				
//				case KeyEvent.VK_ESCAPE:
//					setVisible(false);
//					break;
//					
//				}
//			}
//
//			@Override
//			public void keyReleased(KeyEvent e) {
//				
//			}
//
//			@Override
//			public void keyTyped(KeyEvent e) {
//				
//			}
//			
//		});

		// Basic window setup
		setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		setLayout(new GridBagLayout());
		this.setBackground(new Color(100, 100, 100));
		this.setForeground(Color.WHITE);
		GridBagConstraints constraints = new GridBagConstraints();
		setResizable(false);
		setTitle("Node Attributes");
		
		int row = 0; // Use this variable for inserting components at a vertical position.
		// Much easier to do row++ than modify every freakin' component in this window.

		/*** CONSTRUCT COMPONENTS ***/

		// Construct prime checkbox
		primeBox = new Checkbox();
		primeBox.setState(node.isPrimeNode());
		primeBox.addItemListener(new primeBoxItemListener());
		sonBox = new Checkbox();
		sonBox.setState(node.isLegato());
		sonBox.addItemListener(new sonBoxItemListener());
		setConstraints(constraints, 0, row);
		add(new Label("Prime"), constraints);
		setConstraints(constraints, 1, row++);
		add(primeBox, constraints);
		setConstraints(constraints, 0, row);
		add(new Label("Legato"), constraints);
		setConstraints(constraints, 1, row++);
		add(sonBox, constraints);

		// Construct pitch field
		pitchLabel = new Label("Event List contents: ");

		setConstraints(constraints, 0, row++, 4, 1);
		add(pitchLabel, constraints);







		/** CREATE COMPONENTS **/

		// Pitch list
		pitchList = new List();
		for (String notename : PITCHES) {
			pitchList.add(notename);
		}
		pitchList.setMultipleMode(true);
		pitchList.setBackground(new Color(50, 50, 50));
		pitchList.setForeground(Color.WHITE);


		// Sliders for velocity and likelihood, respectively
		// ValueSlider(ORIENTATION, INITVALUE, VISIBLE, MIN, MAX + 1)
		velDisp = new Label("100");
		lhoodDisp = new Label("1.0");
		velSlider = new ValueSlider(ValueSlider.VERTICAL, 100, 0, 127, 0);
		lhoodSlider = new ValueSlider(ValueSlider.VERTICAL, 100, 0, 100, 0);
		velSlider.addAdjustmentListener(new velSliderAdjustmentListener());
		lhoodSlider.addAdjustmentListener(new lhoodSliderAdjustmentListener());

		// Construct buttons for navigating and editing note lists
		backwardButton = new Button("<< prior");
		backwardButton.addActionListener(new backwardButtonActionListener());
		forwardButton = new Button("next >>");
		forwardButton.addActionListener(new forwardButtonActionListener());
		whichEvent = new Label("asdfasdfsdf");

		// Add Notelist editing buttons
		remButton = new Button("Remove");
		//remButton.setPreferredSize(new Dimension(NL_BUTTONS_WIDTH,remButton.getHeight()));
		remButton.addActionListener(new remButtonActionListener());
		insButton = new Button("Insert");
		//insButton.setPreferredSize(new Dimension(NL_BUTTONS_WIDTH,insButton.getHeight()));
		insButton.addActionListener(new insButtonActionListener());
		saveButton = new Button("Save");
		//saveButton.setPreferredSize(new Dimension(NL_BUTTONS_WIDTH,saveButton.getHeight()));
		saveButton.addActionListener(new saveButtonActionListener());

		// Close and Add buttons
		closeButton = new Button("Close");
		closeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}

		});





		// Add the pitch list, with label
		setConstraints(constraints, 0, row, 2, 1);
		add(new Label("Pitch(es)"), constraints);
		setConstraints(constraints, 0, row+1, 2, 2, 1, 1);
		add(pitchList, constraints);

		// Add the velocity slider, with label and display
		setConstraints(constraints, 2, row);
		add(new Label("Velocity"), constraints);
		setConstraints(constraints, 2, row+1, 1, 1, 0, 1);
		add(velSlider, constraints);
		setConstraints(constraints, 2, row+2);
		add(velDisp, constraints);

		// Add the likelihood slider, with label and display
		setConstraints(constraints, 3, row);
		add(new Label("Likelihood"), constraints);
		setConstraints(constraints, 3, row+1, 1, 1, 0, 1);
		add(lhoodSlider, constraints);
		setConstraints(constraints, 3, row+2);
		add(lhoodDisp, constraints);

		row+=3;
		
		// Create notelist navigation buttons with label
		setConstraints(constraints, 0, row, 1, 1, 0.5, 0);
		add(backwardButton, constraints);
		setConstraints(constraints, 1, row, 1, 1, 0.5, 0);
		add(forwardButton, constraints);
		// It seems we have to create a new GridBagConstraints here, since using
		// our nice setConstraints() method will cause this label to take over
		// the bottom-right hand corner of the window, causing the "add" and "close"
		// buttons not to work.
		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.gridx = 2;
		labelConstraints.gridy = row++;
		labelConstraints.fill = GridBagConstraints.HORIZONTAL;
		add(whichEvent, labelConstraints);

		// Create buttons for editing notelists
		GridBagConstraints lnButtonsContraints = new GridBagConstraints();
		lnButtonsContraints.gridx = 0;
		lnButtonsContraints.gridy = row;
		lnButtonsContraints.insets = new Insets(0, COMPONENT_GAP, COMPONENT_GAP, COMPONENT_GAP);
		lnButtonsContraints.fill = GridBagConstraints.NONE;
		remButton.setPreferredSize(new Dimension(NL_BUTTONS_WIDTH, remButton.getHeight()));
		add(remButton, lnButtonsContraints);

		lnButtonsContraints.gridx = 1;
		lnButtonsContraints.gridy = row;
		lnButtonsContraints.insets = new Insets(0, COMPONENT_GAP, COMPONENT_GAP, COMPONENT_GAP);
		lnButtonsContraints.fill = GridBagConstraints.NONE;
		insButton.setPreferredSize(new Dimension(NL_BUTTONS_WIDTH, insButton.getHeight()));
		add(insButton, lnButtonsContraints);



		lnButtonsContraints.gridx = 2;
		lnButtonsContraints.gridy = row++;
		lnButtonsContraints.insets = new Insets(0, COMPONENT_GAP, COMPONENT_GAP, COMPONENT_GAP);
		lnButtonsContraints.fill = GridBagConstraints.NONE;
		saveButton.setPreferredSize(new Dimension(NL_BUTTONS_WIDTH, saveButton.getHeight()));
		add(saveButton, lnButtonsContraints);

		// Create the close and add buttons
		setConstraints(constraints, 3, row);
		add(closeButton, constraints);

		// Load the first event
		loadEvent(0);

		pack();
		setVisible(true);
	}

	private void setConstraints(GridBagConstraints constraints, int x, int y, int w, int h, double wx, double wy, Insets insets) {
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = w;
		constraints.gridheight = h;
		constraints.weightx = wx;
		constraints.weighty = wy;
		constraints.insets = insets;
		constraints.fill = GridBagConstraints.BOTH;
	}

	private void setConstraints(GridBagConstraints constraints, int x, int y, int w, int h, double wx, double wy) {
		setConstraints(constraints, x, y, w, h, wx, wy, new Insets(COMPONENT_GAP, COMPONENT_GAP, COMPONENT_GAP, COMPONENT_GAP));
	}

	private void setConstraints(GridBagConstraints constraints, int x, int y, int w, int h) {
		setConstraints(constraints, x, y, w, h, 0, 0, new Insets(COMPONENT_GAP, COMPONENT_GAP, COMPONENT_GAP, COMPONENT_GAP));
	}

	private void setConstraints(GridBagConstraints constraints, int x, int y) {
		setConstraints(constraints, x, y, 1, 1, 0, 0, new Insets(COMPONENT_GAP, COMPONENT_GAP, COMPONENT_GAP, COMPONENT_GAP));
	}



	/**
	 * addToPitchField - append text to the TextField pitchField .
	 * This method gets used EventCreatorWindow.
	 * 
	 * @param s - The string that is to be appended.
	 */
	public void addToPitchField(String s) {
		if (null != pitchLabel) {
			StringBuilder newtext = new StringBuilder(pitchLabel.getText());
			if (newtext.length() > 0)
				newtext.append(", ");

			newtext.append(s);

			pitchLabel.setText(newtext.toString());
		}
	}

	/**
	 * loadEvent() - Load a particular event from the Node's eventlist.
	 * 
	 * @param eventno - Index of the event to load
	 * 
	 * If eventno is negative, loadEvent will return without doing anything.
	 * If eventno is greater than the number of events in the eventList, and
	 * 	the eventList is nonempty, then loadEvent will return without doing anything.
	 * If the eventList is empty, then loadEvent will create a blank event (a REST)
	 * 	and load it.
	 * 
	 */
	private void loadEvent(int eventno) {
		if (eventno < 0)
			return;

		NodeEvent e = null;

		if (nodeEventList.size() == 0) {
			e = new NodeEvent();
			nodeEventList.add(e);
		}
		else if (eventno < nodeEventList.size())
			e = nodeEventList.get(eventno);
		else
			return;

		// Deselect all items
		for (int i = 0; i<pitchList.getItemCount(); i++)
			pitchList.deselect(i);
		
		for (Note n : e.getNotes()) {
			int pitch = n.getPitch();
			int velocity = n.getVelocity();

			// Select items that are in the event
			if (REST == n.getPitch())
				pitchList.select(0);
			else
				// pitch + 1, because "REST" is the first "pitch" to appear on pitchList
				pitchList.select(pitch + 1);

			velSlider.setValue(velocity);

		}
		lhoodSlider.setValue((int)(e.getLikelihood()*100.0));

		currEvent = eventno;

		whichEvent.setText("Event "+(currEvent+1)+" of "+nodeEventList.size());


		// Set the text in the field based on the current selection of notes.
		// This selection might be a list of singleton notes, or could be
		// a List of single notes and pitch clusters (which will be encoded
		// as an ArrayList<Note>).
		StringBuilder s = new StringBuilder();
		for (NodeEvent ne : nodeEventList) {
			s.append(ne.toString());
			s.append(", ");
		}

		if (nodeEventList.size() >= 1)
			// Remove comma and space
			s.delete(s.length() - 2, s.length());

		pitchLabel.setText("Event List contents: "+s.toString());

	}


	/**
	 * notesEditied()
	 * 
	 * @param notes - Notes that were created
	 * 
	 * Appends notes to the current list of notes,
	 * and updates the display in pitchField.
	 * 
	 */
	public void saveEvent(int index, NodeEvent ne) {
		/** VALIDATION **/
		if (ne.isEmpty())
			return;

		if (index > nodeEventList.size())
			return;
		
		// Don't allow other pitches to be mixed with the "REST" event.
		boolean isRest = false;
		for (Note n : ne.getNotes()) {
			if (REST == n.getPitch())
				isRest = true;
		}
		if (isRest && NodeEvent.TYPE_REST != ne.getType()) {
			ne = new NodeEvent(new Note(REST));
		}

		// If we're updating the eventList, first remove the old event
		if (index < nodeEventList.size())
			nodeEventList.remove(index);

		nodeEventList.add(index, ne);
	}

	/**
	 * Update the node we were called from.
	 */
	private void updateNode() {
		// Save preferences
		node.setEventList(nodeEventList);

		// Refresh the node's properties.
		node.refresh();

		setVisible(false);
	}
	
	// TODO: This method is never called...
	protected void processKeyEvent(KeyEvent e) {
		System.out.println("Bing!");
		
		switch (e.getKeyCode()) {
		
		case KeyEvent.VK_ESCAPE:
			setVisible(false);
			
		default:
			super.processKeyEvent(e);
			
		}
	}


	/******************
	 * INTERNAL CLASSES 
	 *****************/
	
	
	private class primeBoxItemListener implements ItemListener {

		public void itemStateChanged(ItemEvent e) {
			node.setPrime(primeBox.getState());
			node.refresh();
		}

	}
	
	private class sonBoxItemListener implements ItemListener {
		
		public void itemStateChanged(ItemEvent e) {
			node.setLegato(sonBox.getState());
			node.refresh();
		}
		
	}

	private class velSliderAdjustmentListener implements AdjustmentListener {

		public void adjustmentValueChanged(AdjustmentEvent e) {
			// These are scorllbars, not sliders, so up = less, down = more.
			velDisp.setText(String.valueOf(e.getValue()));
		}

	}

	private class lhoodSliderAdjustmentListener implements AdjustmentListener {

		public void adjustmentValueChanged(AdjustmentEvent e) {
			lhoodDisp.setText(String.valueOf(e.getValue()/100.0));
		}

	}

	private class forwardButtonActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (currEvent < nodeEventList.size())
				loadEvent(++currEvent);
		}

	}

	private class backwardButtonActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (currEvent >= 1)
				loadEvent(--currEvent);
		}

	}

	private class remButtonActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			// Make sure this event exists
			if (currEvent < nodeEventList.size() && nodeEventList.size() > 0)
				nodeEventList.remove(currEvent);

			if (0 == currEvent)
				loadEvent(0);
			else
				loadEvent(--currEvent);
		}

	}


	private class insButtonActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			// Create a note list
			ArrayList<Note> notes = new ArrayList<Note>();
			for (String notename : pitchList.getSelectedItems()) {
				// Strip off any pitch descriptions from notename (e.g. " --- Middle C")
				notename = notename.split(" ")[0];

				int pitch = MidiCommon.getPitchFromString(notename);

				Note n = new Note(pitch, velSlider.getValue());

				notes.add(n);
			}

			int ins = nodeEventList.size();
			saveEvent(ins, new NodeEvent(notes, lhoodSlider.getValue()/100.0));
			loadEvent(ins);
		}

	}


	private class saveButtonActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			// Create a note list
			ArrayList<Note> notes = new ArrayList<Note>();
			for (String notename : pitchList.getSelectedItems()) {
				int pitch = MidiCommon.getPitchFromString(notename);

				Note n = new Note(pitch, velSlider.getValue());
				notes.add(n);
				
			}

			saveEvent(currEvent, new NodeEvent(notes, lhoodSlider.getValue()/100.0));
			loadEvent(currEvent);
		}

	}


	//	/**
	//	 * ekActivatesKeyListener
	//	 *
	//	 * Generic KeyListener that will make a button act like it was clicked
	//	 * if it is selected and <ENTER> is pressed.
	//	 * 
	//	 */
	//	private class ekActivatesKeyListener implements KeyListener {
	//
	//		public void keyPressed(KeyEvent e) {
	//			if (e.getKeyCode() == KeyEvent.VK_ENTER)
	//				((Component)e.getSource()).
	//		}
	//
	//		@Override
	//		public void keyReleased(KeyEvent arg0) {
	//			// TODO Auto-generated method stub
	//			
	//		}
	//
	//		@Override
	//		public void keyTyped(KeyEvent arg0) {
	//			// TODO Auto-generated method stub
	//			
	//		}
	//		
	//		
	//		
	//	}
	


}