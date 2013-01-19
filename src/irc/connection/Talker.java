package irc.connection;

import irc.gui.GUI;
import irc.gui.Server;

import java.io.BufferedWriter;
import java.io.IOException;

public class Talker {
	
	private Connection connection;
	private BufferedWriter writer;
	private Server server;
	private GUI gui;

	public Talker(Connection connection) {
		this.connection = connection;
		server = connection.getServer();
		writer = connection.getWriter();
		gui = GUI.gui;
	}

	// check the command the user has typed.
	private void parseCommand(String message, String senderRoom) {

		if (message.toUpperCase().equals("PART")) leaveRoom(senderRoom);

		if (!message.contains(" ")) return;

		String[] messageSplit = message.split(" ");
		String command = messageSplit[0].toUpperCase();
		String item = messageSplit[1];

		if (command.equals("JOIN")) {
			String room = message.substring(message.indexOf(" ") + 1);
			if (!room.startsWith("#")) room = "#" + room;
			joinRoom(room);
		}

		else if (command.equals("PART")) {
			String room = item;
			if (room.contains(",")) {
				String[] rooms = room.split(",");
				for (String r : rooms) {
					if (!r.startsWith("#")) r = "#" + r;
					leaveRoom(r);
				}	
			} else {
				if (!room.startsWith("#")) room = "#" + room;
				leaveRoom(room);
			}
		}
		
		else if (command.equals("NICK")) {
			changeNick(item);
		}

	}

	public void joinRoom(String room) {
		try {
			writer.write("JOIN " + room + "\r\n");
			writer.flush();
		} catch (IOException e) {
			gui.errorPopup("Shit went wrong trying to join a room! :)\nClosing connection...");
			gui.removeConnection(connection);
		}
	}
	
	public void leaveRoom(String name) {
		try {
			writer.write("PART " + name + "\r\n");
			writer.flush();
		} catch (IOException e) {
			gui.errorPopup("Shit went wrong trying to leave the room.\nClosing connection...");
			gui.removeConnection(connection);
		}
		connection.getServer().removeRoom(name);
	}
	
	// if there's a slash the message should be handled as a command
	public void handleMessage(String message, String room) {
		System.out.println(message + "-" + room);
		if (message.startsWith("/")) {
			parseCommand(message.substring(1), room);
		} else {
			sendMessage(message, room);
		}
	}
	
	// send a standard message to a room
	private void sendMessage(String message, String room) {
		try {
			writer.write("PRIVMSG " + room + " :" + message + "\r\n");
			writer.flush();
		} catch (IOException e) {
			gui.errorPopup("Shit went wrong sending yo' message.\nClosing connection...");
			gui.removeConnection(connection);
		}
	}
	
	private void changeNick(String newNick) {
		try {
			writer.write("NICK " + newNick);
			writer.flush();
		} catch (IOException e) {
			gui.errorPopup("Error sending nick-change-message. Snap.\nClosing connection...");
			gui.removeConnection(connection);
		}
	}
	
	// sends a pong in response to a recieved ping
	public void sendPong(String line) {
		try {
			writer.write("PONG " + line.substring(5) + "\r\n");
			writer.flush();
		} catch (IOException e1) {
			gui.errorPopup("Wuut.. Couldn't send a pong to the server :'(.\nClosing connection...");
			gui.removeConnection(connection);
		}
	}
	
	public void leaveServer() {
		try {
			writer.write("QUIT :Bye bye\r\n");
			System.out.println("Telling server im leaving");
			writer.flush();
		} catch (IOException e) {
			gui.errorPopup("Shit went wrong trying to leave the server.\nClosing connection...");
			gui.removeConnection(connection);
		}
	}
	
	public void sendVersion(String nick) {
		try {
			//Lazors CTCP REPLY VERSION irssi v0.8.15 - running on Darwin x86_64
			//\001VERSION #:#:#\001
			writer.write("NOTICE " + nick + ": \001VERSION Akselirc : v0.1 : Duno\001\n\r");
			System.out.println("Sending version");
			writer.flush();
		} catch (IOException e) {
			System.err.println("VERSION went wrong.");
			gui.removeConnection(connection);
		}
	}
	
	public GUI getGUI() {
		return gui;
	}
	
	public Connection getConnection() {
		return connection;
	}
	
}
