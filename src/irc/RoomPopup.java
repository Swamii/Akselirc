package irc;

import javax.swing.JDialog;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

public class RoomPopup extends JDialog {

	private static final long serialVersionUID = 1L;
	private String[] details;
	private RoomFieldListener keyListener;
	private RoomButtonListener mouseListener;
	private JComboBox<String> serverChoice;
	private JFormattedTextField roomField;
	private JButton roomOkButton;
	private JButton roomNoButton;
	private JLabel info;
	private GUI gui;
	
	public RoomPopup(GUI gui) {
		this.gui = gui;
		details = new String[2];
		keyListener = new RoomFieldListener();
		mouseListener = new RoomButtonListener();
		initGUI();
	}
	
	private void initGUI() {
		ArrayList<Connection> connections = gui.getConnections();
		String[] choices = new String[connections.size()];
		for (int i = 0; i < connections.size(); i++) {
			choices[i] = connections.get(i).getServerName();
		}
		
		serverChoice = new JComboBox<String>(choices);
		serverChoice.setSelectedIndex(gui.getSelectedTab());
		
		info = new JLabel("If the room has a password, write 'ROOM PASSWORD'");
		info.setFont(new Font("Verdana", Font.PLAIN, 10));
		
		roomField = new JFormattedTextField();
		roomField.addAncestorListener(new RequestFocusListener());
		roomField.addKeyListener(keyListener);
		
		roomOkButton = new JButton("Join");
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
		joinPanel.add(new JLabel("Connection: "), c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 3;
		joinPanel.add(serverChoice, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(0, 0, 10, 0);
		joinPanel.add(new JLabel("Room: "), c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 3;
		joinPanel.add(roomField, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 4;
		joinPanel.add(info, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 3;
		c.weightx = 0.5;
		c.gridwidth = 2;
		joinPanel.add(roomOkButton, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridy = 3;
		c.weightx = 0.5;
		c.gridwidth = 2;
		joinPanel.add(roomNoButton, c);
		
		joinPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		
		setTitle("Join room");
		setContentPane(joinPanel);
		setResizable(false);
		pack();
		setLocationRelativeTo(getParent());
		setVisible(true);
		
	}
	
	private void join() {
		details[0] = (String) serverChoice.getSelectedItem();
		details[1] = roomField.getText();
		destroy();
		gui.newRoom(details);
	}
	
	private void destroy() {
		setVisible(false);
		dispose();
	}
	
	private class RoomFieldListener implements KeyListener {

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
		@Override
		public void keyPressed(KeyEvent e) {
			if (KeyEvent.VK_ENTER == e.getKeyCode() && roomOkButton.isEnabled()) {
				join();
			}
		}
		
	}
	
	private class RoomButtonListener implements MouseListener {

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
