package irc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
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
	private StyleContext context;
	private StyledDocument document;
	private Style style;
	private JList<String> userWindow;
	private DefaultListModel<String> users;
	private JScrollPane outerUserWindow;
	private String pwd;
	private String name;
	private Talker talker;
	private Connection connection;

	// constructor for the server talk
	public Room(String name) {
		this.name = name;
		initServerTalk();
	}
	
	// constructor for normal room
	public Room(String name, Connection connection) {
		this.connection = connection;
		this.name = name;
		pwd = null;
		talker = connection.getTalker();
		initGUI();
	}
	
	// constructor for normal room with pwd
	public Room(String name, String pwd, Connection connection) {
		this.connection = connection;
		this.name = name; 
		this.pwd = pwd;
		talker = connection.getTalker();
		initGUI();
	}
	
	public void setEditable(boolean b) {
		userText.setEditable(b);
	}

	public JPanel getPanel() {
		return mainPanel;
	}

	public String getName() {
		return name;
	}
	
	protected String getPwd() {
		return pwd;
	}
	
	public DefaultListModel<String> getUsers() {
		return users;
	}

	public void addText(String text) {
		StyleConstants.setForeground(style, Color.BLACK);
		try {
			document.insertString(document.getLength(), text + "\n", style);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	public void addMessage(String text) {
		StyleConstants.setForeground(style, Color.RED);
		try {
			document.insertString(document.getLength(), " - " + text + " - \n", style);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void addUser(String user) {
		users.addElement(user);
		sort(users);
		userWindow.setModel(users);
		outerUserWindow.revalidate();
		outerUserWindow.repaint();
	}
	
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
	
	public void removeUser(String user) {
		users.removeElement(user);
		userWindow.setModel(users);
		outerUserWindow.revalidate();
		outerUserWindow.repaint();
	}
	
	private void initGUI() {
		
		chatPanel = new JPanel(new BorderLayout());
		
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				talker.sendMessage(event.getActionCommand(), getName());
				addText(connection.getNick() + ": " + event.getActionCommand());
				userText.setText("");
			}
		});
		
		context = new StyleContext();
		document = new DefaultStyledDocument(context);
		style = context.getStyle(StyleContext.DEFAULT_STYLE);
		
		chatWindow = new JTextPane(document);
		chatWindow.setEditable(false);
		
		chatPanel.add(userText, BorderLayout.SOUTH);
		chatPanel.add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		chatPanel.setPreferredSize(new Dimension(660, 500));
		
		mainPanel = new JPanel(new BorderLayout());
		
		users = new DefaultListModel<String>();
		userWindow = new JList<String>(users); 
		
		outerUserWindow = new JScrollPane(userWindow);
		outerUserWindow.setPreferredSize(new Dimension(175, 500));
		
		mainPanel.add(outerUserWindow, BorderLayout.EAST);
		mainPanel.add(chatPanel, BorderLayout.CENTER);
		
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setSize(800, 600);
	}
	
	private void initServerTalk() {
		context = new StyleContext();
		document = new DefaultStyledDocument(context);
		style = context.getStyle(StyleContext.DEFAULT_STYLE);
		
		chatWindow = new JTextPane(document);
		chatWindow.setEditable(false);
		
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setSize(800, 600);
	}
}
