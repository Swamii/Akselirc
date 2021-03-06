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

import javax.swing.SwingUtilities;

/**
 * This class starts a connection to a server. It creates the classes necessary to listen and talk
 * to the server. It is only responsible for the connection phase, then it delegates the listening
 * and talking to the respective classes.
 */

public class Connection implements Runnable {
	
	private String serverName;
	private String nick;
	private Socket socket;
	private BufferedWriter writer;
	private BufferedReader reader;
	private GUI gui;
	private Server server;
	private Talker talker;
	private Listener listener;
	private boolean allGood = false;
	private String startupRooms;
	private Connection connection;
	private Room serverTalk;
    private final int port = 6667;
    private Thread thread;

	public Connection(String serverName, String nick, String rooms) {
		this.nick = nick;
		this.serverName = serverName;
		startupRooms = rooms;
		gui = GUI.gui;
	}
	
	public void start() {
		thread = new Thread(this);
		thread.start();
	}
	
	public void exit() {
		thread.interrupt();
		gui.removeConnection(this);
	}
	
	public void run() {
		// add this connection to the array, so the gui can keep track of it
		socket = new Socket();
		SocketAddress socketAddress = new InetSocketAddress(serverName, port);
		
		connection = this;
		
		// this is a 5-sec timer. if a connections hasn't been made in that time, skip it.
		new java.util.Timer().schedule(
		        new java.util.TimerTask() {
		            public void run() {
		                if (!socket.isConnected()) {
		        			exit();
		        			gui.errorPopup("Connection timed out to: " + serverName);
		                }
		            }
		        }, 5000);
		
		try {
			socket.connect(socketAddress);
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			exit();
			gui.errorPopup("Connection to " + serverName + " failed to initialize ");
		}
		
		// tell the server what nick and name we want.
		try {
			writer.write("NICK " + nick + "\r\n");
			writer.write("USER " + nick + " 8 * : " + nick + "\r\n");
			writer.flush();
		} catch (IOException e) {
			exit();
			gui.errorPopup("Error writing to server\nClosing connection...");
		}
		
		init();
		
		serverTalk = server.getRooms().get(0);
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				serverTalk.addText(line);
				if (line.startsWith("PING ")) {
					// some servers like to ping even when connecting :)
					try {
						writer.write("PONG " + line.substring(5) + "\r\n");
						writer.flush();
					} catch (IOException e1) {
						exit();
						gui.errorPopup("Wuut.. Couldn't send a pong to the server :'(");
					}
				}
				if (line.indexOf("004") >= 0) {
					System.out.println("Connected to " + serverName);
					// all good. let the listener take over.
					startListener();
					break;
				} else if (line.indexOf("433") >= 0) {
					exit();
					gui.errorPopup("Nickname is already in use on: " + serverName);
					break;
				}
			}
		} catch (IOException e) {
			exit();
			gui.errorPopup("Something happened and its not good.");
		}
		
	}

	private synchronized void init() {
		
		// create the server part of the gui
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					server = new Server(connection);
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// create talker and listener
		talker = new Talker(this);
		listener = new Listener(this, socket);

		gui.addServer(server);

	}
	
	// start listener, join any rooms added to preferences.
	private void startListener() {
		
		gui.enableJoinRoomMenuItem(true);
		allGood = true;
		
		listener.start();

		checkPreConfRooms();
	}
	
	private void checkPreConfRooms() {
		if (!startupRooms.equals("")) {
			if (startupRooms.contains(",")) {
				for (String s : startupRooms.split(",")) {
					talker.joinRoom(s);
				}
			} else {
				talker.joinRoom(startupRooms);
			}
		}
	}
	
	public boolean allGood() {
		return allGood;
	}
	
	public String getNick() {
		return nick;
	}
	
	public Server getServer() {
		return server;
	}
	
	public String getServerName() {
		return serverName;
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
