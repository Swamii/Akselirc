package irc.connection;

import java.util.Comparator;

public class CleanUserSorter implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {
		return o1.compareToIgnoreCase(o2);
	}

}
