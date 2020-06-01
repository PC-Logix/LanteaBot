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

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.hooks.Defend;

import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
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
		DiceRollGroup roll =  rollDice(dice);
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
	
	public static boolean isValidURL(String urlString)
	{
	    try
	    {
	        URL url = new URL(urlString);
	        url.toURI();
	        return true;
	    } catch (Exception exception)
	    {
	        return false;
	    }
	}
	

	public static void sendMessage(String target, String message, String targetUser, Enum format, boolean overridePaste){
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
			IRCBot.log.info("--> " +  target + " " + targetUser.replaceAll("\\p{C}", "") + " Message too long to send to channel " + pasteURL);
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

    public static List<String> splitString(String msg, int lineSize) {
        List<String> res = new ArrayList<String>();

        Pattern p = Pattern.compile("\\b.{1," + (lineSize-1) + "}\\b\\W?");
        Matcher m = p.matcher(msg);

        while(m.find()) {
                System.out.println(m.group().trim());   // Debug
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
						message = message.replaceAll("(?i)"+part, antiPing(part));
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
						message = message.replaceAll("(?i)"+part, antiPing(part));
					}
				}
			}
		}
		IRCBot.bot.sendIRC().message(target, "[\u001D " + message + " \u001D]");
		IRCBot.log.info("--> " + " " + target.replaceAll("\\p{C}", "") + " [ " + message + " ]");
		Helper.AntiPings = null;
	}


	public static void sendNotice(String target, String cannotExecuteReason, String nick) {
		IRCBot.bot.sendIRC().notice(target, cannotExecuteReason);
		IRCBot.log.info("--> " + " " + target.replaceAll("\\p{C}", "") + " " + cannotExecuteReason);
	}
	
	public static Boolean stringContainsItemFromList(String inputStr, ImmutableSortedSet<String> items)
	{
		UnmodifiableIterator<String> itr = items.iterator();
		String out = "";
		while(itr.hasNext()) {
			String match = itr.next();
			if(isContain(inputStr,match)){
				//out += match + " ";
				return true;
			}
		}
		return false;
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

	private static String[] responsesFail = new String[] {
			"Oops...",
			"ohno",
			"Not again...",
			"Dammit!",
			"#@%&!!",
			"Fore!",
			"I hope nobody saw that...",
			"I didn't do it!",
	};

	public static int getFailResponseCount() {
		return responsesFail.length;
	}

	public static String getFailResponse() {
		return responsesFail[getRandomInt(0, responsesFail.length - 1)];
	}

	private static String[] responsesSuccess = new String[] {
			"Yes!",
			"I did it!",
			"Woo!",
			"I'm awesome!",
			"Take that RNG!",
			"In yo face!",
			"Exactly as planned.",
	};

	public static int getSuccessResponseCount() {
		return responsesSuccess.length;
	}

	public static String getSuccessResponse() {
		return responsesSuccess[getRandomInt(0, responsesSuccess.length - 1)];
	}

	private static String[] responsesSurprise = new String[] {
			"What? D:",
			"Nooooo",
			"Whatever.",
			"Nuuh",
			"I'll stab you in the face!",
			"How dare you?!",
			"Fight me!",
			"No u!",
			"Someone's mad.",
	};

	public static int getSurpriseResponseCount() {
		return responsesSurprise.length;
	}

	public static String getSurpriseResponse() {
		return responsesSurprise[getRandomInt(0, responsesSurprise.length - 1)];
	}

	private static String[] responsesThanks = new String[] {
		"Thanks!",
		"Wow thanks!",
	};

	public static int getThanksResponseCount() {
		return responsesThanks.length;
	}

	public static String getThanksResponse() {
		return responsesThanks[getRandomInt(0, responsesThanks.length - 1)];
	}

	private static String[] responsesAffirmative = new String[] {
			"Meh.",
			"Sure, I guess",
			"Hm?",
			"What? No.",
			"That's fine I guess",
			"Sure, whatever",
			"Yeah right.",
			"Maybe.",
			"Okay",
	};

	public static int getAffirmativeResponseCount() {
		return responsesAffirmative.length;
	}

	public static String getAffirmativeResponse() {
		return responsesAffirmative[getRandomInt(0, responsesAffirmative.length - 1)];
	}

	private static String[] careDetectorResponses = new String[] {
			"No caring detected in the area",
			"The tricorder shows 0%, captain",
			"Records show zero shits given",
			"Scans indicate 0.001 units of caring, with a 0.02% margin of error",
			"Earlier instances indicate you do not.",
			"Barely even registers on the care-o-meter",
			"The needle seems to be stuck below 0",
			"Detecting trace amounts of background caring, but nothing significant",
	};

	public static int getCareDetectorResponseCount() {
		return careDetectorResponses.length;
	}

	public static String getCareDetectorResponse() {
		return careDetectorResponses[getRandomInt(0, careDetectorResponses.length - 1)];
	}

	private static String[] garbageDisposals = new String[] {
			"into a garbage compactor",
			"into a black hole",
			"in the Sarlacc pit",
			"in the tentacle pit",
			"in the incinerator",
			"into a portal to the moon",
			"into a stargate to deep space",
			"a forgotten box in somebodies garage",
			"into the core of a dying star",
			"into a nearby garbage can",
			"in a hole with lava in it",
			"into the void"	,
	};

	public static int getGarbageDisposalCount() {
		return garbageDisposals.length;
	}

	public static String getGarbageDisposal() {
		return garbageDisposals[getRandomInt(0, garbageDisposals.length - 1)];
	}

	private static String[] responsesHurt = new String[] {
		"ow",
		"ouch",
		"owies",
		"ohno D:",
		"aaah",
		"agh",
		"ack",
		"owwwww",
	};

	public static int getHurtResponseCount() {
		return responsesHurt.length;
	}

	public static String getHurtResponse() {
		return responsesHurt[getRandomInt(0, responsesHurt.length - 1)];
	}

	private static String[] hitLocations = new String[] {
			"on the arm",
			"in the head",
			"on the butt",
			"in their pride",
			"in the small of the back",
			"on the heel",
			"on the left hand",
			"underneath their foot",
			"in their spleen",
			"on a body part they didn't even know they had",
			"in the face",
			"on a small but very important bone",
			"right where they didn't expect",
			"right where the last item hit",
			"right in their lunch",
	};

	public static int getHitPlaceCount() {
		return hitLocations.length;
	}

	public static String getHitPlace() {
		return hitLocations[getRandomInt(0, hitLocations.length - 1)];
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
			for(String obj : channel.getUsersNicks())
			{
			    if (i >= item && !blacklist.contains(obj))
			        return obj;
			    i++;
			}
		}
		return event.getUser().getNick();
	}

	public static String getRandomExclamations() {
		return getRandomExclamations(false, false);
	}

	public static String getRandomExclamations(boolean allowOnes, boolean allowQuestionmarks) {
		return getRandomExclamations(getRandomInt(1, 8), 1, allowOnes, allowQuestionmarks);
	}

	public static String getRandomExclamations(int maxLength, int minLength) {
		return getRandomExclamations(maxLength, minLength, false, false);
	}

	public static String getRandomExclamations(int maxLength, int minLength, boolean allowOnes, boolean allowQuestionMarks) {
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

	private static String[][] garbageItems = new String[][]{
			new String[] {"a", "Twig"},
			new String[] {"a", "Pebble"},
			new String[] {"a", "Piece of cloth"},
			new String[] {"a", "Leaf"},
			new String[] {"a", "Weed"},
			new String[] {"a", "Paper crane"},
			new String[] {"a", "Half-eaten fortune cookie"},
			new String[] {"a", "Cookie with raisins"},
			new String[] {"a", "Turnip"},
			new String[] {"a", "Potato"},
			new String[] {"a", "Doorknob"},
			new String[] {"a", "Rickety Gazebo"},
			new String[] {"", "Half of an IKEA shelf"},
			new String[] {"a", "Metal bearing"},
			new String[] {"a", "Wooden bird"},
			new String[] {"", "Cheese residue"},
			new String[] {"a", "Slice of butter"},
			new String[] {"a", "Depleted 9v battery"},
			new String[] {"a", "Brick"},
			new String[] {"a", "Charred piece of bacon"},
			new String[] {"a", "Single grain of rice"},
			new String[] {"an", "Empty bottle"},
			new String[] {"a", "Bottle filled with concrete"},
			new String[] {"a", "Ball of yarn"},
			new String[] {"a", "Fork"},
			new String[] {"", "Some half-melted snow"},
			new String[] {"a", "Deed for a bridge"},
			new String[] {"an", "Unlabeled key"},
			new String[] {"a", "Napkin with scribbles on it"},
			new String[] {"a", "Butterfly"},
			new String[] {"a", "Phone battery"},
			new String[] {"a", "Set of assorted wires"},
			new String[] {"a", "Pen"},
			new String[] {"a", "Pencil"},
			new String[] {"an", "Eraser"},
			new String[] {"an", "Empty post-it note"},
			new String[] {"a", "Hood ornament in the shape of a tomato"},
			new String[] {"a", "Bottle cap"},
			new String[] {"an", "Empty Array"},
			new String[] {"", "attempt to index nil value"},
			new String[] {"a", "Expired lottery ticket"},
			new String[] {"a", "Tiny bag of catnip"},
			new String[] {"a", "Tiny snail"},
			new String[] {"", "Corn on the cob"},
			new String[] {"a", "Pecan pie"},
			new String[] {"an", "Empty drive slot"},
			new String[] {"a", "Dropbox account with zero capacity"},
			new String[] {"an", "Empty shot glass"},
			new String[] {"a", "Lootcrate"},
			new String[] {"a", "Power adapter incompatible with everything"},
			new String[] {"a", "Lockpick"},
			new String[] {"", "Two lockpicks"},
			new String[] {"a", "Monopoly top hat figure"},
			new String[] {"a", "Pretty average hat"},
			new String[] {"a", "Knight who says NI"},
			new String[] {"the", "Bottom of a barrel"},
			new String[] {"an", "Impossible geometric shape"},
			new String[] {"a", "Geode"},
			new String[] {"a", "Sad looking flower"},
			new String[] {"a", "Happy flower"},
			new String[] {"a", "Particularly fat bee"},
			new String[] {"a", "Box full of wasps"},
			new String[] {"a", "Box full of worms"},
			new String[] {"a", "Box full of thumbtacks"},
			new String[] {"a", "Box full of caps"},
			new String[] {"", "randompotion"},
			new String[] {"a", "Picture of a crudely drawn appendage"},
			new String[] {"a", "Broken .jpg"},
			new String[] {"a", "Broken .png"},
			new String[] {"a", "Broken .gif"},
			new String[] {"a", "Broken .tif"},
			new String[] {"a", "Broken .mov"},
			new String[] {"a", "Broken .zip"},
			new String[] {"a", "Broken .psd"},
			new String[] {"a", "Broken .7z"},
			new String[] {"a", "Broken .mp3"},
			new String[] {"a", "Broken .mp4"},
			new String[] {"a", "Broken .mp5"},
			new String[] {"a", "Broken .mp6"},
			new String[] {"a", "Pentagram pendant"},
			new String[] {"a", "Rosary"},
			new String[] {"an", "Upside-down cross"},
			new String[] {"a", "Poofy ball of fluff"},
			new String[] {"a", "Paperclip, big one"},
			new String[] {"a", "Leftover pumpkin"},
			new String[] {"a", "Fork in the road"},
			new String[] {"a", "Chocolate bar that was left out in the sun"},
			new String[] {"an", "Impossibly green dress"},
			new String[] {"a", "Piece of rope slightly too small to be useful"},
			new String[] {"a", "20ft Pole"},
			new String[] {"", "Ten birds in a bush"},
			new String[] {"a", "Very stale piece of pizza"},
			new String[] {"a", "Tiny packet of cream"},
			new String[] {"a", "Tiny packet of ketchup"},
			new String[] {"a", "Tiny packet of salt"},
			new String[] {"a", "Tiny packet of packets"},
			new String[] {"a", "Tiny packet of rubber bands"},
			new String[] {"a", "Tiny model shoe"},
			new String[] {"a", "Mermaids tear"},
			new String[] {"a", "Mermaid scale"},
			new String[] {"a", "Dragon tooth"},
			new String[] {"a", "Dragon scale"},
			new String[] {"a", "Book that is glued shut"},
			new String[] {"a", "Sealed unmarked canister"},
			new String[] {"a", "Canister of neurotoxin"},
			new String[] {"a", "Frog leg"},
			new String[] {"", "Eye of newt"},
			new String[] {"", "Roberto's knife"},
			new String[] {"an", "Unassuming lamp"},
			new String[] {"a", "Copy of \"The Lusty Argonian Maid\""},
			new String[] {"a", "Cabbage leaf"},
			new String[] {"an", "Ornate chandelier"},
			new String[] {"a", "Tiny cage"},
			new String[] {"a", "Tiny fork"},
			new String[] {"a", "Tiny spoon"},
			new String[] {"a", "Tiny knife"},
			new String[] {"an", "Ornate Nate"},
			new String[] {"a", "Tiny figurine"},
			new String[] {"a", "Mask of your face"},
			new String[] {"a", "Mask of someones face"},
			new String[] {"a", "Tiny clay figure"},
			new String[] {"an", "Empty soup can"},
			new String[] {"an", "Empty wooden chest"},
			new String[] {"a", "Portable stick"},
			new String[] {"a", "Stationary stick"},
			new String[] {"an", "Inanimate carbon rod"},
			new String[] {"a", "Living tombstone"},
			new String[] {"a", "Talking sword that wont stop talking"},
			new String[] {"a", "3D-printer that only prints in papier mache"},
			new String[] {"a", "Raspberry Pi that only beeps at you"},
			new String[] {"a", "Sphere that just wont stop talking"},
			new String[] {"a", "Talking fork"},
			new String[] {"a", "Talking spoon"},
			new String[] {"a", "Talking knife"},
			new String[] {"a", "Talking spork"},
			new String[] {"a", "Eerily quiet singing fish"},
			new String[] {"a", "Suspicious looking statue"},
			new String[] {"a", "Radioactive teapot"},
			new String[] {"a", "Miraculous Miracle Man (MMM) #1 comic"},
			new String[] {"the", "official laws and migration guidelines of Pluto"},
			new String[] {"the", "official baby talk translation guide"},
			new String[] {"", "Loot boxes for dummies volume 1"},
			new String[] {"the", "Extra-terrestrials guide to Earth fourth edition"},
			new String[] {"the", "Ultimate guide to killing all humans"},
			new String[] {"a", "Shiny metal posterior"},
			new String[] {"an", "Unfinished m"},
			new String[] {"a", "Sort-of-holy symbol"},
			new String[] {"a", "Guide to Talking to Rocks"},
			new String[] {"", "randompotion"},
			new String[] {"a", "triangular ball"},
			new String[] {"a", "pie-shaped cake"},
			new String[] {"an", "Inverted hole"},
			new String[] {"a", "Small pile of dirt"},
			new String[] {"a", "Jar of dirt"},
			new String[] {"a", "Cracked crack"},
			new String[] {"an", "Extremely short fork"},
			new String[] {"an", "Incredibly thin sheet of air"},
			new String[] {"a", "Poofy cloud"},
			new String[] {"a", "Hard cloud"},
			new String[] {"a", "Pointy cloud"},
			new String[] {"", "Another settlement that needs your help"},
			new String[] {"a", "baseball cap with the Starbucks logo on it"},
			new String[] {"a", "baseball cap with the McDonalds logo on it"},
			new String[] {"a", "baseball cap with the IKEA logo on it"},
			new String[] {"a", "baseball cap with the Walmart logo on it"},
	};

	public static int getGarbageItemCount() {
		return garbageItems.length;
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
			int index = Helper.getRandomInt(0, garbageItems.length - 1);
			String[] item = garbageItems[index];
			name = (include_prefix && !item[0].equals("") ? item[0] + " " : "") + item[1];
			if (all_lower_case)
				name = name.toLowerCase();
		} catch (Exception ex) {
			name = "[Error]";
		}
		return name;
	}


	private static String[][] animals = new String[][]{
			//prefix, name, suffix, remove n characters from end before applying suffix
			new String[] {"A" , "Pig"      , "s"  , null},
			new String[] {"A" , "Horse"    , "s"  , null},
			new String[] {"A" , "Cat"      , "s"  , null},
			new String[] {"A" , "Dog"      , "s"  , null},
			new String[] {"A" , "Fish"     , ""   , null},
			new String[] {"A" , "Crocodile", "s"  , null},
			new String[] {"A" , "Bird"     , "s"  , null},
			new String[] {"A" , "Lizard"   , "s"  , null},
			new String[] {"A" , "Fox"      , "es" , null},
			new String[] {"A" , "Turtle"   , "s"  , null},
			new String[] {"A" , "Sloth"    , "s"  , null},
			new String[] {"A" , "Wolf"     , "ves", "1"},
			new String[] {"A" , "Robot"    , "s"  , null},
			new String[] {"A" , "Golem"    , "s"  , null},
			new String[] {"A" , "Unicorn"  , "s"  , null},
			new String[] {"A" , "Dryad"    , "s"  , null},
			new String[] {"A" , "Dragon"   , "s"  , null},
			new String[] {"A" , "Fairy"    , "ies", "1"},
			new String[] {"A*", "Spaghetti", ""   , null},
			new String[] {"A*", "Water"    , ""   , null},
			new String[] {"A*", "Lava"     , ""   , null},
			new String[] {"A" , "Shark"    , "s"  , null},
			new String[] {"An", "Otter"    , "s"  , null},
			new String[] {"A" , "Goat"     , "s"  , null},
			new String[] {"A" , "Sheep"    , ""   , null},
			new String[] {"A" , "Toad"     , "s"  , null},
			new String[] {"A" , "Sword"    , "s"  , null},
			new String[] {"A" , "Bear"     , "s"  , null},
			new String[] {"A" , "Platypus" , "i"  , "2"},
			new String[] {"A" , "Frog"     , "s"  , null},
			new String[] {"An", "Octopus" , "i"  , "3"},
			new String[] {"A" , "Unicorn"  , "s"  , null},
	};

	public static int getAnimalCount() {
		return animals.length;
	}

	public static String getTransformationByIndex(int index) {
		return getTransformationByIndex(index, false, false, false, true);
	}

	public static String getTransformationByIndex(int index, boolean lower_case, boolean prefix, boolean plural, boolean ignoreConditionalPrefixes) {
		String ret = "";
		try {
			String[] transformation = animals[index];
			if (!plural) {
				ret = (prefix && !transformation[0].equals("") && !(ignoreConditionalPrefixes && transformation[0].contains("*")) ? transformation[0].replaceAll("\\*", "") + " " : "") + transformation[1];
			} else {
				if (transformation[3] != null)
					ret = transformation[1].substring(0, transformation[1].length() - Integer.parseInt(transformation[3])) + transformation[2];
				else
					ret = transformation[1] + transformation[2];
			}
		} catch (Exception ignored) {}
		return !lower_case ? ret : ret.toLowerCase();
	}

	public static String getRandomTransformation() {
		return getRandomTransformation(false, false, false, true);
	}

	public static String getRandomTransformation(boolean lower_case, boolean prefix, boolean plural, boolean ignoreConditionalPrefixes) {
		int index = Helper.getRandomInt(0, animals.length - 1);
		return getTransformationByIndex(index, lower_case, prefix, plural, ignoreConditionalPrefixes);
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

	public static double round(double value, int places) {
		double scale = Math.pow(10, places);
		return Math.round(value * scale) / scale;
	}

	//Valid tags: {user},{appearance},{turn_appearance},{appearance:item},{consistency},{p_transformation},{transformation},{transformation2},{transformations},{transformations2}
	private static String[] warpLocations = new String[] {
			"You end up at home.",
    	"You end up in your bed.",
			"You end up in a dimension populated by {transformations}.",
			"You end up in a dimension populated by {transformation} girls.",
			"You end up in a dimension populated by {transformation} boys.",
			"You end up in a dimension populated by {transformation} {transformation2} girls.",
			"You end up in a dimension populated by {transformation} {transformation2} boys.",
			"You end up in a dimension populated by {transformation} {transformations2}.",
			"You end up in a dimension inhabited by {p_transformation}.",
			"You end up in a dimension entirely filled with {consistency} {appearance} potion.",
			"You end up in a dimension ruled by {item}.",
			"You end up in a dimension that is just an endless field of flowers.",
			"You end up in a frozen world.",
			"You end up in a dry world.",
			"You end up in a world inhabited by mimes.",
			"You end up in a world inhabited by bards.",
			"You end up in a world inhabited by clowns.",
			"You end up at the location of a great treasure. The treasure of friendship!",
	};

	public static int getWarpLocationCount() {
		return warpLocations.length;
	}

	public static String getWarpLocationByIndex(int index) {
		return getWarpLocationByIndex(index, false);
	}

	public static String getWarpLocationByIndex(int index, boolean lower_case) {
		String ret = PotionHelper.replaceParamsInEffectString(warpLocations[index], "");
		return !lower_case ? ret : ret.toLowerCase();
	}

	public static String getRandomWarpLocation() {
		return getRandomWarpLocation(false);
	}

    public static String getRandomWarpLocation(boolean lower_case) {
		int index = Helper.getRandomInt(0, warpLocations.length - 1);
		return getWarpLocationByIndex(index, lower_case);
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

	private static String[] codeWords = new String[] {
			"Blatherskite",
			"Mew",
			"Nyan",
			"Woof",
			"Ohmygawd",
			"Jeez",
			"Crystal",
			"Doom",
			"Nice",
			"Awesome",
			"Wat",
			"Yip",
			"Wenk",
			"Harmony",
			"Swing",
			"Classic",
			"Noir",
			"Supercalifragilisticexpialidocious",
			"Rather",
			"Technically",
			"Actually",
			"Sup",
			"Soup",
	};

	public static int getCodeWordCount() {
		return codeWords.length;
	}

	public static String getRandomCodeWord() {
		return codeWords[getRandomInt(0, codeWords.length - 1)];
	}

	public static String cleanDiscordNick(String nick) {
		return nick.replaceAll("\\p{C}", "");
	}

	public static String getNumberPrefix(int number) { return getNumberPrefix(String.valueOf(number)); }

	public static String getNumberPrefix(double number) { return getNumberPrefix(Double.valueOf(number)); }

	public static String getNumberPrefix(float number) { return getNumberPrefix(Float.valueOf(number)); }

	public static String getNumberPrefix(long number) { return getNumberPrefix(Long.valueOf(number)); }

	public static String getNumberPrefix(short number) { return getNumberPrefix(Short.valueOf(number)); }

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
}
