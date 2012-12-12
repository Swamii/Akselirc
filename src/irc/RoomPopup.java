package irc;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * 
 * @author Akseli Nelander
 * This class creates a popup where you can join a room.
 *
 */

public class RoomPopup extends JDialog {

	private static final long serialVersionUID = 1L;
	private static RoomPopup RoomPopupInstance = null;
	private RoomFieldListener keyListener;
	private RoomButtonListener mouseListener;
	private JComboBox<String> serverChoice;
	private JFormattedTextField roomField;
	private JButton roomOkButton;
	private JButton roomNoButton;
	private JLabel info;
	private GUI gui;
	
	public RoomPopup() {
		gui = GUI.gui;
		keyListener = new RoomFieldListener();
		mouseListener = new RoomButtonListener();
		initGUI();
	}
	
	private void initGUI() {
		ArrayList<Connection> connections = gui.getConnections();
		
		// get all the connected servers and put them in the combobox
		serverChoice = new JComboBox<String>();
		for (int i = 0; i < connections.size(); i++) {
			// make sure that the server is 100% connected
			if (connections.get(i).allGood()) {
				serverChoice.addItem(connections.get(i).getServerName());
			}
		}
		// set the currently selected servertab as standard
		int tabIndex = gui.getSelectedTab();
		if (connections.get(tabIndex).allGood()) {
			serverChoice.setSelectedIndex(gui.getSelectedTab());
		}
		
		info = new JLabel("If the room has a password, write 'ROOM PASSWORD'");
		info.setFont(new Font("Verdana", Font.PLAIN, 10));
		
		roomField = new JFormattedTextField();
		roomField.addKeyListener(keyListener);
		
		roomOkButton = new JButton("Join");
		// disabled until conditions are met, for instance that there is text in the roomField
		roomOkButton.setEnabled(false);
		roomOkButton.addMouseListener(mouseListener);
		
		roomNoButton = new JButton("Cancel");
		roomNoButton.addMouseListener(mouseListener);
		
		JPanel joinPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		joinPanel.add(new JLabel("Connection:"), c);
		
		c.gridx = 1;
		c.gridwidth = 3;
		joinPanel.add(serverChoice, c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(0, 0, 10, 0);
		joinPanel.add(new JLabel("Room:"), c);
		
		c.gridx = 1;
		c.gridwidth = 3;
		joinPanel.add(roomField, c);
		
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 4;
		joinPanel.add(info, c);
		
		c.gridx = 0;
		c.gridy = 3;
		c.weightx = 0.5;
		c.gridwidth = 2;
		joinPanel.add(roomOkButton, c);
		
		c.gridx = 2;
		joinPanel.add(roomNoButton, c);
		
		joinPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		
		setTitle("Join room");
		setContentPane(joinPanel);
		setResizable(false);
		pack();
		setLocationRelativeTo(getParent());
		// the line under makes sure the roomField gets focus so you can start writing instantly
		roomField.requestFocusInWindow();
		setVisible(true);
		
	}
	
	public static RoomPopup getInstance() {
		if (RoomPopupInstance == null) {
			RoomPopupInstance = new RoomPopup();
		}
		return RoomPopupInstance;
	}
	
	private void join() {
		String server = (String) serverChoice.getSelectedItem();
		String room = roomField.getText();
		if (!room.startsWith("#")) room = "#" + room;
		for (int i = 0; i < gui.getConnections().size(); i++) {
			if (server.equals(gui.getConnections().get(i).getServerName())) {
				destroy();
				gui.getConnections().get(i).addRoom(room);
			}
		}
	}
	
	private void destroy() {
		setVisible(false);
		dispose();
	}
	
	private class RoomFieldListener implements KeyListener {
		
		// enables the roomOkButton if all the conditions are met
		@Override
		public void keyReleased(KeyEvent e) {
			String line = roomField.getText();
			boolean enable = false;
			if (!line.contains(",") &&
				line.length() > 0 && 
				serverChoice.getSelectedItem() != null) {
				if (line.contains(" ")) {
					String password = line.substring(line.indexOf(" ") + 1);
					if (!password.contains(" "))
						enable = true;
				} else enable = true;
			}
			roomOkButton.setEnabled(enable);
		}
		
		@Override
		public void keyTyped(KeyEvent e) {
		}
		// enables to user to click the enter-button to join instead of having to click
		@Override
		public void keyPressed(KeyEvent e) {
			if (KeyEvent.VK_ENTER == e.getKeyCode() && roomOkButton.isEnabled()) {
				join();
			}
		}
		
	}
	
	private class RoomButtonListener implements MouseListener {
		
		// handles the button clicks
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getSource() == roomOkButton && roomOkButton.isEnabled()) {
				// send shit
				join();
			} else if (e.getSource() == roomNoButton) {
				// close shit
				destroy();
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {}
		@Override
		public void mouseReleased(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
		
	}

}
