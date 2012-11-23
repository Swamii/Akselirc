package irc;

import java.awt.Color;
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
		// removes a connection from the array and if it hasn't already, it removes the tab connected to it
		int removeIndex = -1;
		for (Connection c : connections) {
			if (c.equals(connection)) {
				removeIndex = connections.indexOf(c);
				for (int i = 0; i < jtp.getTabCount(); i++) {
					// check if the tab still exists
					if (c.getServerName().equals(jtp.getTitleAt(i))) {
						jtp.removeTabAt(i);
					}
				}
			}
			
		}
		if (removeIndex == -1) {
			return;
		}
		if (connections.get(removeIndex).allGood()) {
			// close the connection if it hasn't already been done
			connections.get(removeIndex).closeCrap();
		}
		connections.remove(removeIndex);
		
		if (connections.size() == 0) {
			// if there are no connections left you can't join a room
			enableJoinRoomMenuItem(false);
		}
	}
	
	public void addConnection(Connection connection) {
		connections.add(connection);
	}
	
	// start connection to server
	public void newServer(String[] details) {
		assert (details.length == 2); // i just wanted to use assert
		Connection connection = new Connection(details[0], details[1], this);
		Thread t = new Thread(connection);
		t.start();
	}
	
	// called by connection when a connection to a server has been established
	public void addServer(Server server) {
		jtp.add(server.getName(), server.getPanel());
		jtp.setTabComponentAt(jtp.getTabCount() - 1, new ButtonTabComponent(jtp, server.getTalker()));
	}
	
	public void serverPopup() {
		new ServerPopup(this);
	}
	
	public void roomPopup() {
		new RoomPopup(this);
	}
	
	// simple error popup
	public void errorPopup(String error) {
		JOptionPane.showMessageDialog(frame,
			    error,
			    "Error",
			    JOptionPane.ERROR_MESSAGE);
	
	}

	public void initGUI() {
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
				quit();
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
	
	private void quit() {
		for (int i = 0; i < connections.size(); i++) {
			connections.get(i).getTalker().leaveServer();
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
