package genseq.gui;

import genseq.midi.GenSeq;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import processing.core.PApplet;

public class GenSeqMenuBar extends MenuBar {

	private static GenSeq gs;
	private static PreferencesFrame pf;

	/*** FILE MENU ***/
	Menu FileMenu;
	/*** File ===>*/
		MenuItem File_New;
		MenuItem File_Open;
		MenuItem File_Save;
		MenuItem File_Close;
		MenuItem File_Exit;
		
	/*** EDIT MENU ***/
	Menu EditMenu;
	/*** Edit ===> */
		MenuItem Edit_Undo;
		MenuItem Edit_Redo;
		MenuItem Edit_Clear;
		// ---------------- //
		MenuItem Edit_Preferences;
		
	/*** SCORE MENU ***/
	Menu ScoreMenu;
	/*** Score ===> */
		MenuItem Score_Play;
		MenuItem Score_Stop;
		
		
		
		
		
		
	/***
	 * Constructor for the menu bar
	 * @param gs - Reference to the GenSeq application hosted inside
	 * of GenSeqWindow.
	 */
	public GenSeqMenuBar(GenSeq gs, PreferencesFrame pf) {
		this.gs = gs;
		this.pf = pf;
		
		constructMenus();
		
	}

	/***
	 * Construct the menus and submenus
	 */
	private void constructMenus() {
		FileMenu = new Menu("File");
		EditMenu = new Menu("Edit");
		ScoreMenu = new Menu("Score");
		
		// File menu items
		File_New = new MenuItem("New");
		File_New.addActionListener(new File_NewActionListener());
		File_Open = new MenuItem("Open");
		File_Open.addActionListener(new File_OpenActionListener());
		File_Save = new MenuItem("Save");
		File_Save.addActionListener(new File_SaveActionListener());
		File_Close = new MenuItem("Close");
		File_Close.addActionListener(new File_CloseActionListener());
		File_Exit = new MenuItem("Exit");
		File_Exit.addActionListener(new File_ExitActionListener());
		// Add the menu items to the menu
		FileMenu.add(File_New);
		FileMenu.add(File_Open);
		FileMenu.add(File_Save);
		FileMenu.add(File_Close);
		FileMenu.add(File_Exit);
		
		
		// Edit menu items
		Edit_Preferences = new MenuItem("Preferences");
		Edit_Preferences.addActionListener(new Edit_PreferencesActionListener());
		Edit_Undo = new MenuItem("Undo");
		Edit_Undo.addActionListener(new Edit_UndoActionListener());
		Edit_Redo = new MenuItem("Redo");
		Edit_Redo.addActionListener(new Edit_RedoActionListener());
		Edit_Clear = new MenuItem("Clear");
		Edit_Clear.addActionListener(new Edit_ClearActionListener());
		// Add the menu items to the menu
		EditMenu.add(Edit_Undo);
		EditMenu.add(Edit_Redo);
		EditMenu.add(Edit_Clear);
		EditMenu.addSeparator();
		EditMenu.add(Edit_Preferences);
		
		
		// Score menu items
		Score_Play = new MenuItem("Play");
		Score_Play.addActionListener(new Score_PlayActionListener());
		Score_Stop = new MenuItem("Stop");
		Score_Stop.addActionListener(new Score_StopActionListener());
		// Add the menu items to the menu
		ScoreMenu.add(Score_Play);
		ScoreMenu.add(Score_Stop);
		
		add(FileMenu);
		add(EditMenu);
		add(ScoreMenu);
	}

	
	/***************************************
	 * CLASSES DEFINING MENU ITEM BEHAVIOR *
	 **************************************/
	
	private class File_NewActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			System.out.println("File ===> New");
		}
		
	}

	private class File_OpenActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			System.out.println("File ===> Open");
		}
		
	}
	
	private class File_SaveActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.err.println("File ===> Save");
		}
		
	}

	private class File_CloseActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.err.println("File ===> Close");
		}
		
	}
	
	private class File_ExitActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			// Too simple an exit, obviously.
			// TODO: Make sure all work is saved before exiting,
			// and take care of any necessary "saving of state."
			System.exit(0);
		}
		
	}
	
	private class Edit_UndoActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.err.println("Edit ===> Undo");			
		}
		
	}
	
	private class Edit_RedoActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.err.println("Edit ===> Redo");			
		}
		
	}
	
	private class Edit_ClearActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.err.println("Edit ===> Clear");			
		}
		
	}
	
	private class Edit_PreferencesActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			pf.setVisible(true);
		}
		
	}
	
	private class Score_PlayActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			gs.playScore();
		}
		
	}
	
	private class Score_StopActionListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			gs.stopScore();
		}
		
	}
}
