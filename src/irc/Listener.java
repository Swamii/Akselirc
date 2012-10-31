package irc;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;

/*
 * listens to server messages
 * sorts messages to the right rooms
 * handles all kinds of parsing of incoming text
 */

public class Listener implements Runnable {
	
	BufferedReader reader;
	GUI gui;
	Connection connection;
	Talker talker;
	ArrayList<Room> rooms;
	
	public Listener(Connection connection, GUI gui, Talker talker) {
		this.talker = talker;
		this.connection = connection;
		this.gui = gui;
		reader = connection.getReader();
	}

	@Override
	public void run() {
		String line = null;
		
		try {
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("PING ")) {
					talker.sendPong(line);
				} else {
					checkShitOutAndDoShitWithIt(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void checkShitOutAndDoShitWithIt(String line) {
		
		rooms = gui.getRooms();
		Room serverTalk = rooms.get(0);
		serverTalk.addText(line);
		
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
		
		if (line.contains("JOIN :#") && line.contains(connection.getNick())) {
			String channel = line.substring(line.indexOf("#"));
			serverTalk.addText("You are now in " + channel);
			for (int i = 1; i < rooms.size(); i++) {
				System.out.println(channel + " - " + rooms.get(i).getName());
				if (channel.equals(rooms.get(i).getName())) {
					rooms.get(i).setJoined(true);
					gui.addRoom(rooms.get(i));
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
						System.out.println("!Adding a user!");
						rooms.get(i).addUser(name);
					}
				}
			}
		}
		
		// if a user joins a channel
		if (line.contains(" JOIN :")) {
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
				if (channel.equals(rooms.get(i).getName()) && name.equals(talker.getNick())) {
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
			for (int i = 1; i < rooms.size(); i++) {
				DefaultListModel<String> users = rooms.get(i).getUsers();
				System.out.println(name + " in " + users);
				if (users.contains(name)) {
					rooms.get(i).removeUser(name);
				}
			}
		}
		
		
	}
	
}
