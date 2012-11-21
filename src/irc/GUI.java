package irc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

public class GUI extends JFrame {
	
	private static final long serialVersionUID = -6734790752015365789L;
	private final static int WIDTH = 800;
	private final static int HEIGHT = 600;
	private ArrayList<Connection> connections = new ArrayList<Connection>();
	private JFrame frame;
	private JTabbedPane jtp;
	private JMenuBar menubar;
	private JMenuItem joinRoom;
	private JMenu file;
	
	public ArrayList<Connection> getConnections() {
		return connections;
	}
	
	public int getSelectedTab() {
		return jtp.getSelectedIndex();
	}
	
	public void removeConnection(Connection connection) {
		int removeIndex = -1;
		for (Connection c : connections) {
			if (c.equals(connection)) {
				removeIndex = connections.indexOf(c);
			}
		}
		if (connections.get(removeIndex).allGood()) {
			connections.get(removeIndex).closeCrap();
		}
		connections.remove(removeIndex);
		
		if (connections.size() == 0) {
			enableJoinRoomMenuItem(false);
		}
	}
	
	public void addConnection(Connection connection) {
		connections.add(connection);
		for (Connection c : connections) {
			System.out.println("---" + c.getServerName());
		}
	}
	
	// start connection to server
	public void newServer(String[] details) {
		assert (details.length == 2); // i just wanted to use assert
		Connection connection = new Connection(details[0], details[1], this);
		Thread t = new Thread(connection);
		t.start();
		if (connection.allGood()) {
			connections.add(connection);
		}
		System.out.println(connections.size());
	}
	
	public void serverPopup() {
		new ServerPopup(this);
	}
	
	public void newRoom(String[] details) {
		assert (details.length == 2);
		String server = details[0];
		String room = details[1];
		String pwd = null;
		if (room.contains(" ")) {
			pwd = room.substring(room.indexOf(" ") + 1);
			room = room.substring(0, room.indexOf(" "));
			System.out.println(room + " " + pwd);
		}
		if (!room.startsWith("#")) {
			room = "#" + room;
		}
		
		for (Connection connection : connections) {
			if (server.equals(connection.getServerName())) {
				if (pwd != null) {
					connection.addRoom(new Room(room, pwd, connection));
				} else {
					connection.addRoom(new Room(room, connection));
				}
			}
		}
	}
	
	public void roomPopup() {
		new RoomPopup(this);
	}
	
	// should maybe be made into its own class soon enough
	public void errorPopup(String error) {
		JOptionPane.showMessageDialog(frame,
			    error,
			    "Error",
			    JOptionPane.ERROR_MESSAGE);
	
	}
	
	// called by connection when a connection to a server has been established
	public void addServer(Server server) {
		jtp.add(server.getName(), server.getPanel());
		jtp.setTabComponentAt(jtp.getTabCount() - 1, new ButtonTabComponent(jtp, server.getTalker()));
	}

	public void initGUI() {
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException | UnsupportedLookAndFeelException e1) {
				e1.printStackTrace();
			}
		}
		
		setTitle("Akselirc v0.01");
		setSize(WIDTH, HEIGHT);
		
		//frame = new JFrame();
		jtp = new JTabbedPane(JTabbedPane.TOP);
		jtp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		menubar = new JMenuBar();
		file = new JMenu("File");
		
		JMenuItem connect = new JMenuItem("New connection...");
		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				serverPopup();
			}
		});
		
		joinRoom = new JMenuItem("Join room...");
		joinRoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				roomPopup();
			}
		});
		
		joinRoom.setEnabled(false);
		
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				for (int i = 0; i < connections.size(); i++) {
					connections.get(i).getTalker().leaveServer();
				}
				System.exit(0);
			}
		});
		
		add(menubar);
		file.add(connect);
		file.add(joinRoom);
		file.add(exit);
		menubar.add(file);
		setJMenuBar(menubar);
		
		getContentPane().add(jtp);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		WindowClosingListener windowListener = new WindowClosingListener();
		addWindowListener(windowListener);
		setVisible(true);
		
	}
	
	public void enableJoinRoomMenuItem(boolean b) {
		joinRoom.setEnabled(b);
	}
	
	private class WindowClosingListener implements WindowListener {
		
		@Override
		public void windowClosing(WindowEvent e) {
			// handle closing sockets n stuff
			for (int i = 0; i < connections.size(); i++) {
				connections.get(i).getTalker().leaveServer();
			}
			System.exit(0);
		}
		
		@Override
		public void windowOpened(WindowEvent e) {}
		@Override
		public void windowClosed(WindowEvent e) {}
		@Override
		public void windowIconified(WindowEvent e) {}
		@Override
		public void windowDeiconified(WindowEvent e) {}
		@Override
		public void windowActivated(WindowEvent e) {}
		@Override
		public void windowDeactivated(WindowEvent e) {}
	}
}
