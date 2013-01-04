package irc.connection;

import java.util.Comparator;

/**
 * Comparator for user names used in irc clients.
 * It puts all names in the order '@' > '+' > anything else.
 * @author Akseli Nelander
 *
 */

public class UserSorter implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {
		char a = o1.charAt(0);
		char b = o2.charAt(0);
		if (a == '@' || a == '+' || b == '@' || b == '+') {
			if (a == b) return o1.compareToIgnoreCase(o2);
			else if (a == '@') return -1;
			else if (b == '@') return 1;
			else if (a == '+') return -1;
			else return 1;
		} else return o1.compareToIgnoreCase(o2);
	}

}
