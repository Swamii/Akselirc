package irc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/*
 * Starts connection with server
 * Listener takes over when a connection is established
 */

public class Connection {
	
	String server;
	String nick;
	Socket socket;
	BufferedWriter writer;
	BufferedReader reader;
	GUI gui;
	
	public Connection(String server, GUI gui) {
		this.server = server;
		this.gui = gui;
		nick = "zeWeakClient";
		
		connect();
	}
	
	private void connect() {
		try {
			socket = new Socket(server, 6667);
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			writer.write("NICK " + nick + "\r\n");
			writer.write("USER " + nick + " 8 * : swamoo weak client!\r\n");
			writer.flush( );
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		String line = null;
		String result = "Something went wrong connecting to the server :(\n";
		try {
			while ((line = reader.readLine()) != null) {
				if (line.indexOf("004") >= 0) {
					result = "You are connected to " + server + "\n";
					System.out.println(result);
					gui.setServerTalkTitle(server);
					gui.enableMenubar(true);
					break;
				} else if (line.indexOf("433") >= 0) {
					result = "Nickname is already in use.\n";
					System.out.println(result);
					break;
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BufferedReader getReader() {
		return reader;
	}
	
	public BufferedWriter getWriter() {
		return writer;
	}
	
	public String getNick() {
		return nick;
	}
	
	public void closeCrap() throws IOException {
		writer.close();
		reader.close();
		socket.close();
	}
}
