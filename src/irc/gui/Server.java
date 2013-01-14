package irc.gui;

import irc.connection.Connection;
import irc.connection.Talker;

import java.awt.GridLayout;
import java.lang.reflect.InvocationTargetException;
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
	private ArrayList<PrivChat> privChats;
	private ArrayList<Room> rooms;

	public Server(Connection connection) {
		this.connection = connection;
		name = connection.getServerName();
		privChats = new ArrayList<PrivChat>();
		rooms = new ArrayList<Room>();
		
		initGUI();
	}	
	
	private void initGUI() {
		panel = new JPanel(new GridLayout(1, 1));
		jtp = new JTabbedPane();
		panel.add(jtp);
		addServerTalk();
	}
	
	public void addServerTalk() {
		Room r = new Room(connection);
		rooms.add(r);
		jtp.add(r.getName(), r.getPanel());
		r.setEditable(true);
	}

	public synchronized void addRoom(final String roomName) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					Room r = new Room(roomName, connection);
					rooms.add(r);
					jtp.add(r.getName(), r.getPanel());
					jtp.setTabComponentAt(jtp.getTabCount() - 1, new ButtonTabComponent(jtp, connection.getTalker()));
					r.setEditable(true);
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void addPrivChatMessage(final String user, final String message) {
		addPrivChat(user);
		for (PrivChat chat : privChats) {
			if (chat.getName().equals(user)) {
				chat.addText(user + ": " + message);
			}
		}
	}
	
	public void addPrivChat(final String user) {
		if (!privChatExists(user)) {
			final PrivChat chat = new PrivChat(user, connection);
			privChats.add(chat);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					jtp.add(chat.getName(), chat.getPanel());
					jtp.setTabComponentAt(jtp.getTabCount() - 1, new ButtonTabComponent(jtp, connection.getTalker()));
				}
			});
		}
	}
	
	public void removePrivChat(int index) {
		PrivChat rem = null;
		for (PrivChat chat : privChats) {
			if (chat.getName().equals(jtp.getTitleAt(index))) {
				rem = chat;
			}
		}
		if (rem != null) {
			privChats.remove(rem);
		}
		jtp.remove(index);
	}
	
	public synchronized void removeRoom(String roomName) {
		Room r = getRoom(roomName);
		rooms.remove(r);
				
		for (int i = 0; i < jtp.getTabCount(); i++) {
			if (roomName.equals(jtp.getTitleAt(i))) {
				jtp.remove(i);
			}
		}
	}
	
	public void removeUser(String user, String roomName, String reason) {
		Room r = getRoom(roomName);
		if (r != null) r.removeUser(user, reason);
	}
	
	public synchronized Room getRoom(String name) {
		Room room = null;
		for (Room r :  rooms) {
			if (r.getName().equals(name)) room = r;
		}
		return room;
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
	
	public ArrayList<Room> getRooms() {
		return rooms;
	}

	public ArrayList<PrivChat> getPrivChats() {
		return privChats;
	}

	public String getName() {
		return name;
	}
	
	public Talker getTalker() {
		return connection.getTalker();
	}

	public JPanel getPanel() {
		return panel;
	}
}
