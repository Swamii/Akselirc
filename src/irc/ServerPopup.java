package irc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ServerPopup extends JDialog {
	
	private static final long serialVersionUID = 1L;
	private static ServerPopup ServerPopupInstance = null;
	private String[] details;
	private ServerFieldListener keyListener;
	private ServerButtonListener mouseListener;
	private JFormattedTextField nickField;
	private JFormattedTextField serverField;
	private JButton serverOkButton;
	private JButton serverNoButton;
	private JPanel connectPanel;
	private GUI gui;
	
	public ServerPopup() {
		gui = GUI.gui;
		details = new String[2];
		keyListener = new ServerFieldListener();
		mouseListener = new ServerButtonListener();
		initGUI();
	}
	
	public static ServerPopup getInstance() {
		if (ServerPopupInstance == null) {
			ServerPopupInstance = new ServerPopup();
		}
		return ServerPopupInstance;
	}
	
	private void initGUI() {
		nickField = new JFormattedTextField();
		// standard user name is the user name you have on your OS
		nickField.setText(System.getProperty("user.name"));
		nickField.addKeyListener(keyListener);
		nickField.setColumns(10);
		
		serverField = new JFormattedTextField();
		serverField.addKeyListener(keyListener);
		serverField.setColumns(10);
		
		serverOkButton = new JButton("Connect");
		serverOkButton.setEnabled(false);
		serverOkButton.addMouseListener(mouseListener);
		
		serverNoButton = new JButton("Cancel");
		serverNoButton.addMouseListener(mouseListener);
		
		connectPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		connectPanel.add(new JLabel("Nick:"), c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridwidth = 3;
		c.insets = new Insets(0, 5, 5, 5);
		connectPanel.add(nickField, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 1;
		c.gridx = 0;
		c.gridwidth = 1;
		c.insets = new Insets(0,0,0,0);
		connectPanel.add(new JLabel("Server:"), c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridwidth = 3;
		c.insets = new Insets(0, 5, 5, 5);
		connectPanel.add(serverField, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 2;
		c.gridx = 0;
		c.gridwidth = 2;
		c.weightx = 0.5;
		c.insets = new Insets(10, 0, 0, 0);
		connectPanel.add(serverOkButton, c);
		
		c.gridx = 2;
		connectPanel.add(serverNoButton, c);
		
		connectPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		
		setTitle("New connection");
		setContentPane(connectPanel);
		setResizable(false);
		pack();
		setLocationRelativeTo(getParent());
		nickField.requestFocusInWindow();
		setVisible(true);
	}
	
	private void connect() {
		details[0] = nickField.getText();
		details[1] = serverField.getText();
		destroy();
		gui.newServer(details);
	}
	
	private void destroy() {
		setVisible(false);
		dispose();
	}
	
	private class ServerFieldListener implements KeyListener {

		@Override
		public void keyReleased(KeyEvent e) {
			String nick = nickField.getText();
			String server = serverField.getText().toLowerCase();
			// check if its a semi-valid irc-server, if it is then enable the ok button
			if (nick.length() > 0 && server.startsWith("irc.") && !nick.contains(" ") && !server.contains(" ")) {
				serverOkButton.setEnabled(true);
			} else {
				serverOkButton.setEnabled(false);
			}
		}
		
		@Override
		public void keyTyped(KeyEvent e) {}
		@Override
		public void keyPressed(KeyEvent e) {
			// enabling the enter key to connect faster
			if (KeyEvent.VK_ENTER == e.getKeyCode() && serverOkButton.isEnabled()) {
				connect();
			}
		}
	}
	
	private class ServerButtonListener implements MouseListener {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getSource() == serverOkButton && serverOkButton.isEnabled()) {
				// send shit
				connect();
			} else if (e.getSource() == serverNoButton) {
				// close shit
				destroy();
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {}
		@Override
		public void mousePressed(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}	
	}
}
