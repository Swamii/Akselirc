package irc;

import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class Server {
	
	private Connection connection;
	private String name;
	private JPanel panel;
	private JTabbedPane jtp;
	private Talker talker;
	private ArrayList<Room> rooms;

	public Server(Connection connection) {
		this.connection = connection;
		name = connection.getServerName();
		talker = connection.getTalker();
		
		initGUI();
	}	
	
	private void initGUI() {
		panel = new JPanel(new GridLayout(1, 1));
		jtp = new JTabbedPane();
		panel.add(jtp);
	}
	
	public void addServerTalk(Room room) {
		jtp.add(room.getName(), room.getPanel());
	}
	
	public void addRoom(String roomName) {
		rooms = connection.getRooms();
		for (Room r : rooms) {
			if (roomName.equals(r.getName())) {
				jtp.add(r.getName(), r.getPanel());
				jtp.setTabComponentAt(jtp.getTabCount() - 1, new ButtonTabComponent(jtp, connection.getTalker()));
				r.setEditable(true);
			}
		}
		
	}

	public String getName() {
		return name;
	}
	
	public Talker getTalker() {
		return talker;
	}

	public JPanel getPanel() {
		return panel;
	}
}
