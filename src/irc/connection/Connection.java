package irc.connection;

import irc.gui.GUI;
import irc.gui.Room;
import irc.gui.Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

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
	private boolean finished = false;
	private boolean allGood = false;
	private String startupRooms;
	private Connection connection;
	private Room serverTalk;

	public Connection(String nick, String server, String rooms) {
		this.nick = nick;
		this.server = server;
		startupRooms = rooms;
		gui = GUI.gui;
	}
	
	
	
	public void run() {
		// add this connection to the array, so the gui can keep track of it
		gui.addConnection(this);
		socket = new Socket();
		SocketAddress socketAddress = new InetSocketAddress(server, 6667);
		
		connection = this;
		
		// this is a 5-sec timer. if a connections hasn't been made in that time, skip it.
		new java.util.Timer().schedule(
		        new java.util.TimerTask() {
		            @Override
		            public void run() {
		                if (!socket.isConnected()) {
		                	System.out.println("not connected..");
		        			gui.removeConnection(connection);
		        			gui.errorPopup("Connection timed out to: " + server);
		        			return;
		                } else {
		                	System.out.println("connected..");
		                }
		            }
		        }, 5000);
		
		try {
			socket.connect(socketAddress);
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			return;
		}
		
		if (!socket.isConnected()) {
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
		
		init();
		
		String line;
		try {
			// read lines from server and respond accordingly
			while ((line = reader.readLine()) != null) {
				serverTalk.addText(line);
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
					gui.errorPopup("Nickname is already in use on: " + server);
					gui.removeConnection(this);
					break;
				}
			}
		} catch (IOException e) {
			gui.errorPopup("Something happened and its not good.");
			gui.removeConnection(this);
			return;
		}
		
	}

	private void init() {
		// create talker and listener
		talker = new Talker(this);
		listener = new Listener(this);
		
		// create the server talk room which displays server messages.
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					serverTalk = new Room(connection);
					s = new Server(connection);
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		rooms.add(serverTalk);

		// add the server talk "room" to the server (the inner jtabbedpane)
		s.addServerTalk(serverTalk);

		gui.addServer(s);

	}
	
	// start listener, join any rooms added to preferences.
	private void startListener() {
		
		thread = new Thread(listener);
		thread.start();
		gui.enableJoinRoomMenuItem(true);
		checkPreConfRooms();
		allGood = true;

	}
	
	private void checkPreConfRooms() {
		if (!startupRooms.equals("")) {
			if (startupRooms.contains(",")) {
				for (String s : startupRooms.split(",")) {
					addRoom(s);
				}
			} else {
				addRoom(startupRooms);
			}
		}
	}
	
	public void addRoom(final String room) {
		// if room has password
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Room r = new Room(room, connection);
				rooms.add(r);
				talker.joinRoom(room);
			}
		});
	}
	
	public void removeRoom(Room r) {
		rooms.remove(r);
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
	
	public boolean isFinished() {
		return finished;
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
