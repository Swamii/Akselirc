package irc.main;

import irc.gui.GUI;

public class Akselirc {

	/**
	 * Akselirc main-class
	 */
	public static void main(String[] args) {
		GUI gui = new GUI();
		gui.initGUI();
		gui.initStartupConnections();
	}
	
}
