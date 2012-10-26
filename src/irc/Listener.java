package irc;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

/*
 * listens to server messages
 * sorts messages to the right rooms
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
		
		String channel;
		rooms = gui.getRooms();
		Room serverTalk = rooms.get(0);
		serverTalk.addText(line);
		
		if (line.contains("PRIVMSG #")) {
			channel = line.substring(line.indexOf("#"), line.indexOf(" :"));
			for (int i = 0; i < rooms.size(); i++) {
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
			channel = line.substring(line.indexOf("#"));
			serverTalk.addText("You are now in " + channel);
			for (int i = 0; i < rooms.size(); i++) {
				System.out.println(channel + " - " + rooms.get(i).getName());
				if (channel.equals(rooms.get(i).getName())) {
					rooms.get(i).setJoined(true);
					gui.addRoom(rooms.get(i));
				}
			}
		}
		
		if (line.contains(connection.getNick() + " @ ") || line.contains(connection.getNick() + " = ")) {
			String names = line.substring(line.indexOf(" :") + 2);
			String[] listOfNames = names.split(" ");
			for (String name : listOfNames) {
				channel = line.substring(line.indexOf("#"), line.indexOf(" ", line.indexOf("#")));
				System.out.println(name + " in channel " + channel);
				for (int i = 0; i < rooms.size(); i++) {
					if (channel.equals(rooms.get(i).getName())) {
						System.out.println("!Adding a user!");
						rooms.get(i).addUser(name);
					}
				}
			}
		}
		
		
	}
	
}
