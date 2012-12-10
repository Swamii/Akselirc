package irc;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

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
	
	private static final long serialVersionUID = 1L;
	public static GUI gui;
	private final static int WIDTH = 800;
	private final static int HEIGHT = 600;
	private volatile ArrayList<Connection> connections = new ArrayList<Connection>();
	private JFrame frame;
	private JTabbedPane jtp;
	private JMenuBar menubar;
	private JMenuItem joinRoom;
	private JMenu file;
	private JMenu edit;
	
	public ArrayList<Connection> getConnections() {
		return connections;
	}
	
	public int getSelectedTab() {
		return jtp.getSelectedIndex();
	}
	
	// function for exiting one connection
	public void removeConnection(Connection connection) {
		
		// check that the crap has been opened before closing it
		if (connection.allGood()) {
			connection.closeCrap();
		}
		
		// if the tab hasn't been removed already (by the ButtonTabComponent), remove it now
		for (int i = 0; i < jtp.getTabCount(); i++) {
			if (jtp.getTitleAt(i).equals(connection.getServerName())) {
				jtp.remove(i);
			}
		}
		
		connections.remove(connection);
		
		System.out.println("# of connections " + connections.size());
		if (connections.size() == 0) {
			// if there are no connections left you can't join a room
			enableJoinRoomMenuItem(false);
		}
	}
	
	public void loadPrefs() {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		ArrayList<String[]> prefsList = new ArrayList<String[]>();
        ArrayList<String> keys = new ArrayList<String>();
        ArrayList<String> allKeys;
		try {
			allKeys = new ArrayList<String>(Arrays.asList(prefs.keys()));
			// allKeys we have all the preferences, but the only ones we need are those related to startup-connections
	        for (String k : allKeys) {
	        	if (k.startsWith("irc.")) {
	        		keys.add(k);
	        	}
	        }
	        for (int i = 0; i < keys.size(); i++) {
	        	String[] tempList = new String[3];
	        	tempList[0] = keys.get(i);
	        	String nickRoom = prefs.get(keys.get(i), "err");
	        	if (!nickRoom.equals("err") && nickRoom.contains(":")) {
	        		if (nickRoom.endsWith(":")) {
	        			tempList[1] = nickRoom.substring(0, nickRoom.length() - 1);
	        			tempList[2] = "";
	        		} else {
	        			String[] nickRoomSplit = nickRoom.split(":");
	            		tempList[1] = nickRoomSplit[0];
	            		tempList[2] = nickRoomSplit[1];
	        		}
	        		
	        	}
	        	prefsList.add(tempList);
	        }
		} catch (BackingStoreException e) {
			errorPopup("Failed to load preferences. Error code 1337.");
		}
		System.out.println(prefsList.size());
		for (String[] list : prefsList) {
			Connection connection = new Connection(list[1], list[0], list[2]);
			synchronized(connection) {
				Thread t = new Thread(connection);
				t.start();
				while (!connection.allGood() && !connection.allBad()) {
					try {
						connection.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	public void addConnection(Connection connection) {
		connections.add(connection);
	}
	
	// start connection to server
	public void newServer(String[] details) {
		assert (details.length == 2); // i just wanted to use assert
		Connection connection = new Connection(details[0], details[1], "");
		Thread t = new Thread(connection);
		t.start();
	}
	
	// called by connection when a connection to a server has been established
	public void addServer(Server server) {
		jtp.add(server.getName(), server.getPanel());
		jtp.setTabComponentAt(jtp.getTabCount() - 1, new ButtonTabComponent(jtp, server.getTalker()));
	}
	
	public void serverPopup() {
		ServerPopup serverPopup = ServerPopup.getInstance();
		serverPopup.setVisible(false);
		serverPopup.setVisible(true);
	}
	
	public void roomPopup() {
		RoomPopup roomPopup = RoomPopup.getInstance();
		roomPopup.setVisible(false);
		roomPopup.setVisible(true);
	}
	
	// simple error popup
	public void errorPopup(String error) {
		JOptionPane.showMessageDialog(frame,
			    error,
			    "Error",
			    JOptionPane.ERROR_MESSAGE);
	}

	public void initGUI() {
		gui = this;
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            UIManager.put("nimbusFocus", new Color(0,0,0,0));
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
		
		setTitle("Akselirc v0.1");
		setSize(WIDTH, HEIGHT);
		
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
				quit();
			}
		});
		
		edit = new JMenu("Edit");
		
		JMenuItem prefs = new JMenuItem("Preferences...");
		prefs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PrefsPopup prefsPopup = PrefsPopup.getInstance();
				prefsPopup.setVisible(false);
				prefsPopup.setVisible(true);
			}
		});
		
		add(menubar);
		edit.add(prefs);
		file.add(connect);
		file.add(joinRoom);
		file.add(exit);
		menubar.add(file);
		menubar.add(edit);
		setJMenuBar(menubar);
		
		getContentPane().add(jtp);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		WindowClosingListener windowListener = new WindowClosingListener();
		addWindowListener(windowListener);
		setVisible(true);
		loadPrefs();
		
	}
	
	// when the program is quitting
	private void quit() {
		setVisible(false);
		for (int i = 0; i < connections.size(); i++) {
			connections.get(i).getTalker().leaveServer();
			if (connections.get(i).allGood()) {
				connections.get(i).closeCrap();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.exit(0);
	}
	
	public void enableJoinRoomMenuItem(boolean b) {
		joinRoom.setEnabled(b);
	}
	
	private class WindowClosingListener implements WindowListener {
		
		@Override
		public void windowClosing(WindowEvent e) {
			quit();
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
