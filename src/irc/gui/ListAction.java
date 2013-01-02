package irc.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JList;
import javax.swing.KeyStroke;

public class ListAction implements MouseListener {

	private final static KeyStroke ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
	private JList<String> list;
	private KeyStroke keyStroke;
		
	public ListAction(JList<String> list, Action action) {
		this(list, action, ENTER);
	}
		
	public ListAction(JList<String> list, Action action, KeyStroke keyStroke) {
		this.list = list;
		this.keyStroke = keyStroke;
			
		InputMap im = list.getInputMap();
		im.put(keyStroke, keyStroke);
			
		setAction(action);
			
	}
		
	public void setAction(Action action) {
		list.getActionMap().put(keyStroke, action);
	}
		
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			Action action = list.getActionMap().get(keyStroke);
			
			
			if (action != null) {
				ActionEvent event = new ActionEvent(
						list,
						ActionEvent.ACTION_PERFORMED,
						"");
				action.actionPerformed(event);
			}
		}
	}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
}
