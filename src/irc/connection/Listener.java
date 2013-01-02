package irc.connection;

import irc.gui.GUI;
import irc.gui.Room;
import irc.gui.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.DefaultListModel;

/**
 * 
 * @author Akseli Nelander
 * This class is responsible for listening to the server and making sure the right
 * stuff gets done.
 *
 */

public class Listener implements Runnable {

	private BufferedReader reader;
	private Talker talker;
	private Connection connection;
	private ArrayList<Room> rooms;
	private Server server;
	private boolean running;
	
	public Listener(Connection connection) {
		this.connection = connection;
		reader = connection.getReader();
		talker = connection.getTalker();
		rooms = connection.getRooms();
		running = true;
	}

	@Override
	public void run() {
		String line = null;
		// make sure
		connection.getRooms().get(0).setEditable(true);
		
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
		}
	}
	
	public void stop() {
		running = false;
	}
	
	// checking the line the server sends
	private void checkShitOutAndDoShitWithIt(String line) {
		
		rooms = connection.getRooms();
		String nick = connection.getNick();
		Room serverTalk = rooms.get(0);
		serverTalk.addText(line);
		server = connection.getServer();
		System.out.println(line);
		
		
		// tried to join a room with a password, without supplying one.
		if (line.contains(nick) && line.contains("Cannot join channel (+k)")) {
			String room = line.substring(line.indexOf("#"), line.indexOf(" ", line.indexOf("#")));
			Room remRoom = null;
			for (int i = 1; i < rooms.size(); i++) {
				if (rooms.get(i).getName().equals(room)) {
					remRoom = rooms.get(i);
				}
			}
			if (remRoom != null) {
				connection.removeRoom(remRoom);
				GUI.gui.pwdPopup(room, connection);
			}
		}
		
		// ping pong so we don't disconnect
		if (line.startsWith("PING ")) {
			talker.sendPong(line);
		}
		
		// recieving private message from user
		if (line.contains(" PRIVMSG " + nick)) {
			String sender = line.substring(line.indexOf(":") + 1, line.indexOf("!"));
			String message = line.substring(line.indexOf(":", 5) + 1);
			if (line.contains("VERSION")) {
				talker.sendVersion(sender);
			} else {
				server.addPrivChatMessage(sender, message);
			}
		}
		
		// sort a message to the right channel
		if (line.contains("PRIVMSG #")) {
			String channel = line.substring(line.indexOf("#"), line.indexOf(" :"));
			for (int i = 1; i < rooms.size(); i++) {
				if (channel.equals(rooms.get(i).getName())) {
					if (line.contains("!") && line.contains(" :")) {
						String user = line.substring(1, line.indexOf("!"));
						String text = line.substring(line.indexOf(":", 2) + 1);
						rooms.get(i).addText(user + ": " + text);
					}
				}
			}
		}
		
		// if the client joins a channel, add the channel and stuff
		else if (line.contains(" JOIN ") && line.contains(nick)) {
			String channel = line.substring(line.indexOf("#"));
			serverTalk.addText("You are now in " + channel);
			for (int i = 1; i < rooms.size(); i++) {
				System.out.println(channel + " - " + rooms.get(i).getName());
				if (channel.equals(rooms.get(i).getName())) {
					server.addRoom(rooms.get(i).getName());
				}
			}
		}
		
		// checking and adding all users when entering a channel
		else if (line.contains(nick + " @ ") || line.contains(nick + " = ")) {
			String names = line.substring(line.indexOf(" :") + 2);
			String[] listOfNames = names.split(" ");
			String channel = line.substring(line.indexOf("#"), line.indexOf(" ", line.indexOf("#")));
			for (int i = 1; i < rooms.size(); i++) {
				if (channel.equals(rooms.get(i).getName())) {
					rooms.get(i).addAllUsers(listOfNames);
				}
			}
		}
		
		// if a user joins a channel
		if (line.contains(" JOIN ")) {
			String channel = line.substring(line.indexOf("#"));
			String name = line.substring(line.indexOf(":") + 1, line.indexOf("!"));
			if (!name.equals(connection.getNick())) {
				System.out.println(channel + "-!JOIN!-" + name);
				for (int i = 1; i < rooms.size(); i++) {
					if (channel.equals(rooms.get(i).getName())) {
						rooms.get(i).addUser(name);
					}
				}
			}
		}
		
		// if a user leaves a channel
		else if (line.contains(" PART ")) {
			String channel = line.substring(line.indexOf("#"));
			String name = line.substring(line.indexOf(":") + 1, line.indexOf("!"));
			for (int i = 1; i < rooms.size(); i++) {
				// check if that user is the client and that we are in the right room in the loop
				Room r = null;
				if (channel.equals(rooms.get(i).getName()) && name.equals(connection.getNick())) {
					r = rooms.get(i);
					connection.removeRoom(r);
					break;
				}
				// check all other users leaving
				else if (channel.equals(rooms.get(i).getName())) {
					rooms.get(i).removeUser(name);
					break;
				}
			}
		}
		
		else if (line.contains(" QUIT ")) {
			String name = line.substring(line.indexOf(":") + 1, line.indexOf("!"));
			
			for (int i = 1; i < rooms.size(); i++) {
				if (rooms.get(i).getUsers().contains(name)) {
					rooms.get(i).removeUser(name);
				}
			}
		}
		
		else if (line.contains(" INVITE ") && line.contains(nick)) {
			String[] invLine = line.split(" ");
			String channel = invLine[3].substring(1);
			talker.joinRoom(channel);
		}
		
		else if (line.contains(" 332 ")) {
			String room = line.substring(line.indexOf("#"), line.indexOf(" ", line.indexOf("#")));
			String topic = line.substring(line.indexOf(":", line.indexOf(room)) + 1);
			System.out.println(topic);
			for (int i = 1; i < rooms.size(); i++) {
				if (rooms.get(i).getName().equals(room))
					rooms.get(i).addMOTD(topic);
			}
			
		}
		
	}
}
