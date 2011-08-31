package genseq.gui;

import genseq.midi.GenSeq;
import genseq.midi.MidiCommon;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Frame;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Label;
import java.awt.Event;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;


@SuppressWarnings("serial")
public class PreferencesFrame extends Frame {
	
	/*** BASIC LOOK & FEEL CONSTANTS ***/
	private static final int DEFAULT_WIDTH = 200;
	private static final int DEFAULT_HEIGHT = 300;
	private static final int COMPONENT_GAP = 10;
	
	/*** EXTERNAL REFERENCES ***/
	// The primary application (a PApplet)
	private static GenSeq gsApplication;
	
	/*** MIDI RESOURCES ***/
	private static ArrayList<MidiDevice.Info> synthInfoList;
	private static MidiDevice midiDevice;
	private static MidiDevice.Info[] synthInfoArray;
	
	/*** GUI COMPONENTS ***/
	private static Label deviceListLabel;
	private static Choice deviceList;
	private static Button closeButton;
	
	
	/**
	 * Constructor for the preferences view (GenSeqPreferencesApplet)
	 * 
	 * @param parent - Reference to the JFrame that hosts this view
	 */
	public PreferencesFrame(GenSeq gsApplication) {
		super();
		
		PreferencesFrame.gsApplication = gsApplication;
		
		// Construct and initialize MIDI resources
		synthInfoList = new ArrayList<MidiDevice.Info>();
		refreshDevices();
		
		init();
	}

	private void init() {
		// Allow the user to close the window
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				setVisible(false);
			}
		});
		
		// Set up window decorations and dimensions
		setResizable(false);
		setTitle(GenSeq.NAME + " Preferences");
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screen.width/2 - getWidth()/2, screen.height/2 - getHeight()/2);
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		
		// Now build the components
		deviceListLabel = new Label("Choose a MIDI Device:");
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weighty = 0;
		constraints.weightx = 1;
		constraints.insets = new Insets(COMPONENT_GAP, COMPONENT_GAP, COMPONENT_GAP, COMPONENT_GAP);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		add(deviceListLabel, constraints);
		
		deviceList = new Choice();
		for (MidiDevice.Info synthInfo : synthInfoList) {
			deviceList.add(synthInfo.toString());
		}
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weighty = 1;
		constraints.weightx = 1;
		constraints.insets = new Insets(COMPONENT_GAP, COMPONENT_GAP, COMPONENT_GAP, COMPONENT_GAP);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		add(deviceList, constraints);
		
		closeButton = new Button("close");
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weighty = 0;
		constraints.weightx = 1;
		constraints.insets = new Insets(COMPONENT_GAP, COMPONENT_GAP, COMPONENT_GAP, COMPONENT_GAP);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.PAGE_END;
		add(closeButton, constraints);
		
		pack();
		setVisible(true);
	}
	
	
	
	
	
	
	
	/**
	 * refreshDevices()
	 * 
	 * Scan the system for available MIDI devices.
	 */
	private void refreshDevices() {
		
		synthInfoArray = MidiSystem.getMidiDeviceInfo();
		synthInfoList.clear();
		
		// Get information about what MIDI devices are available.
		for (MidiDevice.Info synthInfo : synthInfoArray) {
			// We only care about devices that can transmit MIDI
			MidiDevice theDevice;
			
			try {
				theDevice = MidiSystem.getMidiDevice(synthInfo);
			} catch (MidiUnavailableException e1) {
				continue;
			}
			
			if (theDevice.getMaxReceivers() == 0) continue;
			
			try {
				midiDevice = MidiSystem.getMidiDevice(synthInfo);
			} catch (Exception e) {
				System.err.println("Trouble finding information for "+synthInfo.toString());
				e.printStackTrace();
			}
			
			synthInfoList.add(synthInfo);
		}

	}
	
	
	/**************
	 * LISTENERS
	 *************/
	
	public boolean action(Event event, Object arg) {
		// Close button action
		if (event.target.equals(closeButton)) {
			setVisible(false);
			return true;
		}
		
		// Device list action
		if (event.target.equals(deviceList)) {
			int deviceNo = deviceList.getSelectedIndex();
			
			try {
				midiDevice = MidiSystem.getMidiDevice(synthInfoList.get(deviceNo));
				midiDevice.open();
				MidiCommon.setMidiDevice(midiDevice);
			} catch (MidiUnavailableException e1) {
				System.err.println("Unable to open MIDI Device : "+synthInfoList.get(deviceNo).toString());
				System.err.println("It could be that another application has reserved this device. Try shutting down other " +
						"sequencer applications and try again.");
				
				//TODO: Remove this after debugging.
				e1.printStackTrace();
			} 
			
			return true;
		}
		
		return false;
	}
}
