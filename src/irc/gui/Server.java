package irc.gui;

import irc.connection.Connection;
import irc.connection.Talker;

import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class Server {
	
	private Connection connection;
	private String name;
	private JPanel panel;
	private JTabbedPane jtp;
	private Talker talker;
	private ArrayList<Room> rooms;
	private ArrayList<PrivChat> privChats;

	public Server(Connection connection) {
		this.connection = connection;
		name = connection.getServerName();
		talker = connection.getTalker();
		privChats = new ArrayList<PrivChat>();
		
		initGUI();
	}	
	
	private void initGUI() {
		panel = new JPanel(new GridLayout(1, 1));
		jtp = new JTabbedPane();
		panel.add(jtp);
	}
	
	public void addServerTalk(Room room) {
		jtp.add(room.getName(), room.getPanel());
	}
	
	public void addRoom(String roomName) {
		rooms = connection.getRooms();
		for (final Room r : rooms) {
			if (roomName.equals(r.getName())) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						jtp.add(r.getName(), r.getPanel());
						jtp.setTabComponentAt(jtp.getTabCount() - 1, new ButtonTabComponent(jtp, connection.getTalker()));
						r.setEditable(true);
					}
				});
			}
		}	
	}
	
	public void addPrivChatMessage(String user, String message) {
		addPrivChat(user);
		for (PrivChat chat : privChats) {
			if (chat.getName().equals(user)) {
				chat.addText(user + ": " + message);
			}
		}
	}
	
	public void addPrivChat(String user) {
		if (!privChatExists(user)) {
			PrivChat chat = new PrivChat(user, connection);
			privChats.add(chat);
			jtp.add(chat.getName(), chat.getPanel());
			jtp.setTabComponentAt(jtp.getTabCount() - 1, new ButtonTabComponent(jtp, connection.getTalker()));
		}
	}
	
	public void removePrivChat(String user) {
		PrivChat rem = null;
		for (PrivChat chat : privChats) {
			if (chat.getName().equals(user)) {
				rem = chat;
			}
		}
		if (rem != null) {
			privChats.remove(rem);
		}
	}
	
	public ArrayList<PrivChat> getPrivChats() {
		return privChats;
	}
	
	// checks if we have a private chat-tab open with the user
	private boolean privChatExists(String user) {
		boolean exists = false;
		for (int i = 0; i < jtp.getTabCount(); i++) {
			if (user.equals(jtp.getTitleAt(i))) {
				exists = true;
			}
		}
		return exists;
	}
	
	public void removeRoom(String roomName) {
		for (int i = 0; i < jtp.getTabCount(); i++) {
			if (roomName.equals(jtp.getTitleAt(i))) {
				jtp.remove(i);
			}
		}
	}

	public String getName() {
		return name;
	}
	
	public Talker getTalker() {
		return talker;
	}

	public JPanel getPanel() {
		return panel;
	}
}
