package irc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/*
 * creates the room as a tab
 */

public class Room {
	
	JPanel mainPanel;
	JPanel chatPanel;
	JTextField userText;
	JTextArea chatWindow;
	JList<String> userWindow;
	DefaultListModel<String> users;
	JScrollPane outerUserWindow;
	String name;
	String pwd;
	Talker talker;
	boolean hasJoined = false;
	
	public Room(String room) {
		// room for server talk
		
		name = room;
		
		chatWindow = new JTextArea();
		chatWindow.setEditable(false);
		
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setSize(800, 600);
		
	}

	public Room(String room, final Talker talker) {
		// standard room
		
		this.talker = talker;
		name = room;
		
		initGUI();
		
	}
	
	public Room(String room, String pwd, final Talker talker) {
		// standard room with password
		
		this.talker = talker;
		name = room;
		this.pwd = pwd;
		
		initGUI();
		
	}
	
	public void addUser(String user) {
		users.addElement(user);
		userWindow.setModel(users);
		outerUserWindow.revalidate();
		outerUserWindow.repaint();
		System.out.println("Adding element " + user + " to a list of: " + users);
	}
	
	public void removeUser(String user) {
		users.removeElement(user);
		userWindow.setModel(users);
		outerUserWindow.revalidate();
		outerUserWindow.repaint();
	}
	
	public boolean hasJoined() {
		return hasJoined;
	}
	
	public JPanel getPanel() {
		return mainPanel;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPwd() {
		return pwd;
	}
	
	public DefaultListModel<String> getUsers() {
		return users;
	}
	
	public void addText(String text) {
		chatWindow.append(text + "\n");
	}
	
	public void setEditable(boolean b) {
		userText.setEditable(b);
	}

	public void setJoined(boolean b) {
		hasJoined = b;
	}
	
	private void initGUI() {
		
		chatPanel = new JPanel(new BorderLayout());
		
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					talker.sendMessage(event.getActionCommand(), getName());
					addText(talker.getNick() + ": " + event.getActionCommand());
				} catch (IOException e) {
					e.printStackTrace();
				}
				userText.setText("");
			}
		});
		
		chatWindow = new JTextArea();
		chatWindow.setEditable(false);
		
		chatPanel.add(userText, BorderLayout.SOUTH);
		chatPanel.add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		chatPanel.setPreferredSize(new Dimension(660, 500));
		
		mainPanel = new JPanel(new BorderLayout());
		
		users = new DefaultListModel<String>();
		userWindow = new JList<String>(users);
		
		outerUserWindow = new JScrollPane(userWindow);
		outerUserWindow.setPreferredSize(new Dimension(175, 500));
		
		mainPanel.add(outerUserWindow, BorderLayout.EAST);
		mainPanel.add(chatPanel, BorderLayout.CENTER);
		
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setSize(800, 600);
		
	}
}
