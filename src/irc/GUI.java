package irc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class GUI extends JFrame {
	
	/*
	 * Creates panel with tabs, can join rooms with a button
	 * Keeps list of room
	 */
	
	private static final long serialVersionUID = 1L;
	final static int WIDTH = 800;
	final static int HEIGHT = 600;
	ArrayList<Room> rooms = new ArrayList<Room>();
	JPanel panel;
	JFrame frame;
	JTabbedPane jtp;
	JMenuBar menubar;
	Connection connection;
	Listener listener;
	Talker talker;
	
	public GUI() {
		
		
	}
	
	public void go() {
		initGUI();
		initServerTalk();
		connect();
	}
	
	private void connect() {
		connection = new Connection("irc.eth0.info", this);
		talker = new Talker(connection, this);
		listener = new Listener(connection, this, talker);
		Thread t = new Thread(listener);
		t.start();
	}

	private void createRoom() {
		Room r;
		String room = (String)JOptionPane.showInputDialog(
						frame,
						"Enter the room you want to join.\nIf the room has a password,\nput it after the room name,\nlike so: 'ROOM PASSWORD'\n ",
						"Join Room",
						JOptionPane.PLAIN_MESSAGE);
		if (room != null) {
			if (!room.startsWith("#")) {
				room = "#" + room;
			}
			
			if (room.contains(" ")) {
				String roomName = room.substring(0, room.indexOf(" "));
				String roomPwd = room.substring(room.indexOf(" ") + 1);
				System.out.println(roomName + "! - !" + roomPwd);
				r = new Room(roomName, roomPwd, talker);
			} else { r = new Room(room, talker); }
			
			talker.joinRoom(r);
			rooms.add(r);
		}
	}
	
	public void addRoom(Room room) {
		jtp.add(room.getName(), room.getPanel());
		jtp.setTabComponentAt(jtp.getTabCount() - 1, new ButtonTabComponent(jtp, talker));
		room.setEditable(true);
	}
	
	public ArrayList<Room> getRooms() {
		return rooms;
	}
	
	private void initServerTalk() {
		Room serverTalk = new Room("Server Talk");
		rooms.add(serverTalk);
		jtp.add("Server Talk", serverTalk.getPanel());
	}
	
	private void initGUI() {
		setTitle("Akselirc v0.004");
		setSize(WIDTH, HEIGHT);
		
		frame = new JFrame();
		jtp = new JTabbedPane();
		jtp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		menubar = new JMenuBar();
		JMenu file = new JMenu("File");
		
		JMenuItem joinRoom = new JMenuItem("Join room");
		joinRoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				createRoom();
			}
		});
		
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					connection.closeCrap();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.exit(0);
			}
		});
		
		frame.add(menubar);
		file.add(joinRoom);
		file.add(exit);
		menubar.add(file);
		setJMenuBar(menubar);
		
		getContentPane().add(jtp);
		setLocationRelativeTo(null);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    setVisible(true);
	}

}
