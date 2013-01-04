package irc.connection;

import irc.gui.GUI;
import irc.gui.Room;
import irc.gui.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * 
 * This class is responsible for listening to the server and making sure the right
 * stuff gets done.
 * @author Akseli Nelander
 */

public class Listener implements Runnable {

	private BufferedReader reader;
	private Talker talker;
	private Connection connection;
	private ArrayList<Room> rooms;
	private Server server;
	private volatile boolean running;
	private Thread thread;
	private Socket socket;
	
	public Listener(Connection connection, Socket socket) {
		this.connection = connection;
		this.socket = socket;
		server = connection.getServer();
		reader = connection.getReader();
		talker = connection.getTalker();
		rooms = server.getRooms();
		running = true;
	}
	
	public synchronized void start() {
		thread = new Thread(this, connection.getServerName() + " - Listener");
		thread.start();
	}
	
	public synchronized void stop() {
		running = false;
		System.out.println("Stop called! Running = " + running + " - " + connection.getServerName());
	}

	@Override
	public void run() {
		String line = null;
		// make sure the serverTalk tab is editable
		rooms.get(0).setEditable(true);
		
		try {
			while (running) {
				try {
					line = reader.readLine();
				} catch (SocketException e) {
					System.out.println("Connection closed " + connection.getServerName());
				}
				if (line == null) {
					break;
				} else {
					checkShitOutAndDoShitWithIt(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	// checking the line the server sends
	private void checkShitOutAndDoShitWithIt(String line) {
		
		String nick = connection.getNick();
		Room serverTalk = rooms.get(0);
		serverTalk.addText(line);
		System.out.println(line);
		
		
		// tried to join a room with a password, without supplying one.
		if (line.contains(nick) && line.contains("Cannot join channel (+k)")) {
			String room = line.substring(line.indexOf("#"), line.indexOf(" ", line.indexOf("#")));
			GUI.gui.pwdPopup(room, connection);
		}
		
		// ping pong so we don't disconnect
		if (line.startsWith("PING ")) {
			talker.sendPong(line);
		}
		
		// recieving private message from user
		if (line.contains(" PRIVMSG " + nick)) {
			String sender = line.substring(line.indexOf(":") + 1, line.indexOf("!"));
			String message = line.substring(line.indexOf(":", 5) + 1);
			// CTCP stuff
			if (line.contains("VERSION")) {
				talker.sendVersion(sender);
			} else if (line.contains("TIME")) {
				
			} else {
				server.addPrivChatMessage(sender, message);
			}
		}
		
		// sort a message to the right channel
		if (line.contains("PRIVMSG #")) {
			String channel = line.substring(line.indexOf("#"), line.indexOf(" :"));
			String user = line.substring(1, line.indexOf("!"));
			String message = line.substring(line.indexOf(":", 2) + 1);
			server.getRoom(channel).addText(user + ": " + message);
		}
		
		// if the client joins a channel, add the channel and stuff
		else if (line.contains(" JOIN ") && line.contains(nick)) {
			String channel = line.substring(line.indexOf("#"));
			serverTalk.addText("You are now in " + channel);
			server.addRoom(channel);
		}
		
		// checking and adding all users when entering a channel
		else if (line.contains(nick + " @ ") || line.contains(nick + " = ")) {
			String names = line.substring(line.indexOf(" :") + 2);
			String[] listOfNames = names.split(" ");
			String channel = line.substring(line.indexOf("#"), line.indexOf(" ", line.indexOf("#")));
			Room r = server.getRoom(channel);
			r.addAllUsers(listOfNames);
		}
		
		// if a user joins a channel
		else if (line.contains(" JOIN ")) {
			String channel = line.substring(line.indexOf("#"));
			String name = line.substring(line.indexOf(":") + 1, line.indexOf("!"));
			Room r = server.getRoom(channel);
			r.addUser(name);
		}
		
		// if a user leaves a channel
		else if (line.contains(" PART ")) {
			String channel = line.substring(line.indexOf("#"));
			String name = line.substring(line.indexOf(":") + 1, line.indexOf("!"));
			
			// check if that user is the client
			if (name.equals(connection.getNick())) {
				server.removeRoom(channel);
			}
			// check all other users leaving
			else {
				server.removeUser(name, channel);
			}
		}
		
		// if someone quits the server
		else if (line.contains(" QUIT ")) {
			String name = line.substring(line.indexOf(":") + 1, line.indexOf("!"));
			
			for (int i = 1; i < rooms.size(); i++) {
				if (rooms.get(i).getUsers().contains(name)) {
					rooms.get(i).removeUser(name);
				}
			}
		}
		
		// if someone invites you to a room
		else if (line.contains(" INVITE ") && line.contains(nick)) {
			String[] invLine = line.split(" ");
			String channel = invLine[3].substring(1);
			talker.joinRoom(channel);
		}
		
		// when you join the room and the room sends you their topic
		else if (line.contains(" 332 ")) {
			String room = line.substring(line.indexOf("#"), line.indexOf(" ", line.indexOf("#")));
			String topic = line.substring(line.indexOf(":", line.indexOf(room)) + 1);
			server.getRoom(room).addMOTD(topic);
		}
		
	}
}
