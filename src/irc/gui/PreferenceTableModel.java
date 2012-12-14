package irc.gui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class PreferenceTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private String[] columnNames = {"Server", "Nick", "Rooms"};
    private ArrayList<String[]> prefsNotSaved;

    public PreferenceTableModel(){
        prefsNotSaved = GUI.gui.loadPrefs();
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public int getRowCount() {
        return prefsNotSaved.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
    
    public ArrayList<String[]> getPrefs() {
    	return prefsNotSaved;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
    
    public void removeRow(int[] rows) {
    	for (int i = rows.length - 1; i > -1; i--) {
    		prefsNotSaved.remove(rows[i]);
    	}
 
        fireTableRowsDeleted(rows[0], rows[rows.length - 1]);
    }

    @Override
    public Object getValueAt(int row, int col) {
        Object value = null;
        if (row < prefsNotSaved.size()) {
            String key = prefsNotSaved.get(row)[0];
            String nick = prefsNotSaved.get(row)[1];
            String rooms = prefsNotSaved.get(row)[2];
            if (col==0) {
            	return key;
            } else if (col == 1) {
            	return nick;
            } else if (col == 2) {
            	return rooms;
            } else {
            	System.err.println("getValue err :(");
            }
        }
        return value;
    }

    @Override
    public void setValueAt(Object o, int row, int col) {
    	String value = o.toString();
    	String[] tempList;
    	boolean newRow = false;
    	if (row < prefsNotSaved.size()) {
    		tempList = prefsNotSaved.get(row);
    	} else {
    		newRow = true;
    		tempList = new String[3];
    	}
    	
    	// if it is the first column (ie the server column)
    	if (col == 0) {
    		tempList[0] = value;
    	} else if (col == 1) {
        	tempList[1] = value;
        } else if (col == 2) {
        	tempList[2] = value;
    	} else {
        	System.err.println("setValue err :(((");
        }
    	if (newRow) {
    		// new row, add it to last place and tell the table there is a new row
    		prefsNotSaved.add(tempList);
    		fireTableRowsInserted(row, col);
    	} else {
    		// update row, put it in the right place, and remove the old one
    		prefsNotSaved.add(row, tempList);
    		prefsNotSaved.remove(row + 1);
    		fireTableCellUpdated(row, col);
    	}
    }

}