package irc.gui;

import irc.connection.Connection;
import irc.connection.Talker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class PrivChat {
	
	private JPanel panel;
	private JTextField userText;
	private JTextPane chatWindow;
	private StyleContext context;
	private StyledDocument document;
	private Style style;
	private String name;
	private Talker talker;
	private Connection connection;

	public PrivChat(String name, Connection connection) {
		this.name = name;
		this.connection = connection;
		talker = connection.getTalker();
		initGUI();
	}
	
	public String getName() {
		return name;
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	public void initGUI() {
		panel = new JPanel(new BorderLayout());
		
		userText = new JTextField();
		userText.requestFocusInWindow();
		userText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().length() > 0) {
					talker.handleMessage(e.getActionCommand(), getName());
					if (!e.getActionCommand().startsWith("/")) {
						addText(connection.getNick() + ": " + e.getActionCommand());
					}
				}
				userText.setText("");
			}
		});
		userText.setEditable(true);
		
		context = new StyleContext();
		document = new DefaultStyledDocument(context);
		style = context.getStyle(StyleContext.DEFAULT_STYLE);
		chatWindow = new JTextPane(document);
		chatWindow.setEditable(false);
		
		panel.add(userText, BorderLayout.SOUTH);
		panel.add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		
		panel.setBackground(Color.WHITE);
		panel.setSize(800, 600);
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
	
}
