package irc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Connection implements Runnable {
	
	private String server;
	private String nick;
	private Socket socket;
	private BufferedWriter writer;
	private BufferedReader reader;
	private GUI gui;
	private ArrayList<Room> rooms = new ArrayList<Room>();
	private Server s;
	private Talker talker;
	private Listener listener;
	private Thread thread;
	private Boolean allGood;

	public Connection(String nick, String server, GUI gui) {
		this.nick = nick;
		this.server = server;
		this.gui = gui;
		allGood = false;
		
		//connect();
	}
	
	public void run() {
		try {
			socket = new Socket(server, 6667);
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			gui.errorPopup("Something went wrong connecting to " + server);
			gui.removeConnection(this);
			return;
		}
		
		try {
			writer.write("NICK " + nick + "\r\n");
			writer.write("USER " + nick + " 8 * : " + nick + "\r\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				if (line.startsWith("PING ")) {
					// some servers like to ping even when connecting :)
					try {
						writer.write("PONG " + line.substring(5) + "\r\n");
						writer.flush();
					} catch (IOException e1) {
						gui.errorPopup("Wuut.. Couldn't send a pong to the server :'(");
					}
				}
				if (line.indexOf("004") >= 0) {
					System.out.println("Connected to " + server);
					createServer();
					break;
				} else if (line.indexOf("433") >= 0) {
					gui.errorPopup("Nickname is already in use");
					break;
				}
			}
		} catch (IOException e) {
			gui.errorPopup("Something happened");
		}
		
	}
	
	public boolean allGood() {
		return allGood;
	}
	
	private void createServer() {
		
		talker = new Talker(this);
		listener = new Listener(this);
		Room r = new Room("Server talk");
		rooms.add(r);
		s = new Server(this);
		s.addServerTalk(r);
		gui.addServer(s);
		thread = new Thread(listener);
		thread.start();
		gui.enableJoinRoomMenuItem(true);
		allGood = true;
		gui.addConnection(this);
		
	}
	
	public void addRoom(Room room) {
		rooms.add(room);
		talker.joinRoom(room);
	}
	
	public ArrayList<Room> getRooms() {
		return rooms;
	}
	
	public String getNick() {
		return nick;
	}
	
	public Server getServer() {
		return s;
	}
	
	public String getServerName() {
		return server;
	}
	
	public GUI getGUI() {
		return gui;
	}
	
	public Listener getListener() {
		return listener;
	}
	
	public Talker getTalker() {
		return talker;
	}
	
	public BufferedWriter getWriter() {
		return writer;
	}
	
	public BufferedReader getReader() {
		return reader;
	}
	
	public void closeCrap() {
		try {
			listener.stop();
			socket.close();
		} catch (IOException e) {
			gui.errorPopup("Something went wrong closing the crap");
		}
	}
	
}
