package irc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * This class starts a connection to a server. It creates the classes necessary to listen and talk
 * to the server. It is only responsible for the connection phase, then it delegates the listening
 * and talking to the respective classes.
 */

public class Connection implements Runnable {
	
	private String server;
	private String nick;
	private Socket socket;
	private BufferedWriter writer;
	private BufferedReader reader;
	private GUI gui;
	private ArrayList<Room> rooms = new ArrayList<Room>();
	private Server s;
	private Talker talker;
	private Listener listener;
	private Thread thread;
	private volatile Boolean allGood = false;
	private String startupRooms;

	public Connection(String nick, String server, String rooms) {
		this.nick = nick;
		this.server = server;
		startupRooms = rooms;
		gui = GUI.gui;
	}
	
	public void run() {
		// add this connection to the array, so the gui can keep track of it
		gui.addConnection(this);
		try {
			socket = new Socket(server, 6667);
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			gui.errorPopup("Something went wrong connecting to " + server);
			gui.removeConnection(this);
			return;
		}
		
		// tell the server what nick and name we want.
		try {
			writer.write("NICK " + nick + "\r\n");
			writer.write("USER " + nick + " 8 * : " + nick + "\r\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// create talker and listener
		talker = new Talker(this);
		listener = new Listener(this);
		// create the server talk room which displays server messages.
		Room r = new Room(this);
		rooms.add(r);
		s = new Server(this);
		// add the server talk "room" to the server (the inner jtabbedpane)
		s.addServerTalk(r);
		// add the server to the gui
		gui.addServer(s);
		
		String line;
		try {
			// read lines from server and respond accordingly
			while ((line = reader.readLine()) != null) {
				r.addText(line);
				if (line.startsWith("PING ")) {
					// some servers like to ping even when connecting :)
					try {
						writer.write("PONG " + line.substring(5) + "\r\n");
						writer.flush();
					} catch (IOException e1) {
						gui.errorPopup("Wuut.. Couldn't send a pong to the server :'(");
					}
				}
				if (line.indexOf("004") >= 0) {
					System.out.println("Connected to " + server);
					// all good. let the listener take over.
					startListener();
					break;
				} else if (line.indexOf("433") >= 0) {
					gui.errorPopup("Nickname is already in use");
					gui.removeConnection(this);
					break;
				}
			}
		} catch (IOException e) {
			gui.errorPopup("Something happened");
		}
		
	}
	
	private void startListener() {
		
		thread = new Thread(listener);
		thread.start();
		gui.enableJoinRoomMenuItem(true);
		checkPreConfRooms();
		synchronized(this) {
			allGood = true;
			this.notifyAll();
		}
	}
	
	private void checkPreConfRooms() {
		if (!startupRooms.equals("")) {
			if (startupRooms.contains(",")) {
				for (String s : startupRooms.split(",")) {
					addRoom(s);
					talker.joinRoom(s);
				}
			} else {
				addRoom(startupRooms);
				talker.joinRoom(startupRooms);
			}
		}
	}
	
	public void addRoom(String room) {
		// if room has password
		if (room.contains(" ")) {
			String[] roomInfo = room.split(" ");
			Room r = new Room(roomInfo[0], roomInfo[1], this);
			rooms.add(r);
		} else {
			Room r = new Room(room, this);
			rooms.add(r);
		}
	}
	
	public void closeCrap() {
		try {
			listener.stop();
			socket.close();
		} catch (IOException e) {
			gui.errorPopup("Something went wrong closing the crap");
			System.out.println("Shit went wrong closing " + server);
		}
	}
	
	public boolean allGood() {
		return allGood;
	}
	
	// lots of gets since this is the hub for each connection
	public ArrayList<Room> getRooms() {
		return rooms;
	}
	
	public String getNick() {
		return nick;
	}
	
	public Server getServer() {
		return s;
	}
	
	public String getServerName() {
		return server;
	}
	
	public GUI getGUI() {
		return gui;
	}
	
	public Listener getListener() {
		return listener;
	}
	
	public Talker getTalker() {
		return talker;
	}
	
	public BufferedWriter getWriter() {
		return writer;
	}
	
	public BufferedReader getReader() {
		return reader;
	}
}
