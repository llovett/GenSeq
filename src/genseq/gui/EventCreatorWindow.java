package genseq.gui;

import genseq.midi.MIDIConstants;
import genseq.midi.Note;

import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.List;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class EventCreatorWindow extends Frame implements MIDIConstants {

	/*** INTERNAL CONSTANTS ***/
	private static final int DEFAULT_WIDTH = 400;
	private static final int DEFAULT_HEIGHT = 300;
	private static final int COMPONENT_GAP = 10;

	/*** EXTERNAL REFERENCES ***/
	private NodeAttributesWindow parent;

	/*** GUI COMPONENTS ***/
	private List pitchList;
	private Scrollbar velSlider, lhoodSlider;
	private Label velDisp, lhoodDisp;
	private TextField durationField;
	private Button closeButton, addButton;

	public EventCreatorWindow(NodeAttributesWindow parent) {
		this.parent = parent;

		constructComponents();
	}

	private void constructComponents() {
		// Allow the user to close the window
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				// Update note list in our parent
				updateNAW();
				
				setVisible(false);
			}
		});

		/** CREATE COMPONENTS **/

		// Pitch list
		pitchList = new List();
		for (String notename : PITCHES) {
			pitchList.add(notename);
		}
		pitchList.setMultipleMode(true);

		// Sliders for velocity and likelihood, respectively
		// Scrollbar(ORIENTATION, INITVALUE, VISIBLE, MIN, MAX + 1)
		velDisp = new Label("100");
		lhoodDisp = new Label("1.0");
		velSlider = new Scrollbar(Scrollbar.VERTICAL, 100, 1, 0, 128);
		lhoodSlider = new Scrollbar(Scrollbar.VERTICAL, 100, 1, 0, 101);
		velSlider.addAdjustmentListener(new velSliderAdjustmentListener());
		lhoodSlider.addAdjustmentListener(new lhoodSliderAdjustmentListener());

		// Duration field
		durationField = new TextField(10);
		durationField.setText("500");

		// Close and Add buttons
		closeButton = new Button("Close");
		closeButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
			
		});
		addButton = new Button("Add Note");
		addButton.addActionListener(new addButtonActionListener());
		

		/** CONSTRUCT WINDOW **/

		// Basic window setup
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		setResizable(false);
		setTitle("Note Editor");

		// Set up insets for pretty component spacing
		Insets insets = new Insets(COMPONENT_GAP, COMPONENT_GAP, COMPONENT_GAP, COMPONENT_GAP);

		// Add the pitch list, with label
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = insets;
		add(new Label("Pitch(es)"), constraints);
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridheight = 2;
		constraints.weighty = 1;
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = insets;
		add(pitchList, constraints);

		// Add the velocity slider, with label and display
		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weighty = 0;
		constraints.weightx = 0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = insets;
		add(new Label("Velocity"), constraints);
		constraints.gridx = 2;
		constraints.gridy = 1;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = insets;
		add(velSlider, constraints);
		constraints.gridx = 2;
		constraints.gridy = 2;
		constraints.weighty = 0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = insets;
		add(velDisp, constraints);

		// Add the likelihood slider, with label and display
		constraints.gridx = 3;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = insets;
		add(new Label("Likelihood"), constraints);
		constraints.gridx = 3;
		constraints.gridy = 1;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = insets;
		add(lhoodSlider, constraints);
		constraints.gridx = 3;
		constraints.gridy = 2;
		constraints.weighty = 0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = insets;
		add(lhoodDisp, constraints);

		// Add the duration field, with label
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = insets;
		add(new Label("Duration (ms)"), constraints);
		constraints.gridx = 1;
		constraints.gridy = 3;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = insets;
		add(durationField, constraints);

		// Create the close and add buttons
		constraints.gridx = 2;
		constraints.gridy = 3;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = insets;
		add(closeButton, constraints);
		constraints.gridx = 3;
		constraints.gridy = 3;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = insets;
		add(addButton, constraints);
		
		
		pack();
		setVisible(false);
	}

	/**
	 * updateNAW() - Update the NoteAttributesWindow (this.parent)
	 * 
	 * Uses current configurations to build notes, gives these notes over
	 * to our parent.
	 */
	private void updateNAW() {
		// Create a note list
		ArrayList<Note> notes = new ArrayList<Note>();
		for (String notename : pitchList.getSelectedItems()) {
			// Do a stupid linear search, in order to preserve our holy interface MIDIConstants
			int pitch = 0;
			for (; pitch < PITCHES.length; pitch++) {
				if (PITCHES[pitch].equals(notename))
					break;
			}
			
			Note n = new Note(pitch, velSlider.getValue(), Long.valueOf(durationField.getText()), Double.valueOf(lhoodSlider.getValue())/100.0);
			notes.add(n);
		}

		parent.notesEdited(notes);
	}

	/**
	 * processEvent() - Handles events on the EventCreatorWindow.
	 * 
	 * This method is overridden from Frame in order to allow
	 * only numeric characters in durationField.
	 * 
	 */
	protected void processEvent(AWTEvent e) {

		System.out.println("Inside processEvent!");
		
		if (e instanceof KeyEvent && e.getSource() == durationField) {

			if (((KeyEvent)e).getID() == KeyEvent.KEY_PRESSED) {
				char c = ((KeyEvent)e).getKeyChar();

				if (c >= '0' && c <= '9')
					super.processEvent(e);

				else if (Character.isISOControl(c))
					super.processEvent(e);

				// Else, discard the key
				return;
			}

		}

		super.processEvent(e);

	}

	private class velSliderAdjustmentListener implements AdjustmentListener {

		public void adjustmentValueChanged(AdjustmentEvent e) {
			velDisp.setText(String.valueOf(e.getValue()));
		}

	}

	private class lhoodSliderAdjustmentListener implements AdjustmentListener {

		public void adjustmentValueChanged(AdjustmentEvent e) {
			lhoodDisp.setText(String.valueOf(e.getValue()/100.0));
		}

	}

	private class addButtonActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			updateNAW();
		}
		
	}
	
}
