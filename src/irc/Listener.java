package irc;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
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
		Room serverTalk = rooms.get(0);
		serverTalk.addText(line);
		server = connection.getServer();
		System.out.println(line);
		
		if (line.contains(connection.getNick()) && line.contains("Cannot join channel (+k)")) {
			String room = line.substring(line.indexOf("#"), line.indexOf(" ", line.indexOf("#")));
			System.out.println(room + " TO REMOVE");
			Room remRoom = null;
			for (int i = 1; i < rooms.size(); i++) {
				if (rooms.get(i).getName().equals(room)) {
					remRoom = rooms.get(i);
					System.out.println("Got it!");
				}
			}
			System.out.println(rooms.remove(remRoom));
		}
		
		// ping pong so we don't disconnect
		if (line.startsWith("PING ")) {
			talker.sendPong(line);
		}
		
		// recieving private message from user
		if (line.contains(" PRIVMSG " + connection.getNick())) {
			String sender = line.substring(line.indexOf(":") + 1, line.indexOf("!"));
			server.addPrivChatMessage(sender, line.substring(line.indexOf(":", 5) + 1));
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
		if ((line.contains(" JOIN :#") || line.contains(" JOIN #")) && line.contains(connection.getNick())) {
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
		if (line.contains(connection.getNick() + " @ ") || line.contains(connection.getNick() + " = ")) {
			String names = line.substring(line.indexOf(" :") + 2);
			String[] listOfNames = names.split(" ");
			for (String name : listOfNames) {
				String channel = line.substring(line.indexOf("#"), line.indexOf(" ", line.indexOf("#")));
				System.out.println(name + " in channel " + channel);
				for (int i = 1; i < rooms.size(); i++) {
					if (channel.equals(rooms.get(i).getName())) {
						rooms.get(i).addUser(name);
					}
				}
			}
		}
		
		// if a user joins a channel
		if (line.contains(" JOIN :") || line.contains(" JOIN ")) {
			String channel = line.substring(line.indexOf("#"));
			String name = line.substring(line.indexOf(":") + 1, line.indexOf("!"));
			if (!name.equals(connection.getNick())) {
				System.out.println(channel + "-!JOIN!-" + name);
				for (int i = 1; i < rooms.size(); i++) {
					if (channel.equals(rooms.get(i).getName())) {
						rooms.get(i).addUser(name);
						rooms.get(i).addMessage(name + " has joined channel " + channel);
						System.out.println("Adding " + name);
					}
				}
			}
		}
		
		// if a user leaves a channel
		if (line.contains(" PART ")) {
			String channel = line.substring(line.indexOf("#"));
			String name = line.substring(line.indexOf(":") + 1, line.indexOf("!"));
			System.out.println(channel + "-!PART!-" + name);
			for (int i = 1; i < rooms.size(); i++) {
				// check if that user is the client and that we are in the right room in the loop
				if (channel.equals(rooms.get(i).getName()) && name.equals(connection.getNick())) {
					System.out.println(rooms.get(i).getName() + " removed from list of rooms!");
					rooms.remove(i);
				}
				// check all other users leaving
				else if (channel.equals(rooms.get(i).getName())) {
					rooms.get(i).removeUser(name);
					rooms.get(i).addMessage(name + " has left " + channel);
					System.out.println("Removing " + name);
				}
			}
		}
		
		if (line.contains(" QUIT ")) {
			String name = line.substring(line.indexOf(":") + 1, line.indexOf("!"));
			if (name.equals(connection.getNick())) {
				// if the client leaving is you!
				System.out.println("Getting info about leaving " + connection.getServerName());
			}
			for (int i = 1; i < rooms.size(); i++) {
				DefaultListModel<String> users = rooms.get(i).getUsers();
				if (users.contains(name)) {
					rooms.get(i).removeUser(name);
				}
			}
		}		
	}
}
