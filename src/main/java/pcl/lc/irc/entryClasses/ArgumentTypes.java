package pcl.lc.irc.entryClasses;

import java.util.HashMap;

public class ArgumentTypes {
	public static String STRING = "String";
	public static String INTEGER = "Integer";
	public static String DOUBLE = "Double";
	public static String BOOLEAN = "Boolean";
	public static String LIST = "List";

	public static HashMap<String, String> getList() {
		HashMap<String, String> items = new HashMap<>();
		items.put(STRING, "Strings are either a single word (no whitespaces) or a sentence encased in double-quotes.");
		items.put(INTEGER, "Integers are numbers without decimal points.");
		items.put(DOUBLE, "Doubles are numbers that may include decimal points.");
		items.put(BOOLEAN, "A boolean can be the words \"true\" or \"false\", or a bit (1 or 0).");
		items.put(LIST, "A list is one or more strings. These can be either single words, or quoted strings as per the String type.");
		return items;
	}
}
