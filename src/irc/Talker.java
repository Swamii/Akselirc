package irc;

import java.io.BufferedWriter;
import java.io.IOException;

public class Talker {
	
	BufferedWriter writer;
	GUI gui;
	Connection connection;
	String result;
	String nick;
	
	public Talker(Connection connection, GUI gui) {
		this.connection = connection;
		this.gui = gui;
		writer = connection.getWriter();
	}
	
	public void joinRoom(Room room) {
		try {
			if (room.getPwd() != null) { writer.write("JOIN " + room.getName() + " " + room.getPwd() + "\r\n"); }
			else { writer.write("JOIN " + room.getName() + "\r\n"); }
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String message, String room) throws IOException {
		writer.write("PRIVMSG " + room + " :" + message + "\r\n");
		writer.flush();
	}
	
	
	public void sendPong(String line) {
		try {
			writer.write("PONG " + line.substring(5) + "\r\n");
			writer.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public String getNick() {
		return connection.getNick();
	}
	
}
