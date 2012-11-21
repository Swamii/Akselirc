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
		gui = connection.getGUI();
	}
	
	public GUI getGUI() {
		return gui;
	}
	
	public Connection getConnection() {
		return connection;
	}

	public void joinRoom(Room room) {
		try {
			if (room.getPwd() != null) { writer.write("JOIN " + room.getName() + " " + room.getPwd() + "\r\n"); }
			else { writer.write("JOIN " + room.getName() + "\r\n"); }
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
	
	public void sendMessage(String message, String room) {
		try {
			writer.write("PRIVMSG " + room + " :" + message + "\r\n");
			writer.flush();
		} catch (IOException e) {
			gui.errorPopup("Shit went wrong sending yo' message");
		}
	}
	
	public void leaveServer() {
		try {
			writer.write("QUIT :Bye bye\r\n");
			System.out.println("Trying to leave server!");
			writer.flush();
		} catch (IOException e) {
			gui.errorPopup("Shit went wrong trying to leave the server");
		}
		gui.removeConnection(connection);
	}
	
	
	public void sendPong(String line) {
		try {
			writer.write("PONG " + line.substring(5) + "\r\n");
			writer.flush();
		} catch (IOException e1) {
			gui.errorPopup("Wuut.. Couldn't send a pong to the server :'(");
		}
	}
	
}
