package pcl.lc.utils;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericChannelUserEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.Database;
import pcl.lc.irc.DiceRoll;
import pcl.lc.irc.IRCBot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public static String antiPing(String meh, String nick) {
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

	public static String rollDiceString(String dice) {
		DiceRoll roll =  rollDice(dice);
		if (roll != null)
			return roll.getResultString();
		else
			return "Invalid dice format (Eg 1d6)";
	}

	public static DiceRoll rollDice(String dice) {
		final String regex = "(\\d\\d?\\d?)d(\\d\\d?\\d?)";

		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(dice);

		if (matcher.matches()) {
			Integer num_dice = Math.min(100, Integer.valueOf(matcher.group(1)));
			Integer dice_size = Integer.valueOf(matcher.group(2));

			Integer sum = 0;
			ArrayList<Integer> results = new ArrayList<>(100);
			for (Integer i = 0; i < num_dice; i++)
			{
				Integer steps = Helper.getRandomInt(1, 12);
				Integer gone = 0;
				Integer result = 1;
				for (result = 1; gone < steps; gone++)
				{
					if (Objects.equals(result, dice_size))
						result = 0;
					result++;
				}
				results.add(result);
				sum += result;
			}
			return new DiceRoll(results, sum);
		}
		else {
			return null;
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
				PreparedStatement enableHook = Database.getPreparedStatement("enableHook");
				enableHook.setString(1, command);
				enableHook.setString(2, channel);
				enableHook.executeUpdate();
				Helper.sendMessage(channel, "Enabled " + command);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		} else if (action.equals("disable") ) {
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
		return(millis + epoch);
	}

	public static void sendMessage(String target, String message) {
		sendMessage(target, message, null);
	}

	public static void sendMessage(String target, String message, String targetUser, boolean overridePaste){
		sendMessage(target, message, targetUser, PasteUtils.Formats.NONE, overridePaste);
	}

	public static void sendMessage(String target, String message, String targetUser, Enum format){
		sendMessage(target, message, targetUser, format, false);
	}

	public static void sendMessage(String target, String message, String targetUser){
		sendMessage(target, message, targetUser, PasteUtils.Formats.NONE, false);
	}
	
	public static void sendMessage(String target, String message, String targetUser, Enum format, boolean overridePaste){
		if (targetUser != null)
			targetUser = Helper.antiPing(targetUser) + ": ";
		else
			targetUser = "";
		if (message.length() > 200 && !overridePaste) {
			String pasteURL = PasteUtils.paste(message, format);
			IRCBot.bot.sendIRC().message(target, targetUser + "Message too long to send to channel " + pasteURL);
			IRCBot.log.info("--> " +  target + " " + targetUser.replaceAll("\\p{C}", "") + " Message too long to send to channel " + pasteURL);
		} else {
			IRCBot.bot.sendIRC().message(target, targetUser + message);
			List<String> list = new ArrayList<String>();
			list.add(target);
			list.add(targetUser.replaceAll("\\p{C}", ""));
			list.add(message);
			IRCBot.messages.put(UUID.randomUUID(), list);
			IRCBot.log.info("--> " + target + " " + targetUser.replaceAll("\\p{C}", "") + " " + message);
		}
	}

	public static void sendAction(String target, String message) {
		IRCBot.bot.sendIRC().message(target, "\u0001ACTION " + message + "\u0001");
		IRCBot.log.info("--> " + " " + target.replaceAll("\\p{C}", "") + " " + message);
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
	}
	
	public static TimeObject parseMilliseconds(long milliseconds) {
		return parseSeconds(milliseconds / 1000);
	}

	public static TimeObject parseSeconds(long seconds)
	{
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

	public static String timeString(TimeObject timeObject, boolean short_labels, int display_highest_x, boolean discard_seconds_unless_only_value)
	{
		String time_string = "";
		ArrayList<String> strings = new ArrayList<>();

		if (short_labels)
		{
			if (timeObject.years > 0)   strings.add(timeObject.years + "y, ");
			if (timeObject.months > 0)  strings.add(timeObject.months + "m, ");
			if (timeObject.weeks > 0)   strings.add(timeObject.weeks + "w, ");
			if (timeObject.days > 0)    strings.add(timeObject.days + "d, ");
			if (timeObject.hours > 0)   strings.add(timeObject.hours + "h, ");
			if (timeObject.minutes > 0) strings.add(timeObject.minutes + "m, ");
			if (!discard_seconds_unless_only_value || (timeObject.minutes == 0 && timeObject.hours == 0 && timeObject.days == 0 && timeObject.weeks == 0 && timeObject.months == 0 && timeObject.years == 0))
				if (timeObject.seconds > 0) strings.add(timeObject.seconds + "s, ");
		}
		else
		{
			if (timeObject.years > 0)   strings.add((timeObject.years == 1)    ? timeObject.years + " year, "      : timeObject.years + " years, ");
			if (timeObject.months > 0)  strings.add((timeObject.months == 1)   ? timeObject.months + " month, "    : timeObject.months + " months, ");
			if (timeObject.weeks > 0)   strings.add((timeObject.weeks == 1)    ? timeObject.weeks + " week, "      : timeObject.weeks + " weeks, ");
			if (timeObject.days > 0)    strings.add((timeObject.days == 1)     ? timeObject.days + " day, "        : timeObject.days + " days, ");
			if (timeObject.hours > 0)   strings.add((timeObject.hours == 1)    ? timeObject.hours + " hour, "      : timeObject.hours + " hours, ");
			if (timeObject.minutes > 0) strings.add((timeObject.minutes == 1)  ? timeObject.minutes + " minute, "  : timeObject.minutes + " minutes, ");
			if (!discard_seconds_unless_only_value || (timeObject.minutes == 0 && timeObject.hours == 0 && timeObject.days == 0 && timeObject.weeks == 0 && timeObject.months == 0 && timeObject.years == 0))
				if (timeObject.seconds > 0) strings.add((timeObject.seconds == 1)  ? timeObject.seconds + " second, "  : timeObject.seconds + " seconds, ");
		}

		int counter = 0;
		for (String string : strings) {
			if (counter >= display_highest_x)
				break;
			time_string += string;
			counter++;
		}

		time_string = time_string.replaceAll(", $", "");
		if (time_string.length() == 0)
			time_string = "<0";

		return time_string;
	}

	public static String get_fail_response() {
		ArrayList<String> strings = new ArrayList<>();
		strings.add("Oops...");
		strings.add("ohno");
		strings.add("Not again...");
		strings.add("Dammit!");
		strings.add("#@%&!!");
		strings.add("Fore!");
		strings.add("I hope nobody saw that...");
		strings.add("I didn't do it!");
		return strings.get(getRandomInt(0, strings.size() - 1));
	}

	public static String get_success_response() {
		ArrayList<String> strings = new ArrayList<>();
		strings.add("Yes!");
		strings.add("I did it!");
		strings.add("Woo!");
		strings.add("I'm awesome!");
		strings.add("Take that RNG!");
		strings.add("In yo face!");
		strings.add("Exactly as planned.");
		return strings.get(getRandomInt(0, strings.size() - 1));
	}

	public static String get_surprise_response() {
		ArrayList<String> strings = new ArrayList<>();
		strings.add("What? D:");
		strings.add("Nooooo");
		strings.add("Whatever.");
		strings.add("Nuuh");
		strings.add("I'll stab you in the face!");
		strings.add("How dare you?!");
		strings.add("Fight me!");
		strings.add("No u!");
		strings.add("Someone's mad.");
		return strings.get(getRandomInt(0, strings.size() - 1));
	}

	public static String get_thanks_response() {
		ArrayList<String> strings = new ArrayList<>();
		strings.add("Thanks!");
		strings.add("You're too kind!");
		return strings.get(getRandomInt(0, strings.size() - 1));
	}

	public static String parseSelfReferral(String type) {
		switch (type) {
			case "his": //Takes a swing with his axe
				return "her"; //Takes a swing with her axe
			case "he": //He launches the missile
				return "she"; //She launches the missile
			case "him": //The boot hits him in the face
				return "her"; //The boot hits her in the face
			case "himself": //The bot blames himself for ruining christmas
				return "herself"; //The bot blames herself for ruining christmas
			default:
				return type;
		}
	}

	public static boolean doInterractWith(String target) {
		if (target.toLowerCase().contains(IRCBot.getOurNick().toLowerCase()))
			return false;
		switch (target.toLowerCase()) {
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
}
