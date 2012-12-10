package irc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.table.AbstractTableModel;

public class PreferenceTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private String[] columnNames = {"Server", "Nick", "Rooms"};
    private Preferences prefs = Preferences.userNodeForPackage(getClass());
    private ArrayList<String> keys;
    private ArrayList<String> allKeys;
    private ArrayList<String[]> prefsNotSaved;

    public PreferenceTableModel(){
        loadPrefs();
    }
    
    /*
     * this function loads the preferences from the computer, it ultimately puts them in a 2-dimensional array
     * with the order 'server', 'nick', 'rooms'
     */
    private void loadPrefs() {
    	try {
            //prefs.clear();
        	prefsNotSaved = new ArrayList<String[]>();
            keys = new ArrayList<String>();
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
            	prefsNotSaved.add(tempList);
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
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
    		prefsNotSaved.add(tempList);
    		fireTableRowsInserted(row, col);
    	} else {
    		prefsNotSaved.add(row, tempList);
    		prefsNotSaved.remove(row + 1);
    		fireTableCellUpdated(row, col);
    	}
    }

}