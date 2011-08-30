package genseq.gui;

import genseq.midi.GenSeq;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.WindowConstants;

@SuppressWarnings("serial")
public class GenSeqWindow extends Frame {

	private static final int DEFAULT_WIDTH = 600;
	private static final int DEFAULT_HEIGHT = 600;

	static GenSeq gs;
	static PreferencesFrame pf;
	static GenSeqMenuBar gsmb;
	static ToolWindow gstw;
	
	public GenSeqWindow(String name) {
		super(name);
		init();
	}

	public void init() {
		// Allow the user to close the window
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		
		// Define the layout
		setLayout(new BorderLayout());
		
		// Construct and initialize the main application
		gs = new GenSeq(this);
		gs.init();
		gs.start();
		
		// Construct the preferences frame
		pf = new PreferencesFrame(gs);
		pf.setVisible(false);
		
		// Construct the tool frame
		gstw = new ToolWindow(gs, this);
		gstw.setVisible(true);
		
		// Construct the menu bar
		gsmb = new GenSeqMenuBar(gs, pf);
		
		// Set up the JFrame that hosts the application
		this.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		setTitle("Graphikal");
		this.setMenuBar(gsmb);
	
		
		gs.setVisible(true);
		
		add(gs, BorderLayout.CENTER);
		
		pack();
	}

	public static void main(String[] args) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				GenSeqWindow gsw = new GenSeqWindow(GenSeq.NAME + " " + String.valueOf(GenSeq.VERSION));
				gsw.setVisible(true);
			}
		});
	}
	
}