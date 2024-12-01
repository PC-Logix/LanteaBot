package pcl.lc.utils;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.UnmodifiableIterator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.pircbotx.Channel;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericChannelUserEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.DiceRollGroup;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {
	static HashMap<String, HashMap<String, String>> genderStrings = new HashMap<String, HashMap<String, String>>();

	public static void init() {
		//<editor-fold desc="Init Pronoun Maps">
		HashMap<String, String> female = new HashMap<String, String>();
		HashMap<String, String> male = new HashMap<String, String>();
		HashMap<String, String> genderless = new HashMap<String, String>();
		female.put("his", "her");
		female.put("he", "she");
		female.put("him", "her");
		female.put("himself", "herself");
		female.put("he's", "she's");
		genderStrings.put("female", female);
		male.put("his", "his");
		male.put("he", "he");
		male.put("him", "him");
		male.put("himself", "himself");
		male.put("he's", "he's");
		genderStrings.put("male", male);
		genderless.put("his", "their");
		genderless.put("he", "they");
		genderless.put("him", "them");
		genderless.put("himself", "themself");
		genderless.put("he's", "they've");
		genderStrings.put("genderless", genderless);
		//</editor-fold>

		TablesOfRandomThings.initRandomTables();

		String restartFlag = "false";
		try {
			restartFlag = Database.getJsonData("restartFlag");
		} catch (SQLException e) {}
		if (restartFlag == "true") {
			Database.storeJsonData("restartFlag", "false");
			Helper.sendMessageAllChannels("Restart complete!");
		}
	}

	public static final Charset utf8 = Charset.forName("UTF-8");
	public static ImmutableSortedSet<String> AntiPings;

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

	@SuppressWarnings("unchecked")
	public static <T, S extends T> ArrayList<S> getAllOfType(ArrayList<T> list, Class<S> type) {
		ArrayList<S> newList = new ArrayList<S>();
		for (T object : list) if (type.isInstance(object)) newList.add((S) object);
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
			return name.substring(index + 1).toLowerCase();
		}
		return "";
	}

	private static String addZeroWidthSpace(String s) {
		final int mid = s.length() / 2; //get the middle of the String
		String[] parts = {s.substring(0, mid), s.substring(mid)};
		return parts[0] + "\u200B" + parts[1];
		//return s.replaceAll(".(?=.)", "$0" + "\u200B");
	}

	public static String antiPing(String nick) {
		return addZeroWidthSpace(nick);
	}

	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	public static String rollDiceString(String dice) {
		DiceRollGroup roll = rollDice(dice);
		try {
			return roll.getResultString();
		} catch (Exception ex) {
			return ex.getMessage();
		}
	}

	public static DiceRollGroup rollDice(String dice) {
		try {
			return new DiceRollGroup(dice);
		} catch (Exception e) {
			e.printStackTrace();
			return new DiceRollGroup();
		}
	}

	public static String ellipsize(String text, int max) {

		if (textWidth(text) <= max)
			return text;

		// Start by chopping off at the word before max
		// This is an over-approximation due to thin-characters...
		int end = text.lastIndexOf(' ', max - 1);

		// Just one long word. Chop it off.
		if (end == -1)
			return text.substring(0, max - 1) + "…";

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
				PreparedStatement enableHook = Database.getPreparedStatement("enableHook");
				enableHook.setString(1, command);
				enableHook.setString(2, channel);
				enableHook.executeUpdate();
				Helper.sendMessage(channel, "Enabled " + command);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		} else if (action.equals("disable")) {
			try {
				//enabledChannels.remove(chan);
				PreparedStatement disableHook = Database.getPreparedStatement("disableHook");
				disableHook.setString(1, command);
				disableHook.setString(2, channel);
				disableHook.executeUpdate();
				Helper.sendMessage(channel, "Disabled " + command);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}

	/*
	 * Checks if a hook is enabled in a channel
	 * args String channel String hook
	 */
	public static boolean isEnabledHere(String chan, String hook) {
		try {
			PreparedStatement checkHook = Database.getPreparedStatement("checkHookForChan");
			checkHook.setString(1, hook);
			checkHook.setString(2, chan);
			ResultSet results = checkHook.executeQuery();
			if (results.next()) {
				results.close();
				return true;
			}
			results.close();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static long getFutureTime(String time) {
		PeriodFormatter formatter = new PeriodFormatterBuilder()
				.appendWeeks().appendSuffix("w")
				.appendDays().appendSuffix("d")
				.appendHours().appendSuffix("h")
				.appendMinutes().appendSuffix("m")
				.appendSeconds().appendSuffix("s")
				.toFormatter();

		Period p = formatter.parsePeriod(time);
		long millis = p.toStandardDuration().getMillis();
		//long millis = p.normalizedStandard(p.getPeriodType()).getMillis();
		long epoch = System.currentTimeMillis();
		return (millis + epoch);
	}

	public static void sendMessageRaw(String target, String message) {
		List<String> list = new ArrayList<String>();
		list.add(target);
		list.add(IRCBot.getOurNick());
		list.add(message);
		IRCBot.messages.put(UUID.randomUUID(), list);
		IRCBot.bot.sendIRC().message(target, message);
		IRCBot.log.info("--> " + target + " " + IRCBot.getOurNick() + " " + message);
	}

	public static void sendActionRaw(String target, String message) {
		List<String> list = new ArrayList<String>();
		list.add(target);
		list.add(IRCBot.getOurNick());
		list.add("* " + message);
		IRCBot.messages.put(UUID.randomUUID(), list);
		IRCBot.bot.sendIRC().message(target, "\u0001ACTION " + message + "\u0001");
		IRCBot.log.info("--> " + " " + target + " " + message);
	}

	public static void sendMessage(String target, String message) {
		sendMessage(target.trim(), message, null);
	}

	public static void sendMessage(String target, String message, String targetUser, boolean overridePaste) {
		sendMessage(target.trim(), message, targetUser, PasteUtils.Formats.NONE, overridePaste);
	}

	public static void sendMessage(String target, String message, String targetUser, Enum format) {
		sendMessage(target.trim(), message, targetUser, format, false);
	}

	public static void sendMessage(String target, String message, String targetUser) {
		sendMessage(target.trim(), message, targetUser, PasteUtils.Formats.NONE, false);
	}

	public static boolean isValidURL(String urlString) {
		try {
			URL url = new URL(urlString);
			url.toURI();
			return true;
		} catch (Exception exception) {
			return false;
		}
	}


	public static void sendMessage(String target, String message, String targetUser, Enum format, boolean overridePaste) {
		if (AntiPings != null && !AntiPings.isEmpty()) {
			String[] parts = message.split(" ");
			for (String part : parts) {
				if (stringContainsItemFromList(part, AntiPings)) {
					if (!isValidURL(part)) {
						message = message.replaceAll("(?i)\\Q" + part.replaceAll("\\\\E", "\\E").replaceAll("\\\\Q", "\\Q") + "\\E", antiPing(part));
					}
				}
			}
		}

		if (targetUser != null)
			targetUser = Helper.antiPing(targetUser) + ": ";
		else
			targetUser = "";
		int messageLimit = 320; // Max message length before it's returned as a paste or split
		message = StringUtils.strip(message);
		if (message.length() > messageLimit && !overridePaste) {
			String pasteURL = PasteUtils.paste(message, format);
			IRCBot.bot.sendIRC().message(target, targetUser + "Message too long to send to channel " + pasteURL);
			IRCBot.log.info("--> " + target + " " + targetUser.replaceAll("\\p{C}", "") + " Message too long to send to channel " + pasteURL);
		} else {
			if (message.length() > messageLimit) {
				List<String> messages = splitString(message, messageLimit);
				for (String temp : messages) {
					IRCBot.bot.sendIRC().message(target, temp);
				}
			} else {
				IRCBot.bot.sendIRC().message(target, targetUser + message);
			}
			List<String> list = new ArrayList<String>();
			list.add(target);
			list.add(targetUser.replace(": ", "").replaceAll("\\p{C}", "") != "" ? targetUser.replace(": ", "").replaceAll("\\p{C}", "") : IRCBot.getOurNick());
			list.add(message);
			IRCBot.messages.put(UUID.randomUUID(), list);
			IRCBot.log.info("--> " + target + " " + targetUser.replaceAll("\\p{C}", "") + " " + message);
		}
		Helper.AntiPings = null;
	}

	public static void sendMessageAllChannels(String message) {
		@SuppressWarnings("SqlResolve") ResultSet readChannels = Database.getConnection().createStatement().executeQuery("SELECT name FROM channels;");
		int rowCount = 0;
		while (readChannels.next()) {
			rowCount++;
			sendMessage(readChannels.getString("name"), message);
		}
	}

	public static List<String> splitString(String msg, int lineSize) {
		List<String> res = new ArrayList<String>();

		Pattern p = Pattern.compile("\\b.{1," + (lineSize - 1) + "}\\b\\W?");
		Matcher m = p.matcher(msg);

		while (m.find()) {
//			System.out.println(m.group().trim());   // Debug
			res.add(m.group());
		}
		return res;
	}

	public static boolean targetIsChannel(String target) {
		if (target.contains("#"))
			return true;
		return false;
	}

	public static ImmutableSortedSet<String> getNamesFromTarget(String target) {
		try {
			Channel channel = IRCBot.bot.getUserChannelDao().getChannel(target);
			channel.createSnapshot();
			return channel.getUsersNicks();
		} catch (Exception e) {
			//error handling code
		}
		return AntiPings;
	}

	public static void sendAction(String target, String message) {
		if (AntiPings != null && !AntiPings.isEmpty()) {
			String[] parts = message.split(" ");
			for (String part : parts) {
				if (stringContainsItemFromList(part, AntiPings)) {
					if (!isValidURL(part)) {
						message = message.replaceAll("(?i)" + part, antiPing(part));
					}
				}
			}
		}
		IRCBot.bot.sendIRC().message(target, "\u0001ACTION " + message + "\u0001");
		IRCBot.log.info("--> " + " " + target.replaceAll("\\p{C}", "") + " " + message);
		Helper.AntiPings = null;
	}

	public static void sendWorldAction(String target, String message) {
		if (AntiPings != null && !AntiPings.isEmpty()) {
			String[] parts = message.split(" ");
			for (String part : parts) {
				if (stringContainsItemFromList(part, AntiPings)) {
					if (!isValidURL(part)) {
						message = message.replaceAll("(?i)" + part, antiPing(part));
					}
				}
			}
		}
		IRCBot.bot.sendIRC().message(target, "[\u001D " + message + " \u001D]");
		IRCBot.log.info("--> " + " " + target.replaceAll("\\p{C}", "") + " [ " + message + " ]");
		Helper.AntiPings = null;
	}

	public static void sendNotice(String target, String notice) {
		sendNotice(target, notice, null);
	}

	public static void sendNotice(String target, String notice, String callingRelay) {
		if (callingRelay != null && !callingRelay.equals(""))
			sendMessage(callingRelay, target + ": " + notice);
		else
			IRCBot.bot.sendIRC().notice(target, notice);
	}

	public static Boolean stringContainsItemFromList(String inputStr, ImmutableSortedSet<String> items) {
		UnmodifiableIterator<String> itr = items.iterator();
		String out = "";
		while (itr.hasNext()) {
			String match = itr.next();
			if (isContain(inputStr, match)) {
				//out += match + " ";
				return true;
			}
		}
		return false;
	}

	private static boolean isContain(String source, String subItem) {
		String pattern = "\\b" + Pattern.quote(subItem) + "\\b";
		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(source);
		return m.find();
	}

	public static class TimeObject {
		private long years;
		private long months;
		private long weeks;
		private long days;
		private long hours;
		private long minutes;
		private long seconds;
		private long input;

		public TimeObject(long years, long months, long weeks, long days, long hours, long minutes, long seconds, long input) {
			this.years = years;
			this.months = months;
			this.weeks = weeks;
			this.days = days;
			this.hours = hours;
			this.minutes = minutes;
			this.seconds = seconds;
			this.input = input;
		}

		public TimeObject() {}

		public long getYears() {
			return years;
		}

		public long getMonths() {
			return months;
		}

		public long getWeeks() {
			return weeks;
		}

		public long getDays() {
			return days;
		}

		public long getHours() {
			return hours;
		}

		public long getMinutes() {
			return minutes;
		}

		public long getSeconds() {
			return seconds;
		}

		public long getInput() {
			return input;
		}

		@Override
		public String toString() {
			return timeString(this);
		}
	}

	public static TimeObject parseMilliseconds(long milliseconds) {
		return parseSeconds(milliseconds / 1000);
	}

	public static TimeObject parseSeconds(long seconds) {
		long input = seconds;
		if (seconds < 0)
			seconds = seconds * -1;

		long years = (long) Math.floor(seconds / 60 / 60 / 24 / 30 / 12);

		seconds = seconds - (years * 12 * 30 * 24 * 60 * 60);

		long months = (long) Math.floor(seconds / 60 / 60 / 24 / 30);

		seconds = seconds - (months * 30 * 24 * 60 * 60);

		long weeks = (long) Math.floor(seconds / 60 / 60 / 24 / 7);

		seconds = seconds - (weeks * 7 * 24 * 60 * 60);

		long days = (long) Math.floor(seconds / 60 / 60 / 24);

		seconds = seconds - (days * 24 * 60 * 60);

		long hours = (long) Math.floor(seconds / 60 / 60);

		seconds = seconds - (hours * 60 * 60);

		long minutes = (long) Math.floor(seconds / 60);

		seconds = seconds - (minutes * 60);

		return new TimeObject(years, months, weeks, days, hours, minutes, (long) Math.floor(seconds), input);
	}

	public static String timeString(TimeObject timeObject, boolean discard_seconds_unless_only_value) {
		return timeString(timeObject, false, 6, discard_seconds_unless_only_value);
	}

	public static String timeString(TimeObject timeObject) {
		return timeString(timeObject, false, 6, false);
	}

	public static String timeString(TimeObject timeObject, boolean short_labels, int display_highest_x, boolean discard_seconds_unless_only_value) {
		String time_string = "";
		ArrayList<String> strings = new ArrayList<>();

		if (short_labels) {
			if (timeObject.years > 0) strings.add(timeObject.years + "y, ");
			if (timeObject.months > 0) strings.add(timeObject.months + "m, ");
			if (timeObject.weeks > 0) strings.add(timeObject.weeks + "w, ");
			if (timeObject.days > 0) strings.add(timeObject.days + "d, ");
			if (timeObject.hours > 0) strings.add(timeObject.hours + "h, ");
			if (timeObject.minutes > 0) strings.add(timeObject.minutes + "m, ");
			if (!discard_seconds_unless_only_value || (timeObject.minutes == 0 && timeObject.hours == 0 && timeObject.days == 0 && timeObject.weeks == 0 && timeObject.months == 0 && timeObject.years == 0))
				if (timeObject.seconds > 0) strings.add(timeObject.seconds + "s, ");
		} else {
			if (timeObject.years > 0)
				strings.add((timeObject.years == 1) ? timeObject.years + " year, " : timeObject.years + " years, ");
			if (timeObject.months > 0)
				strings.add((timeObject.months == 1) ? timeObject.months + " month, " : timeObject.months + " months, ");
			if (timeObject.weeks > 0)
				strings.add((timeObject.weeks == 1) ? timeObject.weeks + " week, " : timeObject.weeks + " weeks, ");
			if (timeObject.days > 0)
				strings.add((timeObject.days == 1) ? timeObject.days + " day, " : timeObject.days + " days, ");
			if (timeObject.hours > 0)
				strings.add((timeObject.hours == 1) ? timeObject.hours + " hour, " : timeObject.hours + " hours, ");
			if (timeObject.minutes > 0)
				strings.add((timeObject.minutes == 1) ? timeObject.minutes + " minute, " : timeObject.minutes + " minutes, ");
			if (!discard_seconds_unless_only_value || (timeObject.minutes == 0 && timeObject.hours == 0 && timeObject.days == 0 && timeObject.weeks == 0 && timeObject.months == 0 && timeObject.years == 0))
				if (timeObject.seconds > 0)
					strings.add((timeObject.seconds == 1) ? timeObject.seconds + " second, " : timeObject.seconds + " seconds, ");
		}

		int counter = 0;
		for (String string : strings) {
			if (counter >= display_highest_x)
				break;
			time_string += string;
			counter++;
		}

		String substitute = "$1 and $2";
		time_string = time_string.replaceAll(", $", "");
		Pattern pattern = Pattern.compile("(.*), (.*)$");
		Matcher matcher = pattern.matcher(time_string);
		time_string = matcher.replaceFirst(substitute);
		if (time_string.length() == 0)
			time_string = "<0";

		return time_string;
	}

	public static String parseSelfReferral(String type) {
		String gender = Config.botGender;
		HashMap<String, String> genderRef = genderStrings.get(gender);
		return genderRef.get(type);
	}

	public static boolean doInteractWith(String target) {
		IRCBot.log.info(target.toLowerCase());
		if (target.toLowerCase().contains(IRCBot.getOurNick().toLowerCase()))
			return false;
		switch (target.toLowerCase()) {
			case "me":
			case "herself":
			case "himself":
			case "itself":
			case "themself":
			case "themselves":
			case "themselfs":
				return false;
			default:
				return true;
		}
	}

	public static String getTarget(GenericMessageEvent event) {
		if (event instanceof GenericChannelUserEvent && ((GenericChannelUserEvent) event).getChannel() != null)
			return ((GenericChannelUserEvent) event).getChannel().getName();
		return event.getUser().getNick();
	}

	public static String getRandomUser(GenericMessageEvent event) {
		return getRandomUser(event, new ArrayList<>());
	}

	public static String getRandomUser(GenericMessageEvent event, ArrayList<String> blacklist) {
		if (event instanceof GenericChannelUserEvent && ((GenericChannelUserEvent) event).getChannel() != null) {
			Channel channel = ((GenericChannelUserEvent) event).getChannel();
			int size = channel.getUsersNicks().size();
			int item = new Random().nextInt(size); // In real life, the Random object should be rather more shared than this
			int i = 0;
			for (String obj : channel.getUsersNicks()) {
				if (i >= item && !blacklist.contains(obj))
					return obj;
				i++;
			}
		}
		return event.getUser().getNick();
	}

	public static Matcher getMatcherFromPattern(String pattern, String input) {
		String regex = "^(" + pattern + ") (.*)";
		Pattern pt = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
//		System.out.println("Trying '" + regex + "' on '" + input + "'");
		return pt.matcher(input);
	}

	public static String[] solvePrefixes(String input) {
		String regex;
		String match;
		Pattern pattern;
		Matcher matcher;

		String[] counters_part_a = {
				"a", "an", "the", "a whole lot of", "many", "a lot of", "a number of"
		};
		String[] counters_part_twenty = {
				"twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
		};
		String[] counters_part_one = {
				"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"
		};
		String[] counters_part_hundred = {
				"hundred", "thousand", "million", "milliard", "billion", "billiard", "trillion", "quadrillion", "quintillion", "sextillion", "septillion", "octillion", "nonillion", "decillion", "undecillion", "duodecillion", "tredecillion", "quattuordecillion", "quindecillion", "sexdecillion", "septendecillion", "octodecillion", "novemdecillion", "vigintillion", "centillion"
		};

		for (String prefix : counters_part_a) {
			matcher = getMatcherFromPattern(prefix, input);
			if (matcher.matches()) {
				for (String prefixa : counters_part_hundred) {
					Matcher a = getMatcherFromPattern(prefix + " " + prefixa, input);
					if (a.matches())
						return new String[]{a.group(1), a.group(2)};
				}
				return new String[]{matcher.group(1), matcher.group(2)};
			}
		}

		for (String prefix : counters_part_one) {
			matcher = getMatcherFromPattern(prefix, input);
			if (matcher.matches()) {
				for (String prefixa : counters_part_hundred) {
					Matcher a = getMatcherFromPattern(prefix + " " + prefixa, input);
					if (a.matches())
						return new String[]{a.group(1), a.group(2)};
				}
				return new String[]{matcher.group(1), matcher.group(2)};
			}
		}

		for (String prefix : counters_part_twenty) {
			matcher = getMatcherFromPattern(prefix, input);
			if (matcher.matches()) {
				for (String prefixa : counters_part_one) {
					Matcher a = getMatcherFromPattern(prefix + " " + prefixa, input);
					if (a.matches()) {
						for (String prefixb : counters_part_hundred) {
							Matcher b = getMatcherFromPattern(prefix + " " + prefixa + " " + prefixb, input);
							if (b.matches())
								return new String[]{b.group(1), b.group(2)};
						}
						return new String[]{a.group(1), a.group(2)};
					}
				}

				for (String prefixa : counters_part_hundred) {
					Matcher a = getMatcherFromPattern(prefix + " " + prefixa, input);
					if (a.matches())
						return new String[]{a.group(1), a.group(2)};
				}
				return new String[]{matcher.group(1), matcher.group(2)};
			}
		}

		return null;
	}

	public static double round(double value, int places) {
		double scale = Math.pow(10, places);
		return Math.round(value * scale) / scale;
	}

	public static String replaceSubstring(String string, String replace, int startIndex, int endIndex) {
		return string.substring(0, startIndex) + replace + string.substring(endIndex);
	}

	public static String reverseString(String input) {
		ArrayList<String> newString = new ArrayList<>();
		for (String s : input.split(" ")) {
			newString.add(0, s);
		}
		return String.join(" ", newString);
	}

	public static ArrayList<String> covertIntegerListToStringList(ArrayList<Integer> input) {
		ArrayList<String> converted = new ArrayList<>();
		for (Integer in : input) {
			converted.add(String.valueOf(in));
		}
		return converted;
	}

	public static String getNumberPrefix(int number) {
		return getNumberPrefix(String.valueOf(number));
	}

	public static String getNumberPrefix(double number) {
		return getNumberPrefix(Double.valueOf(number));
	}

	public static String getNumberPrefix(float number) {
		return getNumberPrefix(Float.valueOf(number));
	}

	public static String getNumberPrefix(long number) {
		return getNumberPrefix(Long.valueOf(number));
	}

	public static String getNumberPrefix(short number) {
		return getNumberPrefix(Short.valueOf(number));
	}

	public static String getNumberPrefix(String number) {
		switch (number) {
			case "8":
			case "11":
			case "18":
				return "an";
			default:
				return "a";
		}
	}

	public static String oxfordJoin(ArrayList<? extends Object> input, String delimiter, String finalDelimiter) {
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < input.size(); i++) {
			String value = String.valueOf(input.get(i));
			if (i == 0)
				output.append(value);
			else if (i == (input.size() - 1))
				output.append(finalDelimiter).append(value);
			else
				output.append(delimiter).append(value);
		}
		return output.toString();
	}

	public static String replacePlaceholders(String input, ArrayList<String> insert) {
		return replacePlaceholders(input, insert, true);
	}

	/**
	 * Replaces a sequence of $0 placeholders with items with the corresponding index from the insert array.
	 *
	 * The placeholders can start at 0 or 1, and have one or two digits.
	 *
	 * @param input A string containing placeholders to be replaced
	 * @param insert An array of strings to be inserted
	 * @param insertJunkItems If true, inserts junk items in place of any placeholder with an index that exceeds the insert array's highest index. If false these placeholders are left in the string.
	 * @return Returns the input string with relevant placeholders replaced
	 */
	public static String replacePlaceholders(String input, ArrayList<String> insert, boolean insertJunkItems) {
		if (!input.contains("$0")) {
			ArrayList<String> dummy = new ArrayList<>();
			dummy.add("Dummy");
			dummy.addAll(insert);
			insert = dummy;
		}
		Pattern pattern = Pattern.compile("(\\$\\d\\d?)");
		Matcher matcher = pattern.matcher(input);
		while (matcher.find()) {
			String choice;
			try {
				choice = insert.get(Integer.parseInt(matcher.group(1).replace("$", "")));
			} catch (Exception e) {
				choice = TablesOfRandomThings.getRandomGarbageItem(true, true);
			}
			input = input.replace(matcher.group(1), choice);
		}
		return input;
	}

	public static String cleanNick(String nick) {
		nick = nick.replaceAll("\u200B", "");
		nick = nick.replaceAll("^@", "");
		return nick.replaceAll("\\p{C}", "");
	}

	public static String getSnippetIfUrl(String input) {
		if (input.startsWith("http://") || input.startsWith("https://")) {
			URL url = null;
			try {
				Pattern urlPattern = Pattern.compile(
						"(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
								+ "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
								+ "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
						Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
				Matcher matcher = urlPattern.matcher(input);
				if (matcher.find()) {
					System.out.println(matcher.group());
				}
				url = new URL(matcher.group());

				URLConnection con = url.openConnection();
				InputStream in = con.getInputStream();
				String encoding = con.getContentEncoding();  // ** WRONG: should use "con.getContentType()" instead but it returns something like "text/html; charset=UTF-8" so this value must be parsed to extract the actual encoding
				encoding = encoding == null ? "UTF-8" : encoding;
				String body = IOUtils.toString(in, encoding);
				System.out.println("Body:" + body);
				return body;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return input;
	}
}
