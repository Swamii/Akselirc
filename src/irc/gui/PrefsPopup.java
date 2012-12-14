package irc.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;


public class PrefsPopup extends JDialog {

	/**
	 * This is the JDialog for the Preferences
	 */
	
	private static final long serialVersionUID = 1L;
	private static PrefsPopup PrefsPopupInstance = null;
	private JPanel mainPanel;
	private JPanel okCancelPanel;
	private JPanel tabbedPanePanel;
	private JPanel startupPanel;
	private JPanel buttonPanel;
	private JPanel soundPanel;
	private JPanel colorPanel;
	private JPanel tablePanel;
	private JButton okBtn;
	private JButton cancelBtn;
	private JButton addBtn;
	private JButton remBtn;
	private JButton editBtn;
	private JTabbedPane jtp;
	private ButtonListener buttonListener;
	public static JTable table;
	
	public PrefsPopup() {
		initGUI();
	}
	
	private void initGUI() {
		colorPanel = new JPanel();
		soundPanel = new JPanel();
		buttonListener = new ButtonListener();
		// startupPanel ->
		startupPanel = new JPanel(new BorderLayout());
		startupPanel.setBorder(new EmptyBorder(15,15,15,15));
		
		tablePanel = createTablePanel();
		buttonPanel = createButtonPanel();
		
		JLabel info = new JLabel("Here you can enter the servers " +
				"you want to join at startup.");
		info.setBorder(new EmptyBorder(0, 0, 10, 20));
		info.setFont(new Font("Verdana", Font.BOLD, 12));
		
		startupPanel.add(info, BorderLayout.NORTH);
		startupPanel.add(tablePanel, BorderLayout.CENTER);
		startupPanel.add(buttonPanel, BorderLayout.SOUTH);
		// <- end startupPanel
		
		tabbedPanePanel = createTabPanel();
		okCancelPanel = createOkCancelPanel();

		// main stuff ->
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(tabbedPanePanel, BorderLayout.CENTER);
		mainPanel.add(okCancelPanel, BorderLayout.SOUTH);
		
		add(mainPanel);
		setSize(600, 400);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
		// <- end main stuff
		
	}
	
	// this function is used to make sure there can only be one instance of this class
	// this function is the one called to create the popup
	public static PrefsPopup getInstance() {
		if (PrefsPopupInstance == null) {
			PrefsPopupInstance = new PrefsPopup();
		}
		return PrefsPopupInstance;
	}
	
	private JPanel createTabPanel() {
		tabbedPanePanel = new JPanel(new BorderLayout());
		jtp = new JTabbedPane();
		jtp.setTabPlacement(JTabbedPane.LEFT);
		jtp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		jtp.addTab("Startup", startupPanel);
		jtp.addTab("Sound", soundPanel);
		jtp.addTab("Colors", colorPanel);
		tabbedPanePanel.add(jtp, BorderLayout.CENTER);
		return tabbedPanePanel;
	}
	
	private JPanel createOkCancelPanel() {
		okBtn = createButton("Save");
		getRootPane().setDefaultButton(okBtn);

		cancelBtn = createButton("Cancel");

		okCancelPanel = new JPanel(new FlowLayout());
		okCancelPanel.add(okBtn);
		okCancelPanel.add(cancelBtn);
		
		return okCancelPanel;
	}
	
	private JPanel createTablePanel() {
		
		tablePanel = new JPanel(new BorderLayout());
		
		table = createTable();
		
		tablePanel.add(table.getTableHeader(), BorderLayout.NORTH);
		tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
		
		return tablePanel;
	}
	
	private JTable createTable() {
		
		PreferenceTableModel tableModel = new PreferenceTableModel();
		table = new JTable(tableModel);
		return table;
	}
	
	private JPanel createButtonPanel() {
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		
		addBtn = createButton("Add");
		editBtn = createButton("Edit");
		remBtn = createButton("Remove");
		
		buttonPanel.add(addBtn, FlowLayout.LEFT);
		buttonPanel.add(editBtn);
		buttonPanel.add(remBtn);
		
		return buttonPanel;
	}
	
	private JButton createButton(String name) {
		JButton button = new JButton(name);
		button.setActionCommand(name);
		button.addActionListener(buttonListener);
		return button;
	}
	
	private void destroy() {
		setVisible(false);
		dispose();
	}
	
	private void save() {
		PreferenceTableModel tableModel = (PreferenceTableModel) table.getModel();
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		ArrayList<String[]> prefsList = tableModel.getPrefs();
		try {
			prefs.clear();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		for (String[] list : prefsList) {
			prefs.put(list[0], list[1] + ":" + list[2]);
		}
		
		destroy();
	}
	
	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("Save")) {
				save();
			} else if (e.getActionCommand().equals("Cancel")) {
				destroy();
			} else if (e.getActionCommand().equals("Add")) {
				new PrefsEditServerPopup();
			} else if (e.getActionCommand().equals("Edit")) {
				if (table.getSelectedRow() != -1) {
					int row = table.getSelectedRow();
					new PrefsEditServerPopup( 
							(String)table.getValueAt(row, 0), 
							(String)table.getValueAt(row, 1), 
							(String)table.getValueAt(row, 2),
							row);
				}
			} else if (e.getActionCommand().equals("Remove")) {
				if (table.getSelectedRow() != -1) {
					int[] rows = table.getSelectedRows();
					PreferenceTableModel tableModel = (PreferenceTableModel) table.getModel();
					tableModel.removeRow(rows);
				}
			}
		}
	}
}
