package irc.gui;

import irc.connection.Connection;
import irc.connection.Talker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class Room {
	
	private JPanel mainPanel;
	private JPanel chatPanel;
	private JTextField userText;
	private JTextPane chatWindow;
	private JTextArea topicText;
	private StyleContext context;
	private StyledDocument document;
	private Style style;
	private JList<String> userWindow;
	private DefaultListModel<String> users;
	private JScrollPane outerUserWindow;
	private String name;
	private Talker talker;
	private Connection connection;
	private Server server;

	// constructor for the server talk
	public Room(Connection connection) {
		name = "Server talk";
		this.connection = connection;
		talker = connection.getTalker();
		initServerTalk();
	}
	
	// constructor for normal room
	public Room(String name, Connection connection) {
		this.connection = connection;
		
		// check if the room has a password
		if (name.contains(" ")) {
			this.name = name.split(" ")[0];
		} else {
			this.name = name;
		}
		talker = connection.getTalker();
		server = connection.getServer();
		initGUI();
	}
	
	public void setEditable(boolean b) {
		userText.setEditable(b);
	}

	public void addText(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				StyleConstants.setForeground(style, Color.BLACK);
				try {
					document.insertString(document.getLength(), text + "\n", style);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				chatWindow.setCaretPosition(document.getLength());
			}
		});
	}
	
	public void addMessage(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				StyleConstants.setForeground(style, Color.RED);
				try {
					document.insertString(document.getLength(), " - " + text + " - \n", style);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				chatWindow.setCaretPosition(document.getLength());
			}
		});
	}
	
	public void addMOTD(final String topic) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				topicText.setText("Topic: " + topic);
				topicText.setVisible(true);
			}
		});
		
	}
	
	// used to add a single user who enters the room
	public void addUser(final String user) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				users.addElement(user);
				sort(users);
				userWindow.setModel(users);
				outerUserWindow.revalidate();
				outerUserWindow.repaint();
				addMessage(user + " has joined " + name);
			}
		});
	}
	
	// used to add all users when first entering a room
	public void addAllUsers(final String[] listOfUsers) {
		for (String user : listOfUsers) {
			users.addElement(user);
		}
		sort(users);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				userWindow.setModel(users);
				outerUserWindow.revalidate();
				outerUserWindow.repaint();
			}
		});
	}
	
	// very bad sorting function
	private void sort(DefaultListModel<String> users) {
		String[] userArray = new String[users.size()];
		for (int i = 0; i < users.size(); i++) {
			userArray[i] = users.get(i);
		}
		users.clear();
		Arrays.sort(userArray);
		for (int i = 0; i < userArray.length; i++) {
			users.addElement(userArray[i]);
		}
	}
	
	public void removeUser(final String user) {
		users.removeElement(user);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				userWindow.setModel(users);
				outerUserWindow.revalidate();
				outerUserWindow.repaint();
				addMessage(user + " has left " + name);
			}
		});
	}
	
	protected void initGUI() {
		ButtonListener buttonListener = new ButtonListener();
		chatPanel = new JPanel(new BorderLayout());
		
		userText = new JTextField();
		userText.setEditable(false);
		userText.requestFocusInWindow();
		userText.addKeyListener(buttonListener);
		userText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				talker.handleMessage(event.getActionCommand(), getName());
				if (!event.getActionCommand().startsWith("/")) {
					addText(connection.getNick() + ": " + event.getActionCommand());
				}
				userText.setText("");
			}
		});
		
		context = new StyleContext();
		document = new DefaultStyledDocument(context);
		style = context.getStyle(StyleContext.DEFAULT_STYLE);
		
		chatWindow = new JTextPane(document);
		chatWindow.setEditable(false);
		
		topicText = new JTextArea();
		topicText.setForeground(Color.BLUE);
	    topicText.setWrapStyleWord(true);
	    topicText.setLineWrap(true);
	    topicText.setEditable(false);
	    topicText.setText("Topic: ");
	    topicText.setVisible(false);
		
		chatPanel.add(userText, BorderLayout.SOUTH);
		chatPanel.add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		chatPanel.add(topicText, BorderLayout.NORTH);
		chatPanel.setPreferredSize(new Dimension(660, 500));
		
		mainPanel = new JPanel(new BorderLayout());
		
		users = new DefaultListModel<String>();
		userWindow = new JList<String>(users);
		
		Action action = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unchecked")
				JList<String> list = (JList<String>) e.getSource();
				server.addPrivChat(fixName(list.getSelectedValue()));
			}
		};
		
		userWindow.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
		        @SuppressWarnings("unchecked")
				JList<String> list = (JList<String>)evt.getSource();
		        if (evt.getClickCount() == 2) {
		            server.addPrivChat(fixName(list.getSelectedValue()));
		        }
		    }
		});
		
		@SuppressWarnings("unused")
		ListAction la = new ListAction(userWindow, action);
		
		outerUserWindow = new JScrollPane(userWindow);
		outerUserWindow.setPreferredSize(new Dimension(175, 500));
		
		mainPanel.add(outerUserWindow, BorderLayout.EAST);
		mainPanel.add(chatPanel, BorderLayout.CENTER);
		
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setSize(800, 600);
	}
	
	private String fixName(String name) {
		if (name.startsWith("@") || name.startsWith("+")) {
			name = name.substring(1);
		}
		return name;
	}
	
	private void initServerTalk() {
		context = new StyleContext();
		document = new DefaultStyledDocument(context);
		style = context.getStyle(StyleContext.DEFAULT_STYLE);
		
		chatWindow = new JTextPane(document);
		chatWindow.setEditable(false);
		
		mainPanel = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(chatWindow);
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		
		userText = new JTextField();
		userText.requestFocusInWindow();
		userText.setEditable(false);
		userText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getActionCommand().startsWith("/")) {
					talker.handleMessage(event.getActionCommand(), getName());
				}
				userText.setText("");
			}
		});
		mainPanel.add(userText, BorderLayout.SOUTH);
		
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setSize(800, 600);
	}
	
	public JPanel getPanel() {
		return mainPanel;
	}

	public String getName() {
		return name;
	}
	
	public DefaultListModel<String> getUsers() {
		return users;
	}
	
	private class ButtonListener implements KeyListener {

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_TAB) {
				System.out.println("TAB!");
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
}