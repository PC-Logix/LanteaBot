package pcl.lc.utils;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.util.ArrayList;

import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.IRCBot;

public class Helper {
	public static final Charset utf8 = Charset.forName("UTF-8");
	
	@SuppressWarnings("unchecked")
	public static <T> boolean equalsOR(T instance, T... compareWith) {
		for (T t : compareWith) if (instance.equals(t)) return true;
		return false;
	}
	@SuppressWarnings("unchecked")
	public static <T> boolean equalsAND(T instance, T... compareWith) {
		for (T t : compareWith) if (!instance.equals(t)) return false;
		return true;
	}
	
	@SuppressWarnings("unchecked") public static <T, S extends T> ArrayList<S> getAllOfType(ArrayList<T> list, Class<S> type) {
		ArrayList<S> newList = new ArrayList<S>();
		for (T object : list) if (type.isInstance(object)) newList.add((S)object);
		return newList;
	}
	public static <T, S extends T> ArrayList<T> removeAllOfType(ArrayList<T> list, Class<S> type) {
		ArrayList<T> newList = new ArrayList<T>();
		for (T object : list) if (!type.isInstance(object)) newList.add(object);
		return newList;
	}
	
	public static int countCharOccurences(String string, char needle) {
		int count = 0;
		for (int i = 0; i < string.length(); i++) if (string.charAt(i) == needle) count++;
		return count;
	}
	
	public static String getFilenameExt(File file) {
		String name = file.getName();
		if (name.contains(".")) {
			int index = name.lastIndexOf(".");
			return name.substring(index+1).toLowerCase();
		}
		return "";
	}
	
    private static String addZeroWidthSpace(String s) {
    	final int mid = s.length() / 2; //get the middle of the String
	String[] parts = {s.substring(0, mid),s.substring(mid)};
	return parts[0] + "\u200B" + parts[1];
        //return s.replaceAll(".(?=.)", "$0" + "\u200B");
    }
	
	public static String antiPing(String nick) {
		return addZeroWidthSpace(nick);
	}
	
	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e) {e.printStackTrace();}
	}
	
	@SuppressWarnings("rawtypes")
	public static Boolean isChannelOp(MessageEvent event) {
		if (event.getChannel().isOp(event.getUser()) || event.getChannel().isOwner(event.getUser()) || event.getChannel().isSuperOp(event.getUser())) {
			return true;
		} else {
			return false;
		}
		
	}

	/**
	 * Returns a random number between min (inclusive) and max (exclusive)
	 * http://stackoverflow.com/questions/1527803/generating-random-whole-numbers-in-javascript-in-a-specific-range#1527820
	 */
	public static double getRandomArbitrary(Integer min, Integer max) {
		return Math.random() * (max - min) + min;
	}

	/**
	 * Returns a random integer between min (inclusive) and max (inclusive)
	 * Using Math.round() will give you a non-uniform distribution!
	 * http://stackoverflow.com/questions/1527803/generating-random-whole-numbers-in-javascript-in-a-specific-range#1527820
	 */
	public static Integer getRandomInt(Integer min, Integer max) {
		return (int) (Math.floor(Math.random() * (max - min + 1)) + min);
	}
	
		private static int textWidth(String str) {
		String NON_THIN = "[^iIl1\\.,']";
		return (int) (str.length() - str.replaceAll(NON_THIN, "").length() / 2);
	}

	public static String ellipsize(String text, int max) {

		if (textWidth(text) <= max)
			return text;

		// Start by chopping off at the word before max
		// This is an over-approximation due to thin-characters...
		int end = text.lastIndexOf(' ', max - 1);

		// Just one long word. Chop it off.
		if (end == -1)
			return text.substring(0, max-1) + "…";

		// Step forward as long as textWidth allows.
		int newEnd = end;
		do {
			end = newEnd;
			newEnd = text.indexOf(' ', end + 1);

			// No more spaces.
			if (newEnd == -1)
				newEnd = text.length();

		} while (textWidth(text.substring(0, newEnd) + "…") < max);

		return text.substring(0, end) + "…";
	}

	String readFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(" ");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}
	public static Boolean toggleCommand(String command, String channel, String action) {
		if (action.equals("enable")) {
			try {
				//enabledChannels.add(chan);
				PreparedStatement enableHook = IRCBot.getInstance().getPreparedStatement("enableHook");
				enableHook.setString(1, command);
				enableHook.setString(2, channel);
				enableHook.executeUpdate();
				IRCBot.getInstance().sendMessage(channel, "Enabled " + command);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		} else if (action.equals("disable") ) {
			try {
				//enabledChannels.remove(chan);
				PreparedStatement disableHook = IRCBot.getInstance().getPreparedStatement("disableHook");
				disableHook.setString(1, command);
				disableHook.setString(2, channel);
				disableHook.executeUpdate();
				IRCBot.getInstance().sendMessage(channel, "Disabled " + command);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}
}
