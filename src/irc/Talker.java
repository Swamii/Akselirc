package irc;

import java.io.BufferedWriter;
import java.io.IOException;

public class Talker {
	
	private Connection connection;
	private BufferedWriter writer;
	private GUI gui;

	public Talker(Connection connection) {
		this.connection = connection;
		writer = connection.getWriter();
		gui = GUI.gui;
	}
	
	public GUI getGUI() {
		return gui;
	}
	
	public Connection getConnection() {
		return connection;
	}

	public void joinRoom(String room) {
		try {
			writer.write("JOIN " + room + "\r\n");
			writer.flush();
		} catch (IOException e) {
			gui.errorPopup("Shit went wrong trying to join a room! :)");
		}
	}
	
	public void leaveRoom(String name) {
		try {
			System.out.println("PART " + name + "\'r\'n");
			writer.write("PART " + name + "\r\n");
			writer.flush();
		} catch (IOException e) {
			gui.errorPopup("Shit went wrong trying to leave the room");
		}
	}
	
	// if there's a slash the message should be handled as a command
	public void handleMessage(String message, String room) {
		System.out.println(message + "-" + room);
		if (message.startsWith("/")) {
			parseCommand(message.substring(1));
		} else {
			sendMessage(message, room);
		}
	}
	
	// check the command the user has typed. i shouldn't have to check this since the irc protocol is so simple.
	private void parseCommand(String message) {
		if (!message.contains(" ")) {
			return;
		}
		
		String[] messageSplit = message.split(" ");
		int messageLen = messageSplit.length;
		
		if (messageSplit[0].toUpperCase().equals("JOIN") && messageLen < 4 && messageLen > 1 ) {
			String room = message.substring(message.indexOf(" ") + 1);
			if (!room.startsWith("#")) room = "#" + room;
			connection.addRoom(room);
		}
		
		if (messageSplit[0].toUpperCase().equals("PART") && messageLen < 3 && messageLen > 1) {
			String room = message.substring(message.indexOf(" ") + 1);
			if (room.contains(",")) {
				String[] rooms = room.split(",");
				for (String r : rooms) {
					if (!r.startsWith("#")) {
						r = "#" + r;
					}
					leaveRoom(r);
					connection.getServer().removeRoom(r);
				}
				
			} else {
				if (!room.startsWith("#")) {
					room = "#" + room;
				}
				leaveRoom(room);
				connection.getServer().removeRoom(room);
			}
		}
		
		
	}
	
	// send a standard message to a room
	private void sendMessage(String message, String room) {
		try {
			writer.write("PRIVMSG " + room + " :" + message + "\r\n");
			writer.flush();
		} catch (IOException e) {
			gui.errorPopup("Shit went wrong sending yo' message");
		}
	}
	
	// sends a pong in response to a recieved ping
	public void sendPong(String line) {
		try {
			writer.write("PONG " + line.substring(5) + "\r\n");
			writer.flush();
		} catch (IOException e1) {
			gui.errorPopup("Wuut.. Couldn't send a pong to the server :'(");
		}
	}
	
	public void leaveServer() {
		try {
			writer.write("QUIT :Bye bye\r\n");
			System.out.println("Telling server im leaving");
			writer.flush();
		} catch (IOException e) {
			gui.errorPopup("Shit went wrong trying to leave the server");
		}
	}
	
}
