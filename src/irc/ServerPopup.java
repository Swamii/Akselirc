package irc;

import java.awt.GridLayout;
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
	private String[] details;
	private ServerFieldListener keyListener;
	private ServerButtonListener mouseListener;
	private JFormattedTextField nickField;
	private JFormattedTextField serverField;
	private JButton serverOkButton;
	private JButton serverNoButton;
	private JPanel connectPanel;
	private GUI gui;
	
	public ServerPopup(GUI gui) {
		this.gui = gui;
		details = new String[2];
		keyListener = new ServerFieldListener();
		mouseListener = new ServerButtonListener();
		initGUI();
	}
	
	private void initGUI() {
		nickField = new JFormattedTextField();
		nickField.setText(System.getProperty("user.name"));
		nickField.addAncestorListener(new RequestFocusListener());
		nickField.addKeyListener(keyListener);
		
		serverField = new JFormattedTextField();
		serverField.addKeyListener(keyListener);
		
		serverOkButton = new JButton("Connect");
		serverOkButton.setEnabled(false);
		serverOkButton.addMouseListener(mouseListener);
		
		serverNoButton = new JButton("Cancel");
		serverNoButton.addMouseListener(mouseListener);
		
		connectPanel = new JPanel(new GridLayout(0, 2));
		connectPanel.add(new JLabel(" Nick name:"));
		connectPanel.add(nickField);
		connectPanel.add(new JLabel(" Server:"));
		connectPanel.add(serverField);
		connectPanel.add(serverOkButton);
		connectPanel.add(serverNoButton);
		connectPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		
		setTitle("New connection");
		setContentPane(connectPanel);
		setResizable(false);
		pack();
		setLocationRelativeTo(getParent());
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
	
	private void connectScreen() {
		JLabel text = new JLabel("Connecting...");
		JPanel connectingPanel = new JPanel();
		
		JDialog connecting = new JDialog();
	}
	
	private class ServerFieldListener implements KeyListener {

		@Override
		public void keyReleased(KeyEvent e) {
			String nick = nickField.getText();
			String server = serverField.getText().toLowerCase();
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
