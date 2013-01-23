package irc.gui;

import irc.connection.CleanUserSorter;
import irc.connection.Connection;
import irc.connection.Talker;
import irc.connection.UserSorter;
import irc.misc.ListAction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * The Room class. It handles everything concerning rooms.
 * A problem which will probably come into play went entering
 * multiple channels with many users is the too much computing is
 * handled on the Swing EventQueue
 * @author Akseli
 *
 */

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
	private ArrayList<String> usersClean;
	private JScrollPane outerUserWindow;
	private String name;
	private Talker talker;
	private Connection connection;
	private Server server;
	private UserSorter userSorter;
	private CleanUserSorter cleanSorter;
	private ButtonListener buttonListener;

	// constructor for the server talk
	public Room(Connection connection) {
		this.name = "Server talk";
		this.connection = connection;
		initServerTalk();
	}
	
	// constructor for normal room
	public Room(String name, Connection connection) {
		this.connection = connection;
		userSorter = new UserSorter();
		cleanSorter = new CleanUserSorter();
		
		// check if the room has a password
		if (name.contains(" ")) {
			this.name = name.split(" ")[0];
		} else {
			this.name = name;
		}
		talker = connection.getTalker();
		server = connection.getServer();
		
		usersClean = new ArrayList<String>();
		
		initGUI();
	}
	
	public void setEditable(boolean b) {
		userText.setEditable(b);
	}

	public void addText(final String text) {
		final Calendar cal = Calendar.getInstance();
		final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		if (text.contains("http://") || text.contains("https://")) {
			int startIndex = text.indexOf("http");
			int endIndex;
			
			/*
			if (text.contains(" ")) {
				endIndex = text.indexOf(" ", startIndex);
			} else {
				endIndex = text.length() - 1;
			}
			
			System.out.println(text.substring(startIndex, endIndex));
			*/
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					StyleConstants.setForeground(style, Color.BLUE);
					document.insertString(document.getLength(),
							"[" + sdf.format(cal.getTime()) + "] ",
							style);
					StyleConstants.setForeground(style, Color.BLACK);
					document.insertString(document.getLength(),
							text + "\n", 
							style);
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
			public void run() {
				users.addElement(user);
				sort();
				userWindow.setModel(users);
				outerUserWindow.revalidate();
				outerUserWindow.repaint();
			}
		});
		addMessage(user + " has joined " + name);
	}
	
	// used to add all users when first entering a room
	public void addAllUsers(final String[] listOfUsers) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for (String user : listOfUsers) {
					users.addElement(user);
				}
				sort();
				userWindow.setModel(users);
				outerUserWindow.revalidate();
				outerUserWindow.repaint();
			}
		});
	}
	
	public void removeUser(final String user, String reason) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				users.removeElement(user);
				sort();
				userWindow.setModel(users);
				outerUserWindow.revalidate();
				outerUserWindow.repaint();
			}
		});
		addMessage(user + reason);
	}

	// when the users MODE gets changed (i.e user given op status or voice)
	public void updateName(String operator, final String name, final String change) {
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				users.removeElement(name);
				switch (change) {
				case "+o":
					users.addElement("@" + name);
					break;
				case "+v":
					users.addElement("+" + name);
					break;
				case "-v":
				case "-o":
					users.addElement(fixName(name));
					break;
				}
				sort();
				userWindow.setModel(users);
				outerUserWindow.revalidate();
				outerUserWindow.repaint();
			}
		});
		addMessage(operator + " changed " + name + " (" + change + ")"); 
	}
	
	/**
	 * if someone changes their name, this gets called
	 * from the listener.
	 * @param name The old name
	 * @param newName The new name
	 */
	public void changeName(final String name, final String newName) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				users.removeElement(name);
				users.addElement(newName);
				sort();
				userWindow.setModel(users);
				outerUserWindow.revalidate();
				outerUserWindow.repaint();
			}
		});
		addMessage(name + " changed his name to " + newName);
	}

	// better sorting function
	private void sort() {
		String[] userArray = new String[users.size()];
		String[] cleanArray = new String[users.size()];
		for (int i = 0; i < users.size(); i++) {
			userArray[i] = users.get(i);
			cleanArray[i] = fixName(users.get(i));
		}
		usersClean.clear();
		users.clear();
		Arrays.sort(cleanArray, cleanSorter);
		for (String s : cleanArray) {
			usersClean.add(s);
		}
		Arrays.sort(userArray, userSorter);
		for (String s : userArray) {
			users.addElement(s);
		}
	}
	
	/**
	 * Returns the next user in the list.
	 * Used by the KeyListener.
	 * @param lastWord
	 * @param switchWord The letters which the user wants to check
	 * @return
	 */
	private String getNext(String lastWord, String switchWord) {
		int nextIndex = usersClean.indexOf(lastWord) + 1;
		String nextWord = lastWord;
		if (nextIndex < usersClean.size() && 
				usersClean.get(nextIndex).toLowerCase().startsWith(switchWord.toLowerCase())) {
			nextWord = usersClean.get(nextIndex);
		}
		return nextWord;
	}
	
	protected void initGUI() {
		buttonListener = new ButtonListener();
		chatPanel = new JPanel(new BorderLayout());
		
		createUserText();
		
		context = new StyleContext();
		document = new DefaultStyledDocument(context);
		style = context.getStyle(StyleContext.DEFAULT_STYLE);

		final Desktop desktop = Desktop.getDesktop();
		chatWindow = new JTextPane(document);
		chatWindow.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
					try {
						System.out.println(e.getURL());
						chatWindow.setPage(e.getURL());
						try {
							desktop.browse(new URI(e.getURL().toString()));
						} catch (URISyntaxException ex) {
							ex.printStackTrace();
						}
					} catch (IOException ex) {
						ex.printStackTrace();
					}

				}
			}
		});
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
	
	private void createUserText() {
		userText = new JTextField();
		userText.setEditable(false);
		userText.requestFocusInWindow();
		userText.addKeyListener(buttonListener);
		userText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String text = event.getActionCommand();
				if (text.trim().length() > 0) {
					talker.handleMessage(text, name);
					if (!text.startsWith("/")) {
						addText(connection.getNick() + ": " + text);
					}
					userText.setText("");
				}
			}
		});
		// disable the tab-key for this field to make sure we can use it to tab through users
		userText.setFocusTraversalKeysEnabled(false);
		
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
				String text = event.getActionCommand();
				if (text.startsWith("/")) {
					connection.getTalker().handleMessage(text, name);
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
	
	/**
	 * This class handles the tab-completion feature
	 * when you quickly want to type a name you can 
	 * type the first letter and then tab through all
	 * the names starting with that letter. Multiple letters
	 * works as well
	 * @author Akseli
	 *
	 */
	private class ButtonListener implements KeyListener {
		
		private String switchWord;
		
		public void keyTyped(KeyEvent e) {
		}

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_TAB && userText.getText().length() > 0) {
				String words = userText.getText();
				String lastWord = "";
				String allButLastWord = "";

				if (words.contains(" ")) {
					lastWord = words.substring(words.lastIndexOf(" ") + 1);
					allButLastWord = words.substring(0, words.length() - lastWord.length());
				} else {
					lastWord = words;
				}
				
				// if you want to tab to the next name
				if (usersClean.contains(lastWord)) {
					if (switchWord == null) {
						switchWord = lastWord;
					}
					System.out.println(switchWord + ", " + lastWord);
					userText.setText(allButLastWord + getNext(lastWord, switchWord));
				} 
				// if you have a incomplete name typed
				else {

					switchWord = lastWord;
					
					for (int i = 0; i < usersClean.size(); i++) {
						if (usersClean.get(i).toLowerCase().startsWith(lastWord.toLowerCase())) {
							userText.setText(allButLastWord + usersClean.get(i));
							break;
						}
					}
				}
			}
		}
		
		public void keyReleased(KeyEvent e) {
		}
	}
	
}