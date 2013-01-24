package irc.gui;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;


public class PrefsEditServerPopup extends JDialog {
	
	private static final long serialVersionUID = 1L;
	private String server;
	private String nick;
	private String rooms;
	private final JPanel contentPanel = new JPanel();
	private JFormattedTextField serverField;
	private JFormattedTextField nickField;
	private JFormattedTextField roomsField;
	private JButton okButton;
	private JButton cancelButton;
	private boolean isEdit;
	private int row;
	private JTable table;

	/**
	 * Create the dialog.
	 */
	public PrefsEditServerPopup() {
		table = PrefsPopup.table;
		server = "";
		nick = "";
		rooms = "";
		row = table.getRowCount();
		System.out.println(row);
		setTitle("Add");
		isEdit = false;
		initGUI();
	}
	
	public PrefsEditServerPopup(String server, String nick, String rooms, int row) {
		table = PrefsPopup.table;
		this.server = server;
		this.nick = nick;
		this.rooms = rooms;
		this.row = row;
		setTitle("Edit");
		isEdit = true;
		initGUI();
	}
	
	
	public static PrefsEditServerPopup getInstance() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void initGUI() {
		setResizable(false);
		setBounds(100, 100, 301, 236);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		JLabel lblServer = new JLabel("Server:");
		lblServer.setFont(new Font("Verdana", Font.BOLD, 12));
		JLabel lblNick = new JLabel("Nick:");
		lblNick.setFont(new Font("Verdana", Font.BOLD, 12));
		JLabel lblRooms = new JLabel("Rooms:");
		lblRooms.setFont(new Font("Verdana", Font.BOLD, 12));
		
		FieldListener fieldListener = new FieldListener();
		serverField = new JFormattedTextField();
		serverField.addKeyListener(fieldListener);
		serverField.setText(server);
		
		nickField = new JFormattedTextField();
		nickField.addKeyListener(fieldListener);
		nickField.setText(nick);
		
		roomsField = new JFormattedTextField();
		roomsField.addKeyListener(fieldListener);
		roomsField.setText(rooms);
		
		JLabel lblAddMultipleRooms = new JLabel("Add multiple rooms separated by a comma");
		lblAddMultipleRooms.setFont(new Font("Verdana", Font.PLAIN, 11));
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(lblServer)
								.addComponent(lblNick))
							.addGap(18)
							.addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
								.addComponent(nickField, GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
								.addComponent(serverField, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)))
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addComponent(lblRooms)
							.addGap(18)
							.addComponent(roomsField, GroupLayout.PREFERRED_SIZE, 205, GroupLayout.PREFERRED_SIZE))
						.addComponent(lblAddMultipleRooms))
					.addContainerGap())
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblServer)
						.addComponent(serverField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNick)
						.addComponent(nickField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(15)
					.addComponent(lblAddMultipleRooms)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblRooms)
						.addComponent(roomsField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(13, Short.MAX_VALUE))
		);
		contentPanel.setLayout(gl_contentPanel);
		
		// start button panel 
		{
			JPanel buttonPane = new JPanel();
			ButtonListener buttonListener = new ButtonListener();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				okButton.setEnabled(isEdit);
				okButton.addActionListener(buttonListener);
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(buttonListener);
				buttonPane.add(cancelButton);
			}
		}
		// end button panel 
		
		setLocationRelativeTo(getParent());
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	private void destroy() {
		setVisible(false);
		dispose();
	}
	
	private void submit() {
		String server = serverField.getText().toLowerCase();
		String nick = nickField.getText();
		String rooms = checkRooms();

		table.setValueAt(server, row, 0);
		table.setValueAt(nick, row, 1);
		table.setValueAt(rooms, row, 2);
		
		setVisible(false);
		dispose();	
	}
	
	private String checkRooms() {
		String room = roomsField.getText();
		if (room.equals("")) return room;
		if (room.contains(" ")) room = room.replace(" ", "");
		if (!room.startsWith("#")) room = "#" + room;
		if (room.contains(",")) {
			String temp = "";
			for (String s : room.split(",")) {
				if (!s.startsWith("#")) s = "#" + s;
				if (temp.equals("")) temp = s;
				else temp = temp + "," + s;
			}
			room = temp;
		}
		return room;
	}
	
	private class FieldListener implements KeyListener {
		@Override
		public void keyReleased(KeyEvent e) {
			String server = serverField.getText().toLowerCase();
			String nick = nickField.getText();
			// check if its a semi-valid irc-server, if it is then enable the ok button
			if (nick.length() > 0 && server.startsWith("irc.") && !nick.contains(" ") && !server.contains(" ")) {
				okButton.setEnabled(true);
			} else {
				okButton.setEnabled(false);
			}
		}
		@Override
		public void keyTyped(KeyEvent e) {}
		@Override
		public void keyPressed(KeyEvent e) {
			// enabling the enter key to connect faster
			if (KeyEvent.VK_ENTER == e.getKeyCode() && okButton.isEnabled()) {
				submit();
			}
		}
	}
	
	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("OK")) {
				System.out.println("ok");
				submit();
			} else if (e.getActionCommand().equals("Cancel")) {
				destroy();
			}
		}
	}
	
}
