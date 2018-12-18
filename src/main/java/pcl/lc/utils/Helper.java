package pcl.lc.utils;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.UnmodifiableIterator;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.pircbotx.Channel;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericChannelUserEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.IRCBot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {
	static HashMap<String,HashMap<String, String>> genderStrings = new HashMap<String,HashMap<String, String>>();
	public static void init() {
		
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
				Integer steps = Helper.getRandomInt(1, dice_size);
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
		return(millis + epoch);
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
		IRCBot.log.info("--> " + " " + target + " " + message);	}

	public static void sendMessage(String target, String message) {
		sendMessage(target.trim(), message, null);
	}

	public static void sendMessage(String target, String message, String targetUser, boolean overridePaste){
		sendMessage(target.trim(), message, targetUser, PasteUtils.Formats.NONE, overridePaste);
	}

	public static void sendMessage(String target, String message, String targetUser, Enum format){
		sendMessage(target.trim(), message, targetUser, format, false);
	}

	public static void sendMessage(String target, String message, String targetUser){
		sendMessage(target.trim(), message, targetUser, PasteUtils.Formats.NONE, false);
	}
	
	public static void sendMessage(String target, String message, String targetUser, Enum format, boolean overridePaste){
		if (AntiPings != null && !AntiPings.isEmpty()) {
			String findMatch = stringContainsItemFromList(message, AntiPings);
			if (!findMatch.equals("false")) {
				String[] parts = findMatch.split(" ");
				for (String part : parts) {
					message = message.replace(part, antiPing(part));
				}
			}
		}
		
		if (targetUser != null)
			targetUser = Helper.antiPing(targetUser) + ": ";
		else
			targetUser = "";
		message = StringUtils.strip(message);
		if (message.length() > 200 && !overridePaste) {
			String pasteURL = PasteUtils.paste(message, format);
			IRCBot.bot.sendIRC().message(target, targetUser + "Message too long to send to channel " + pasteURL);
			IRCBot.log.info("--> " +  target + " " + targetUser.replaceAll("\\p{C}", "") + " Message too long to send to channel " + pasteURL);
		} else {
			IRCBot.bot.sendIRC().message(target, targetUser + message);
			List<String> list = new ArrayList<String>();
			list.add(target);
			list.add(targetUser.replace(": ", "").replaceAll("\\p{C}", "") != "" ? targetUser.replace(": ", "").replaceAll("\\p{C}", "") : IRCBot.getOurNick());
			list.add(message);
			IRCBot.messages.put(UUID.randomUUID(), list);
			IRCBot.log.info("--> " + target + " " + targetUser.replaceAll("\\p{C}", "") + " " + message);
		}
		Helper.AntiPings = null;
	}

	public static boolean targetIsChannel(String target) {
		if (target.contains("#"))
			return true;
		return false;
	}
	
	public static ImmutableSortedSet<String> getNamesFromTarget(String target) {
		Channel channel = IRCBot.bot.getUserChannelDao().getChannel(target);
		channel.createSnapshot();
		return channel.getUsersNicks();
	}
	
	public static void sendAction(String target, String message) {
		if (AntiPings != null && !AntiPings.isEmpty()) {
			String findMatch = Helper.stringContainsItemFromList(message, AntiPings);
			if (!findMatch.equals("false")) {
				String[] parts = findMatch.split(" ");
				for (String part : parts) {
					message = message.replace(part, antiPing(part));
				}
			}
		}
		IRCBot.bot.sendIRC().message(target, "\u0001ACTION " + message + "\u0001");
		IRCBot.log.info("--> " + " " + target.replaceAll("\\p{C}", "") + " " + message);
		Helper.AntiPings = null;
	}


	public static void sendNotice(String target, String cannotExecuteReason, String nick) {
		IRCBot.bot.sendIRC().notice(target, cannotExecuteReason);
		IRCBot.log.info("--> " + " " + target.replaceAll("\\p{C}", "") + " " + cannotExecuteReason);
	}
	
	public static String stringContainsItemFromList(String inputStr, ImmutableSortedSet<String> items)
	{
		UnmodifiableIterator<String> itr = items.iterator();
		String out = "";
		while(itr.hasNext()) {
			String match = itr.next();
			if(isContain(inputStr,match)){
				out += match + " ";
			}
		}
		if (out.equals(""))
			out = "false";
		return out;
	}
	
    private static boolean isContain(String source, String subItem){
        String pattern = "\\b"+Pattern.quote(subItem)+"\\b";
        Pattern p=Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m=p.matcher(source);
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

		String substitute = "$1 and $2";
		time_string = time_string.replaceAll(", $", "");
		Pattern pattern = Pattern.compile("(.*), (.*)$");
		Matcher matcher = pattern.matcher(time_string);
		time_string = matcher.replaceFirst(substitute);
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

	public static String get_care_response() {
		ArrayList<String> strings = new ArrayList<>();
		strings.add("No caring detected in the area");
		strings.add("The tricorder shows 0%, captain");
		strings.add("Records show zero shits given");
		strings.add("Scans indicate 0.001 units of caring, with a 0.02% margin of error");
		strings.add("Earlier instances indicate you do not.");
		strings.add("Barely even registers on the care-o-meter");
		strings.add("The needle seems to be stuck below 0");
		strings.add("Detecting trace amounts of background caring, but nothing significant");
		return strings.get(getRandomInt(0, strings.size() - 1));
	}

	public static String get_right_response() {
		ArrayList<String> strings = new ArrayList<>();
		strings.add("Meh.");
		strings.add("Sure, I guess");
		strings.add("Hm?");
		strings.add("What? No.");
		strings.add("That's fine I guess");
		strings.add("Sure, whatever");
		strings.add("Yeah right.");
		strings.add("Maybe.");
		strings.add("Okay");
		return strings.get(getRandomInt(0, strings.size() - 1));
	}

	public static String get_garbage() {
		ArrayList<String> strings = new ArrayList<>();
		strings.add("into a garbage compactor");
		strings.add("into a black hole");
		strings.add("in the Sarlacc pit");
		strings.add("in the tentacle pit");
		strings.add("in the incinerator");
		strings.add("into a portal to the moon");
		strings.add("into a stargate to deep space");
		strings.add("a forgotten box in somebodies garage");
		strings.add("into the core of a dying star");
		strings.add("into a nearby garbage can");
		strings.add("in a hole with lava in it");
		strings.add("into the void");
		return strings.get(getRandomInt(0, strings.size() - 1));
	}

	public static String get_hurt_response() {
		ArrayList<String> strings = new ArrayList<>();
		strings.add("ow");
		strings.add("ouch");
		strings.add("owies");
		strings.add("ohno D:");
		return strings.get(getRandomInt(0, strings.size() - 1));
	}

	public static String get_hit_place() {
		ArrayList<String> strings = new ArrayList<>();
		strings.add("on the arm");
		strings.add("in the head");
		strings.add("on the butt");
		strings.add("in their pride");
		strings.add("in the small of the back");
		strings.add("on the heel");
		strings.add("on the left hand");
		strings.add("underneath their foot");
		strings.add("in their spleen");
		strings.add("on a body part they didn't even know they had");
		strings.add("in the face");
		strings.add("on a small but very important bone");
		strings.add("right where they didn't expect");
		strings.add("just a bit left of where it would have killed them");
		strings.add("right where the last item hit");
		strings.add("right in their lunch");
		return strings.get(getRandomInt(0, strings.size() - 1));
	}

	public static String parseSelfReferral(String type) {
		//Temp until I add the config opt
		String gender = "female";
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
		if (event instanceof GenericChannelUserEvent && ((GenericChannelUserEvent) event).getChannel() != null) {
			Channel channel = ((GenericChannelUserEvent) event).getChannel();
			int size = channel.getUsersNicks().size();
			int item = new Random().nextInt(size); // In real life, the Random object should be rather more shared than this
			int i = 0;
			for(String obj : channel.getUsersNicks())
			{
			    if (i == item)
			        return obj;
			    i++;
			}
		}
		return event.getUser().getNick();
	}

	public static String getRandomExlamations() {
		return getRandomExlamations(false, false);
	}

	public static String getRandomExlamations(boolean allowOnes, boolean allowQuestionmarks) {
		return getRandomExlamations(getRandomInt(1, 8), 1, allowOnes, allowQuestionmarks);
	}

	public static String getRandomExlamations(int maxLength, int minLength) {
		return getRandomExlamations(maxLength, minLength, false, false);
	}

	public static String getRandomExlamations(int maxLength, int minLength, boolean allowOnes, boolean allowQuestionMarks) {
		String charss = "!";
		if (allowOnes)
			charss += "1";
		if (allowQuestionMarks)
			charss += "?";
		char[] chars = charss.toCharArray();
		String output = "";
		for (int i = minLength; i < maxLength; i++) {
			output += chars[getRandomInt(0, chars.length -1)];
		}
		return output;
	}

	public static int getOffensiveItemBonus(Item item)
	{
		return getOffensiveItemBonus(item.getName());
	}

	public static int getOffensiveItemBonus(String itemName)
	{
		int value = 0;
		itemName = itemName.toLowerCase();

		List<String> noDamage = new ArrayList<>();
		noDamage.add("discharged ");
		noDamage.add("friendly ");
		noDamage.add("baby");
		noDamage.add("fake");
		noDamage.add("artificial ");
		noDamage.add("replica ");

		for (String str : noDamage)
			if (itemName.contains(str))
				return Integer.MIN_VALUE;

		List<String> minusTwo = new ArrayList<>();
		minusTwo.add("broken ");

		for (String str : minusTwo)
			if (itemName.contains(str))
				value -= 2;


		List<String> minusOne = new ArrayList<>();
		minusOne.add("ripped ");
		minusOne.add("fragile ");
		minusOne.add("crumbling ");
		minusOne.add("dull ");
		minusOne.add("stuffed ");
		minusOne.add("soft ");
		minusOne.add("fluffy ");
		minusOne.add("ill ");
		minusOne.add("plush");
		minusOne.add("defeat ");
		minusOne.add("depress");
		minusOne.add("artificial ");
		minusOne.add("power");
		minusOne.add("damaged ");

		for (String str : minusOne)
			if (itemName.contains(str))
				value -= 1;

		List<String> plusOne = new ArrayList<>();
		plusOne.add("heavy ");
		plusOne.add("blunt ");
		plusOne.add("pointy ");
		plusOne.add("charged ");
		plusOne.add("kitten ");
		plusOne.add("poison");
		plusOne.add("double ");
		plusOne.add("bees ");
		plusOne.add("military grade ");
		plusOne.add("shark ");
		plusOne.add("bear ");
		plusOne.add("tiger ");
		plusOne.add("lion ");

		for (String str : plusOne)
			if (itemName.contains(str))
				value += 1;

		List<String> plusTwo = new ArrayList<>();
		plusTwo.add("sharp");
		plusTwo.add("weighted ");
		plusTwo.add("dangerous");
		plusTwo.add("special ");
		plusTwo.add("cat ");
		plusTwo.add("super ");
		plusTwo.add("magic");
		plusTwo.add("orbital ");
		plusTwo.add("vorpal ");
		plusTwo.add("chicken ");
		plusTwo.add("nuclear ");
		plusTwo.add("hippo");

		for (String str : plusTwo)
			if (itemName.contains(str))
				value += 2;

		return value;
	}

	public static int getDefensiveItemBonus(Item item)
	{
		return getDefensiveItemBonus(item.getName());
	}

	public static int getDefensiveItemBonus(String itemName)
	{
		int value = 0;
		itemName = itemName.toLowerCase();

		List<String> noDefense = new ArrayList<>();
		noDefense.add("paper ");
		noDefense.add("fragile ");
		noDefense.add("artificial ");
		noDefense.add("replica ");
		noDefense.add("fake");

		for (String str : noDefense)
			if (itemName.contains(str))
				return Integer.MIN_VALUE;

		List<String> minusTwo = new ArrayList<>();
		minusTwo.add("broken ");

		for (String str : minusTwo)
			if (itemName.contains(str))
				value -= 2;


		List<String> minusOne = new ArrayList<>();
		minusOne.add("ripped ");
		minusOne.add("fragile ");
		minusOne.add("crumbling ");
		minusOne.add("dull");
		minusOne.add("soft ");
		minusOne.add("ill ");
		minusOne.add("plush ");
		minusOne.add("defeat");
		minusOne.add("depress");
		minusOne.add("artificial ");
		minusOne.add("power");
		minusOne.add("damaged ");

		for (String str : minusOne)
			if (itemName.contains(str))
				value -= 1;

		List<String> plusOne = new ArrayList<>();
		plusOne.add("hard");
		plusOne.add("solid ");
		plusOne.add("rugged ");
		plusOne.add("charged ");
		plusOne.add("defensive ");
		plusOne.add("military grade ");

		for (String str : plusOne)
			if (itemName.contains(str))
				value += 1;

		List<String> plusTwo = new ArrayList<>();
		plusTwo.add("reinforced ");
		plusTwo.add("shielded ");
		plusTwo.add("robust ");
		plusTwo.add("super");
		plusTwo.add("magic");

		for (String str : plusTwo)
			if (itemName.contains(str))
				value += 2;

		return value;
	}

	public static int getHealingItemBonus(Item item) {
		return getHealingItemBonus(item.getName());
	}

	public static int getHealingItemBonus(String itemName)
	{
		int value = 0;
		itemName = itemName.toLowerCase();

		List<String> noHeal = new ArrayList<>();
		noHeal.add("plague");
		noHeal.add("ill ");
		noHeal.add("sick ");
		noHeal.add("infected ");
		noHeal.add("corrupt");
		noHeal.add("fake");
		noHeal.add("replica ");

		for (String str : noHeal)
			if (itemName.contains(str))
				return Integer.MIN_VALUE;

		List<String> minusTwo = new ArrayList<>();
		minusTwo.add("broken");

		for (String str : minusTwo)
			if (itemName.contains(str))
				value -= 2;


		List<String> minusOne = new ArrayList<>();
		minusOne.add("ripped ");
		minusOne.add("crumbling ");
		minusOne.add("ill ");
		minusOne.add("plush ");
		minusOne.add("defeat ");
		minusOne.add("depress");
		minusOne.add("artificial ");
		minusOne.add("damage");
		minusOne.add("attack ");
		minusOne.add("hurt ");
		minusOne.add("bees ");
		minusOne.add("nuclear ");

		for (String str : minusOne)
			if (itemName.contains(str))
				value -= 1;

		List<String> plusOne = new ArrayList<>();
		plusOne.add("friend");
		plusOne.add("happy ");
		plusOne.add("charged ");
		plusOne.add("healing ");
		plusOne.add("refreshing ");
		plusOne.add("sugar ");
		plusOne.add("lewd ");

		for (String str : plusOne)
			if (itemName.contains(str))
				value += 1;

		List<String> plusTwo = new ArrayList<>();
		plusTwo.add("healthy ");
		plusTwo.add("friendly ");
		plusTwo.add("magic");
		plusTwo.add("super");

		for (String str : plusTwo)
			if (itemName.contains(str))
				value += 2;

		return value;
	}

	/**
	 * Calls getRandomGarbageItem(boolean all_lower_case) with all_lower_case = false
	 * Returns a random mundane garbage item
	 * @return A random name
	 */
	public static String getRandomGarbageItem() {
		return getRandomGarbageItem(true, false);
	}

	/**
	 * Returns a random mundane garbage item
	 * @param all_lower_case Whether to return all lower case
	 * @return A random name
	 */
	public static String getRandomGarbageItem(boolean include_prefix, boolean all_lower_case) {
		String name;
		try {
			List<String[]> garbage = new ArrayList<>();
			garbage.add(new String[] {"a", "Twig"});
			garbage.add(new String[] {"a", "Pebble"});
			garbage.add(new String[] {"a", "Piece of cloth"});
			garbage.add(new String[] {"a", "Leaf"});
			garbage.add(new String[] {"a", "Weed"});
			garbage.add(new String[] {"a", "Paper crane"});
			garbage.add(new String[] {"a", "Half-eaten fortune cookie"});
			garbage.add(new String[] {"a", "Cookie with raisins"});
			garbage.add(new String[] {"a", "Turnip"});
			garbage.add(new String[] {"a", "Potato"});
			garbage.add(new String[] {"a", "Doorknob"});
			garbage.add(new String[] {"a", "Rickety Gazebo"});
			garbage.add(new String[] {"", "Half of an IKEA shelf"});
			garbage.add(new String[] {"a", "Metal bearing"});
			garbage.add(new String[] {"a", "Wooden bird"});
			garbage.add(new String[] {"", "Cheese residue"});
			garbage.add(new String[] {"a", "Slice of butter"});
			garbage.add(new String[] {"a", "Depleted 9v battery"});
			garbage.add(new String[] {"a", "Brick"});
			garbage.add(new String[] {"a", "Charred piece of bacon"});
			garbage.add(new String[] {"a", "Single grain of rice"});
			garbage.add(new String[] {"an", "Empty bottle"});
			garbage.add(new String[] {"a", "Bottle filled with concrete"});
			garbage.add(new String[] {"a", "Ball of yarn"});
			garbage.add(new String[] {"a", "Fork"});
			garbage.add(new String[] {"", "Some half-melted snow"});
			garbage.add(new String[] {"a", "Deed for a bridge"});
			garbage.add(new String[] {"an", "Unlabeled key"});
			garbage.add(new String[] {"a", "Napkin with scribbles on it"});
			garbage.add(new String[] {"a", "Butterfly"});
			garbage.add(new String[] {"a", "Phone battery"});
			garbage.add(new String[] {"a", "Set of assorted wires"});
			garbage.add(new String[] {"a", "Pen"});
			garbage.add(new String[] {"a", "Pencil"});
			garbage.add(new String[] {"an", "Eraser"});
			garbage.add(new String[] {"an", "Empty post-it note"});
			garbage.add(new String[] {"a", "Hood ornament in the shape of a tomato"});
			garbage.add(new String[] {"a", "Bottle cap"});
			garbage.add(new String[] {"an", "Empty Array"});
			garbage.add(new String[] {"", "attempt to index nil value"});
			garbage.add(new String[] {"a", "Expired lottery ticket"});
			garbage.add(new String[] {"a", "Tiny bag of catnip"});
			garbage.add(new String[] {"a", "Tiny snail"});
			garbage.add(new String[] {"", "Corn on the cob"});
			garbage.add(new String[] {"a", "Pecan pie"});
			garbage.add(new String[] {"an", "Empty drive slot"});
			garbage.add(new String[] {"a", "Dropbox account with zero capacity"});
			garbage.add(new String[] {"an", "Empty shot glass"});
			garbage.add(new String[] {"a", "Lootcrate"});
			garbage.add(new String[] {"a", "Power adapter incompatible with everything"});
			garbage.add(new String[] {"a", "Lockpick"});
			garbage.add(new String[] {"", "Two lockpicks"});
			garbage.add(new String[] {"a", "Monopoly top hat figure"});
			garbage.add(new String[] {"a", "Pretty average hat"});
			garbage.add(new String[] {"a", "Knight who says NI"});
			garbage.add(new String[] {"the", "Bottom of a barrel"});
			garbage.add(new String[] {"an", "Impossible geometric shape"});
			garbage.add(new String[] {"a", "Geode"});
			garbage.add(new String[] {"a", "Sad looking flower"});
			garbage.add(new String[] {"a", "Happy flower"});
			garbage.add(new String[] {"a", "Particularly fat bee"});
			garbage.add(new String[] {"a", "Box full of wasps"});
			garbage.add(new String[] {"a", "Box full of worms"});
			garbage.add(new String[] {"a", "Box full of thumbtacks"});
			garbage.add(new String[] {"a", "Box full of caps"});
			garbage.add(new String[] {"", "randompotion"});
			garbage.add(new String[] {"a", "Picture of a crudely drawn appendage"});
			garbage.add(new String[] {"a", "Broken .jpg"});
			garbage.add(new String[] {"a", "Broken .png"});
			garbage.add(new String[] {"a", "Broken .gif"});
			garbage.add(new String[] {"a", "Broken .tif"});
			garbage.add(new String[] {"a", "Broken .mov"});
			garbage.add(new String[] {"a", "Broken .zip"});
			garbage.add(new String[] {"a", "Broken .psd"});
			garbage.add(new String[] {"a", "Broken .7z"});
			garbage.add(new String[] {"a", "Broken .mp3"});
			garbage.add(new String[] {"a", "Broken .mp4"});
			garbage.add(new String[] {"a", "Broken .mp5"});
			garbage.add(new String[] {"a", "Broken .mp6"});
			garbage.add(new String[] {"a", "Pentagram pendant"});
			garbage.add(new String[] {"a", "Rosary"});
			garbage.add(new String[] {"an", "Upside-down cross"});
			garbage.add(new String[] {"a", "Poofy ball of fluff"});
			garbage.add(new String[] {"a", "Paperclip, big one"});
			garbage.add(new String[] {"a", "Leftover pumpkin"});
			garbage.add(new String[] {"a", "Fork in the road"});
			garbage.add(new String[] {"a", "Chocolate bar that was left out in the sun"});
			garbage.add(new String[] {"an", "Impossibly green dress"});
			garbage.add(new String[] {"a", "Piece of rope slightly too small to be useful"});
			garbage.add(new String[] {"a", "20ft Pole"});
			garbage.add(new String[] {"", "Ten birds in a bush"});
			garbage.add(new String[] {"a", "Very stale piece of pizza"});
			garbage.add(new String[] {"a", "Tiny packet of cream"});
			garbage.add(new String[] {"a", "Tiny packet of ketchup"});
			garbage.add(new String[] {"a", "Tiny packet of salt"});
			garbage.add(new String[] {"a", "Tiny packet of packets"});
			garbage.add(new String[] {"a", "Tiny packet of rubber bands"});
			garbage.add(new String[] {"a", "Tiny model shoe"});
			garbage.add(new String[] {"a", "Mermaids tear"});
			garbage.add(new String[] {"a", "Mermaid scale"});
			garbage.add(new String[] {"a", "Dragon tooth"});
			garbage.add(new String[] {"a", "Dragon scale"});
			garbage.add(new String[] {"a", "Book that is glued shut"});
			garbage.add(new String[] {"a", "Sealed unmarked canister"});
			garbage.add(new String[] {"a", "Canister of neurotoxin"});
			garbage.add(new String[] {"a", "Frog leg"});
			garbage.add(new String[] {"", "Eye of newt"});
			garbage.add(new String[] {"", "Roberto's knife"});
			garbage.add(new String[] {"an", "Unassuming lamp"});
			garbage.add(new String[] {"a", "Copy of \"The Lusty Argonian Maid\""});
			garbage.add(new String[] {"a", "Cabbage leaf"});
			garbage.add(new String[] {"an", "Ornate chandelier"});
			garbage.add(new String[] {"a", "Tiny cage"});
			garbage.add(new String[] {"a", "Tiny fork"});
			garbage.add(new String[] {"a", "Tiny spoon"});
			garbage.add(new String[] {"a", "Tiny knife"});
			garbage.add(new String[] {"an", "Ornate Nate"});
			garbage.add(new String[] {"a", "Tiny figurine"});
			garbage.add(new String[] {"a", "Mask of your face"});
			garbage.add(new String[] {"a", "Mask of someones face"});
			garbage.add(new String[] {"a", "Tiny clay figure"});
			garbage.add(new String[] {"an", "Empty soup can"});
			garbage.add(new String[] {"an", "Empty wooden chest"});
			garbage.add(new String[] {"a", "Portable stick"});
			garbage.add(new String[] {"a", "Stationary stick"});
			garbage.add(new String[] {"an", "Inanimate carbon rod"});
			garbage.add(new String[] {"a", "Living tombstone"});
			garbage.add(new String[] {"a", "Talking sword that wont stop talking"});
			garbage.add(new String[] {"a", "3D-printer that only prints in papier mache"});
			garbage.add(new String[] {"a", "Raspberry Pi that only beeps at you"});
			garbage.add(new String[] {"a", "Sphere that just wont stop talking"});
			garbage.add(new String[] {"a", "Talking fork"});
			garbage.add(new String[] {"a", "Talking spoon"});
			garbage.add(new String[] {"a", "Talking knife"});
			garbage.add(new String[] {"a", "Talking spork"});
			garbage.add(new String[] {"a", "Eerily quiet singing fish"});
			garbage.add(new String[] {"a", "Suspicious looking statue"});
			garbage.add(new String[] {"a", "Radioactive teapot"});
			garbage.add(new String[] {"a", "Miraculous Miracle Man (MMM) #1 comic"});
			garbage.add(new String[] {"the", "official laws and migration guidelines of Pluto"});
			garbage.add(new String[] {"the", "official baby talk translation guide"});
			garbage.add(new String[] {"", "Loot boxes for dummies volume 1"});
			garbage.add(new String[] {"the", "Extra-terrestrials guide to Earth fourth edition"});
			garbage.add(new String[] {"the", "Ultimate guide to killing all humans"});
			garbage.add(new String[] {"a", "Shiny metal posterior"});
			garbage.add(new String[] {"an", "Unfinished m"});
			garbage.add(new String[] {"a", "Sort-of-holy symbol"});
			garbage.add(new String[] {"a", "Guide to Talking to Rocks"});
			garbage.add(new String[] {"", "randompotion"});
			garbage.add(new String[] {"a", "triangular ball"});
			garbage.add(new String[] {"a", "pie-shaped cake"});
			garbage.add(new String[] {"an", "Inverted hole"});
			garbage.add(new String[] {"a", "Small pile of dirt"});
			garbage.add(new String[] {"a", "Jar of dirt"});
			garbage.add(new String[] {"a", "Cracked crack"});
			garbage.add(new String[] {"an", "Extremely short fork"});
			garbage.add(new String[] {"an", "Incredibly thin sheet of air"});
			garbage.add(new String[] {"a", "Poofy cloud"});
			garbage.add(new String[] {"a", "Hard cloud"});
			garbage.add(new String[] {"a", "Pointy cloud"});
			garbage.add(new String[] {"", "Another settlement that needs your help"});

			String[] item = garbage.get(getRandomInt(0, garbage.size() - 1));
			name = (include_prefix && item[0] != "" ? item[0] + " " : "") + item[1];
			if (all_lower_case)
				name = name.toLowerCase();
		} catch (Exception ex) {
			name = "[Error]";
		}
		return name;
	}

	public static String getRandomAnimal() {
		return getRandomAnimal(false);
	}

	public static String getRandomAnimal(boolean lower_case) {
		String[] animals = new String[]{
			"Pig",
			"Horse",
			"Cat",
			"Dog",
			"Fish",
			"Crocodile",
			"Bird"
		};
		String ret = animals[Helper.getRandomInt(0, animals.length - 1)];
		if (lower_case)
			return ret.toLowerCase();
		return ret;
	}

	public static Matcher getMatcherFromPattern(String pattern, String input) {
		String regex = "^(" + pattern + ") (.*)";
		Pattern pt = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		System.out.println("Trying '" + regex + "' on '" + input + "'");
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

		for (String prefix: counters_part_a) {
			matcher = getMatcherFromPattern(prefix, input);
			if (matcher.matches()) {
				for (String prefixa: counters_part_hundred) {
					Matcher a = getMatcherFromPattern(prefix + " " + prefixa, input);
					if (a.matches())
						return new String[]{a.group(1), a.group(2)};
				}
				return new String[]{matcher.group(1), matcher.group(2)};
			}
		}

		for (String prefix: counters_part_one) {
			matcher = getMatcherFromPattern(prefix, input);
			if (matcher.matches()) {
				for (String prefixa: counters_part_hundred) {
					Matcher a  = getMatcherFromPattern(prefix + " " + prefixa, input);
					if (a.matches())
						return new String[]{a.group(1), a.group(2)};
				}
				return new String[]{matcher.group(1), matcher.group(2)};
			}
		}

		for (String prefix: counters_part_twenty) {
			matcher = getMatcherFromPattern(prefix, input);
			if (matcher.matches()) {
				for (String prefixa: counters_part_one) {
					Matcher a = getMatcherFromPattern(prefix + " " + prefixa, input);
					if (a.matches()) {
						for (String prefixb: counters_part_hundred) {
							Matcher b = getMatcherFromPattern(prefix + " " + prefixa + " " + prefixb, input);
							if (b.matches())
								return new String[]{b.group(1), b.group(2)};
						}
						return new String[]{a.group(1), a.group(2)};
					}
				}

				for (String prefixa: counters_part_hundred) {
					Matcher a = getMatcherFromPattern(prefix + " " + prefixa, input);
					if (a.matches())
						return new String[]{a.group(1), a.group(2)};
				}
				return new String[]{matcher.group(1), matcher.group(2)};
			}
		}

		return null;
	}
}
