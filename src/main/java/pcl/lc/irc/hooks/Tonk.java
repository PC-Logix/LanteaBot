package pcl.lc.irc.hooks;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.joda.time.DateTime;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.httpd.httpd;
import pcl.lc.irc.*;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.entryClasses.CommandRateLimit;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PreparedStatementKeys {
	static String GET_TONK_COUNT = "getTonkCount";
	static String GET_TONK_USERS = "getTonkUsers";
	static String CLEAR_EVERYTHING_TONK = "clearEverythingTonk";
	static String GET_THREE_TOP_TONKS = "getThreeTopTonks";
	static String CLEAR_TONK_FAILS = "clearTonkFails";
}

/**
 * A module for Tonking
 * Created by Forecaster on 30/03/2017 for the LanteaBot project.
 */
public class Tonk extends AbstractListener {
	private static final String numberFormat = "#.########";
	static final String tonk_record_key = "tonkrecord";
	private static final String last_tonk_key = "lasttonk";
	private static final String tonk_attempts_key = "tonkattempt";
	private static final boolean applyBonusPoints = true;
	private static final int maxTonkFails = 2;
	private Command local_command;
	private Command reset_command;
	private Command tonkout_command;
	private Command tonkpoints_command;
	private Command wind_back_command;
	private Command tonkreseteverything_command;
	private Command tonk_attempts_remaining;
	private Command tonk_merge_scores;
	private Command tonk_destroy_scores;
	private Command tonk_snipe;
	private Command tonk_snipe_blue;
	private Command tonk_snipe_red;
	private Command tonk_snipe_green;
	private Command tonk_snipe_count;
	private CommandRateLimit rateLimit;

	enum TonkSnipeType {
		BLUE("blue", new String[]{"Blue Shell", "s"}, "Shell", 0.2, 0, 1, "#1"),
		RED("red", new String[]{"Red Shell", "s"}, "Shell", 0.50, 14, 3, "+5"),
		GREEN("green", new String[]{"Green Shell", "s"}, "Shell", 0.30, 10, 5, "+3");

		String keyword; //Keyword is used as the command but also as a collection key
		String[] displayName;
		String typeClass; //Eg. "Shell" or "Bullet" etc.
		double pointTransferPercentage;
		int hitDC;
		int maxUses;
		String targetPosition; // Define the possible target positions as #n for a static position, or as a +n or -n for a range between n and the users current position. If null or invalid no restriction is considered.

		TonkSnipeType(String keyword, String[] displayName, String typeClass, double pointTransferPercentage, int hitDC, int maxUses) {
			this(keyword, displayName, typeClass, pointTransferPercentage, hitDC, maxUses, null);
		}

		TonkSnipeType(String keyword, String displayName, String typeClass, double pointTransferPercentage, int hitDC, int maxUses) {
			this(keyword, new String[]{displayName}, typeClass, pointTransferPercentage, hitDC, maxUses, null);
		}

		TonkSnipeType(String keyword, String displayName, String typeClass, double pointTransferPercentage, int hitDC, int maxUses, String targetPosition) {
			this(keyword, new String[]{displayName}, typeClass, pointTransferPercentage, hitDC, maxUses, targetPosition);
		}

		TonkSnipeType(String keyword, String[] displayName, String typeClass, double pointTransferPercentage, int hitDC, int maxUsers, String targetPosition) {
			this.keyword = keyword;
			this.displayName = displayName;
			this.typeClass = typeClass;
			this.pointTransferPercentage = pointTransferPercentage;
			this.hitDC = hitDC;
			this.maxUses = maxUsers;
			this.targetPosition = targetPosition;
		}

		public String getDisplayName() {
			return getDisplayName(false);
		}

		public String getDisplayName(boolean plural) {
			return this.displayName[0] + (plural ? this.displayName[1] : "");
		}

		public String canTarget() {
			if (this.targetPosition == null || this.targetPosition.equals(""))
				return "";
			Pattern pattern;
			Matcher matcher;
			pattern = Pattern.compile("#(\\d+)");
			matcher = pattern.matcher(this.targetPosition);
			if (matcher.find())
				return "This type targets users in position #" + matcher.group(1) + ".";
			pattern = Pattern.compile("\\+(\\d+)");
			matcher = pattern.matcher(this.targetPosition);
			if (matcher.find())
				return "This type targets users up to " + matcher.group(1) + " positions ahead of you.";
			pattern = Pattern.compile("(-\\d+)");
			matcher = pattern.matcher(this.targetPosition);
			if (matcher.find())
				return "This type targets users down to " + matcher.group(1) + " positions behind you.";
			return "";
		}

		public boolean isValidTarget(String sniper, String target) throws Exception {
			if (this.targetPosition == null || this.targetPosition.equals(""))
				return true;
			int offset = Integer.MIN_VALUE;
			Pattern pattern;
			Matcher matcher;
			pattern = Pattern.compile("#(\\d+)");
			matcher = pattern.matcher(this.targetPosition);
			boolean matchesStatic = false;
			try {
				matchesStatic = matcher.find();
			} catch (Exception ignored) {
			}
			if (matchesStatic) {
				int targetPosition = Integer.parseInt(matcher.group(1));
				if (getScoreboardPosition(target) == targetPosition)
					return true;
				else
					throw new Exception(this.getDisplayName(true) + " can only target position " + this.targetPosition);
			}
			pattern = Pattern.compile("\\+(\\d+)");
			matcher = pattern.matcher(this.targetPosition);
			try {
				if (matcher.find()) {
					String match = matcher.group(1);
					offset = Integer.parseInt(match);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			pattern = Pattern.compile("(-\\d+)");
			matcher = pattern.matcher(this.targetPosition);
			try {
				if (matcher.find())
					offset = Integer.parseInt(matcher.group(1));
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Offset for '" + this.targetPosition + "': " + offset);
			if (offset != Integer.MIN_VALUE) {
				int positionDiff = getScoreboardPosition(sniper) - getScoreboardPosition(target);
				if (positionDiff <= offset)
					return true;
				else {
					throw new Exception(this.getDisplayName(true) + " can only target within " + String.valueOf(offset).replace("-", "") + " positions " + (offset > 0 ? "ahead of" : "behind") + " you.");
				}
			}
			throw new Exception("The isValidTarget method shouldn't reach this point.");
		}
	}

	@Override
	protected void initHook() {
		initCommands();
		httpd.registerContext("/tonk", new TonkHandler(), "Tonk");
		IRCBot.registerCommand(local_command);
		IRCBot.registerCommand(reset_command);
		IRCBot.registerCommand(tonkout_command);
		IRCBot.registerCommand(tonkpoints_command);
		IRCBot.registerCommand(tonk_attempts_remaining);
		IRCBot.registerCommand(tonk_merge_scores);
		IRCBot.registerCommand(tonk_destroy_scores);
		IRCBot.registerCommand(tonk_snipe);
		IRCBot.registerCommand(wind_back_command);
		IRCBot.registerCommand(tonkreseteverything_command);
		Database.addPreparedStatement(PreparedStatementKeys.GET_TONK_COUNT, "SELECT count(*) FROM JsonData;");
		Database.addPreparedStatement(PreparedStatementKeys.GET_TONK_USERS, "SELECT mykey, store FROM JsonData WHERE mykey LIKE '" + tonk_record_key + "_%' ORDER BY CAST(store AS DECIMAL) DESC;");
		Database.addPreparedStatement(PreparedStatementKeys.CLEAR_EVERYTHING_TONK, "DELETE FROM JsonData WHERE mykey like '" + tonk_record_key + "_%' OR mykey ='" + tonk_record_key + "' OR mykey = '" + last_tonk_key + "'");
		Database.addPreparedStatement(PreparedStatementKeys.GET_THREE_TOP_TONKS, "SELECT mykey, store FROM JsonData WHERE mykey like '" + tonk_record_key + "_%' ORDER BY store DESC LIMIT 3");
		Database.addPreparedStatement(PreparedStatementKeys.CLEAR_TONK_FAILS, "DELETE FROM JsonData WHERE mykey LIKE '" + tonk_attempts_key + "_%'");
	}

	static String html;

	public Tonk() throws IOException {
		InputStream htmlIn = getClass().getResourceAsStream("/html/tonk.html");
		html = CharStreams.toString(new InputStreamReader(htmlIn, Charsets.UTF_8));
	}

	private void initCommands() {
		rateLimit = new CommandRateLimit(0, 15, 0, false, true);
		local_command = new Command("tonk", rateLimit) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				int attempts = getTonkFails(nick);
				if (attempts >= maxTonkFails) {
					Helper.sendMessage(target, "A sad trumpet plays for an uncomfortably long time...");
					return;
				}

				String tonkin = Database.getJsonData(last_tonk_key);
				String tonk_record = Database.getJsonData(tonk_record_key);
				long now = new Date().getTime();
				IRCBot.log.info("tonkin :" + tonkin + " tonk_record: " + tonk_record);
				if (tonkin.equals("") || tonk_record.equals("")) {
					Helper.sendMessage(target, "You got the first Tonk " + nick + ", but this is only the beginning.");
					Database.storeJsonData(tonk_record_key, "0;" + nick);
					Database.storeJsonData(last_tonk_key, String.valueOf(now));
					IRCBot.log.info("No previous tonk found");
				} else {
					long lasttonk = 0;
					lasttonk = Long.parseLong(tonkin);

					long diff = now - lasttonk;

					String position = "";
					String advance = "";
					String overtook = "";

					String[] tonk = tonk_record.split(";");
					long tonk_record_long = Long.parseLong(tonk[0]);
					System.out.println("Tonk record long: " + tonk_record_long);
					String recorder = tonk[1].trim();
					boolean nick_is_recorder = nick.equals(recorder);

					if (tonk_record_long < diff) {
						IRCBot.log.info("New record");
						IRCBot.log.info("'" + recorder + "' == '" + nick + "' => " + (nick_is_recorder ? "true" : "false"));

						int record_hours = GetHours(tonk_record_long) + 1;
						System.out.println("Record hours: " + record_hours);

						String personal_record_key = tonk_record_key + "_" + nick;
						double hours = GetHoursDouble(diff - tonk_record_long, 2);

						double tonk_record_personal = 0;
						try {
							tonk_record_personal = Double.parseDouble(Database.getJsonData(personal_record_key));
						} catch (Exception ignored) {
						}

						if (!nick_is_recorder) {
							System.out.println("Points added to score: " + hours + " * " + record_hours + " = " + (hours * record_hours));
							tonk_record_personal += (hours * record_hours);
							int pre_position = getScoreboardPosition(nick);
							Database.storeJsonData(personal_record_key, String.valueOf(tonk_record_personal));
							int post_position = getScoreboardPosition(nick);
							position = (pre_position == post_position || pre_position == -1 ? " Position #" + post_position : " Position #" + pre_position + " => #" + post_position) + ".";
							if (pre_position != post_position)
								overtook = " (Overtook " + getByScoreboardPosition(post_position + 1) + ")";

							ScoreRemainingResult sr = getScoreRemainingToAdvance(nick);
							if (sr != null && sr.user != null) {
								advance = " Need " + displayTonkPoints(sr.score - tonk_record_personal) + " more points to pass " + Helper.antiPing(sr.user) + "!";
							}

						} else {
							System.out.println("No points gained because nick equals record holder (Lost: " + hours + " * " + record_hours + " = " + (hours * record_hours) + ")");
						}

						Helper.sendMessage(target, Exclamation.getRandomExpression() + "! " + Helper.antiPing(nick) + "! You beat " + (nick_is_recorder ? "your own" : Helper.antiPing(recorder) + "'s") + " previous record of " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)) + " (By " + Helper.timeString(Helper.parseMilliseconds(diff - tonk_record_long)) + ")! I hope you're happy!");
						Helper.sendMessage(target, nick + "'s new record is " + Helper.timeString(Helper.parseMilliseconds(diff)) + "! " + ((Helper.round(hours / 1000d, 8) > 0) ? (!nick_is_recorder ? (" " + nick + " also gained " + displayTonkPoints(hours * record_hours) + (record_hours > 1 ? " (" + displayTonkPoints(hours) + " x " + record_hours + ")" : "") + " tonk points for stealing the tonk.") : " No points gained for stealing from yourself. (Lost out on " + displayTonkPoints(hours) + (record_hours > 1 ? " x " + record_hours + " = " + displayTonkPoints(hours * record_hours) : "") + ")") : "") + position + overtook + advance);
						Database.storeJsonData(tonk_record_key, diff + ";" + nick);
						Database.storeJsonData(last_tonk_key, String.valueOf(now));
						Database.getPreparedStatement(PreparedStatementKeys.CLEAR_TONK_FAILS).executeUpdate();
					} else {
//							if (nick_is_recorder) {
//								Helper.sendMessage(target, "You still hold the record " + nick + ", for now... " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)));
//							} else {
						IRCBot.log.info("No new record set");
						Helper.sendMessage(target, "I'm sorry " + nick + ", you were not able to beat " + recorder + "'s record of " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)) + " this time. " +
								Helper.timeString(Helper.parseMilliseconds(diff)) + " were wasted! Missed by " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long - diff)) + "!");
						Database.storeJsonData(last_tonk_key, String.valueOf(now));
						Database.storeJsonData(tonk_attempts_key + "_" + nick, String.valueOf(attempts + 1));
//							}
					}
				}
			}
		};
		local_command.setHelpText("What is tonk? Tonk is life. For a description of the rules see " + Config.commandprefix + "tonkleaders");

		reset_command = new Command("resettonk", Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				long now = new Date().getTime();
				Helper.sendMessage(target, "Tonk reset " + nick + ", you are the record holder!");
				Database.storeJsonData(tonk_record_key, "0;" + nick);
				Database.storeJsonData(last_tonk_key, String.valueOf(now));
				Database.getPreparedStatement(PreparedStatementKeys.CLEAR_TONK_FAILS).executeUpdate();
			}
		};
		reset_command.setHelpText("Reset current tonk.");
		reset_command.registerAlias("tonkreset");

		wind_back_command = new Command("tonkback", Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				tonkTimeRemove(params);
			}
		};
		reset_command.setHelpText("Used for testing.");

		tonkout_command = new Command("tonkout", rateLimit) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				int attempts = getTonkFails(nick);
				if (attempts >= maxTonkFails) {
					Helper.sendMessage(target, "A sad flute plays for an uncomfortably long time...");
					return;
				}

				String tonkin = Database.getJsonData(last_tonk_key);
				String tonk_record = Database.getJsonData(tonk_record_key);
				long now = new Date().getTime();
				IRCBot.log.info("tonkin :" + tonkin + " tonk_record: " + tonk_record);
				if (tonkin.equals("") || tonk_record.equals("")) {
					Helper.sendMessage(target, "You got the first Tonk " + nick + ", but this is only the beginning.");
					Database.storeJsonData(tonk_record_key, "0;" + nick);
					Database.storeJsonData(last_tonk_key, String.valueOf(now));
					IRCBot.log.info("No previous tonk found");
				} else {
					long lasttonk = 0;
					lasttonk = Long.parseLong(tonkin);

					long diff = now - lasttonk;

					String[] tonk = tonk_record.split(";");
					long tonk_record_long = Long.parseLong(tonk[0]);
					String recorder = tonk[1].trim();
					boolean nick_is_recorder = nick.equals(recorder);

//                        if (nick_is_recorder) {
					if (tonk_record_long <= 0) {
						Database.storeJsonData(last_tonk_key, String.valueOf(now));
						Helper.sendMessage(target, "You gotta tonk before you can tonk out. For this transgression the timer has been reset.", nick);
					} else if (tonk_record_long < diff) {
						String personal_record_key = tonk_record_key + "_" + nick;

						int hours = GetHours(diff);

						double tonk_record_personal = 0;
						try {
							tonk_record_personal = Double.parseDouble(Database.getJsonData(personal_record_key));
						} catch (Exception ignored) {
						}

						tonk_record_personal += hours;

						boolean applyPoints = false;
						if (applyBonusPoints && hours > 1)
							applyPoints = true;

						if (applyPoints) {
							if (nick_is_recorder)
								tonk_record_personal += 2d * (hours - 1);
							else
								tonk_record_personal += (2d * (hours - 1) * 0.75d);
						}
						int pre_position = getScoreboardPosition(nick);
						Database.storeJsonData(personal_record_key, String.valueOf(tonk_record_personal));
						int post_position = getScoreboardPosition(nick);

						String position = (pre_position == post_position || pre_position == -1 ? "Position #" + post_position : "Position #" + pre_position + " => #" + post_position);
						String overtook = "";
						if (pre_position != post_position)
							overtook = " (Overtook " + getByScoreboardPosition(post_position + 1) + ")";

						String advance = "";
						ScoreRemainingResult sr = getScoreRemainingToAdvance(nick);
						if (sr != null && sr.user != null) {
							advance = " Need " + displayTonkPoints(sr.score - tonk_record_personal) + " more points to pass " + Helper.antiPing(sr.user) + "!";
						}

						Helper.sendMessage(target, Exclamation.getRandomExpression() + "! " + Helper.antiPing(nick) + "! You beat " + (nick_is_recorder ? "your own" : Helper.antiPing(recorder) + "'s") + " previous record of " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)) + " (By " + Helper.timeString(Helper.parseMilliseconds(diff - tonk_record_long)) + ")! I hope you're happy!");
						if (nick_is_recorder)
							Helper.sendMessage(target, Helper.antiPing(nick) + " has tonked out! Tonk has been reset! They gained " + displayTonkPoints(hours) + " tonk points!" + (applyPoints ? " plus " + displayTonkPoints(2d * (hours - 1)) + " bonus points for consecutive hours!" : "") + " Current score: " + displayTonkPoints(tonk_record_personal) + ", " + position + overtook + advance);
						else
							Helper.sendMessage(target, Helper.antiPing(nick) + " has stolen the tonkout! Tonk has been reset! They gained " + displayTonkPoints(hours) + " tonk points!" + (applyPoints ? " plus " + displayTonkPoints((2d * (hours - 1)) * 0.5d) + " bonus points for consecutive hours! (Reduced to 50% because stealing)" : "") + " Current score: " + displayTonkPoints(tonk_record_personal) + ". " + position + overtook + advance);

						Database.storeJsonData(tonk_record_key, "0;" + nick);
						Database.storeJsonData(last_tonk_key, String.valueOf(now));
						try {
							Database.getPreparedStatement(PreparedStatementKeys.CLEAR_TONK_FAILS).executeUpdate();
						} catch (Exception ex) {
							Helper.sendMessage(target, "Failed to reset tonk fail counters.");
							ex.printStackTrace();
						}
					} else {
						Helper.sendMessage(target, "I'm sorry " + Helper.antiPing(nick) + ", you were not able to beat " + Helper.antiPing(recorder) + "'s record of " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)) + " this time. " +
								Helper.timeString(Helper.parseMilliseconds(diff)) + " were wasted! Missed by " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long - diff)) + "!");
						Database.storeJsonData(last_tonk_key, String.valueOf(now));
						Database.storeJsonData(tonk_attempts_key + "_" + nick, String.valueOf(attempts + 1));
					}
				}
			}
		};
		tonkout_command.setHelpText("What is tonk? Tonk is life. For a description of the rules see " + Config.commandprefix + "tonkleaders");
		tonkout_command.registerAlias("tonktonk");

		tonkpoints_command = new Command("tonkpoints") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				String data = Database.getJsonData(tonk_record_key + "_" + nick);
				if (data != null && !data.isEmpty()) {
					double score = Double.parseDouble(data);
					String advance = "";
					ScoreRemainingResult rs = getScoreRemainingToAdvance(nick);
					if (rs != null && rs.user != null)
						advance = " Need " + displayTonkPoints(rs.score - score) + " more points to pass " + Helper.antiPing(rs.user) + "!";
					Helper.sendMessage(target, "You currently have " + displayTonkPoints(score) + " points! Position #" + getScoreboardPosition(nick) + advance, nick);
				} else {
					Helper.sendMessage(target, "I can't find a record, so you have 0 points.", nick);
				}
			}
		};
		tonkpoints_command.registerAlias("tonkscore");

		tonkreseteverything_command = new Command("tonkreseteverything", Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				PreparedStatement top = Database.getPreparedStatement(PreparedStatementKeys.GET_THREE_TOP_TONKS);
				ResultSet result = top.executeQuery();
				String prefix = "Top scores: ";
				ArrayList<String> topList = new ArrayList<>();
				int position = 0;
				while (result.next()) {
					position++;
					topList.add("#" + position + ": " + result.getString(1).replace("tonkrecord_", ""));
				}
				Helper.sendMessage(target, prefix + String.join(", ", topList));
				Helper.sendMessage(target, "Resetting the tonk scoreboard forever!");

				PreparedStatement reset = Database.getPreparedStatement(PreparedStatementKeys.CLEAR_EVERYTHING_TONK);
				reset.executeUpdate();
			}
		};

		tonk_attempts_remaining = new Command("tonkattempts") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				int attempts = maxTonkFails - getTonkFails(nick);

				if (attempts <= 0)
					Helper.sendMessage(target, "You have no attempts left. When a successful tonk or tonkout happens everyone gets " + maxTonkFails + " new attempts.");
				else
					Helper.sendMessage(target, "You have " + Math.max(0, attempts) + " attempt" + (attempts == 1 ? "" : "s") + " left.");
			}
		};

		tonk_merge_scores = new Command("tonkmerge", Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> param) throws Exception {
				String from_key = tonk_record_key + "_" + param.get(1);
				String to_key = tonk_record_key + "_" + param.get(0);
				double from = Double.parseDouble(Database.getJsonData(from_key));
				double to = Double.parseDouble(Database.getJsonData(to_key));

				Database.storeJsonData(to_key, String.valueOf(to + from));
				Database.destroyJsonData(from_key);
				Helper.sendMessage(target, "Merge successful! " + param.get(1) + ": " + displayTonkPoints(from) + " + " + param.get(0) + ": " + displayTonkPoints(to) + " => " + param.get(0) + ": " + displayTonkPoints(to + from));
			}
		};
		tonk_merge_scores.setHelpText("Merges the score of the second name into the first name and wipes the second name from the scoreboard. Syntax: " + Config.commandprefix + tonk_merge_scores.getCommand() + " <first_name> <second_name>");

		tonk_destroy_scores = new Command("tonkdestroy", Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> param) {
				ArrayList<String> successes = new ArrayList<>();
				ArrayList<String> failures = new ArrayList<>();
				for (String name : param) {
					try {
						String key = tonk_record_key + "_" + name;
						Database.destroyJsonData(key);
						successes.add(name);
					} catch (Exception ex) {
						ex.printStackTrace();
						failures.add(name);
					}
				}
				String succ = "";
				if (successes.size() > 0)
					succ = "Cleared " + String.join(",", successes);
				String fail = "";
				if (failures.size() > 0)
					fail = "Failed to clear " + String.join(",", failures);
				Helper.sendMessage(target, succ + (!succ.equals("") && !fail.equals("") ? ", " : "") + fail);
			}
		};
		tonk_destroy_scores.setHelpText("Wipes entries from the tonk scoreboard. Accepts as many names as arguments as will fit in a message.");

		DecimalFormat tonkSnipePercentageFormat = new DecimalFormat("#");
		tonk_snipe = new Command("tonksnipe") {
		};
		tonk_snipe.registerAlias("redshell", TonkSnipeType.RED.keyword);
		tonk_snipe.registerAlias("blueshell", TonkSnipeType.BLUE.keyword);
		tonk_snipe.registerAlias("greenshell", TonkSnipeType.GREEN.keyword);
		tonk_snipe.registerAlias("shellcount", "count");
		tonk_snipe.registerAlias("tonkshells", "count");
		tonk_snipe.registerAlias("ammocount", "count");
		tonk_snipe.setHelpText("If you are in last place on the tonk scoreboard you can attempt to snipe someone with a green, red, or blue shell. A successful snipe will remove a percentage (depending on the shell type) of the difference between your and their points, and give it to you. You can only succeed once. If it fails you can try again after " + TonkSnipe.daysBetweenTonkSnipes + " days.");

		tonk_snipe_blue = new Command(TonkSnipeType.BLUE.keyword) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (getMaxScoreboardPosition() <= 1) {
					Helper.sendMessage(target, "There are not enough people on the scoreboard.", nick);
				}
				if (TonkSnipe.canUseType(nick, TonkSnipeType.BLUE)) {
					String leader = getByScoreboardPosition(1);
					if (leader != null && !leader.equals(nick))
						Helper.sendMessage(target, TonkSnipe.doSnipe(nick, leader, TonkSnipeType.BLUE), nick);
					else if (leader == null)
						Helper.sendMessage(target, "There seems to be no one in first position...", nick);
					else
						Helper.sendMessage(target, "You probably don't want to target yourself.", nick);
				} else
					Helper.sendMessage(target, "You are out of " + TonkSnipeType.BLUE.getDisplayName(true), nick);
			}
		};
		tonk_snipe.registerSubCommand(tonk_snipe_blue);

		tonk_snipe_red = new Command(TonkSnipeType.RED.keyword) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws Exception {
				String targetUser = params.get(0);
				TonkSnipeType.RED.isValidTarget(nick, targetUser);
				Helper.sendMessage(target, TonkSnipe.doSnipe(nick, targetUser, TonkSnipeType.RED), nick);
			}
		};
		tonk_snipe.registerSubCommand(tonk_snipe_red);

		tonk_snipe_green = new Command(TonkSnipeType.GREEN.keyword) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws Exception {
				String targetUser = params.get(0);
				TonkSnipeType.GREEN.isValidTarget(nick, targetUser);
				Helper.sendMessage(target, TonkSnipe.doSnipe(nick, targetUser, TonkSnipeType.GREEN), nick);
			}
		};
		tonk_snipe.registerSubCommand(tonk_snipe_green);

		tonk_snipe_count = new Command("count") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				ArrayList<String> shells = new ArrayList<>();
				for (TonkSnipeType type : TonkSnipeType.values()) {
					int shellCount = TonkSnipe.shellCount(nick, type);
					shells.add(shellCount + " " + type.getDisplayName(shellCount != 1));
				}
				Helper.sendMessage(target, "You have " + Helper.oxfordJoin(shells, ", ", ", and "), nick);
			}
		};
		tonk_snipe.registerSubCommand(tonk_snipe_count);
	}

	static int GetHours(long tonk_time) {
		return (int) Math.floor(tonk_time / 1000d / 60d / 60d);
	}

	public static double GetHoursDouble(long tonk_time, int decimals) {
		double hours = tonk_time / 1000d / 60d / 60d;
		return Helper.round(hours, decimals);
	}

	private static int getTonkFails(String nick) {
		int attempts = 0;
		try {
			String strAttempts = Database.getJsonData(tonk_attempts_key + "_" + nick);
			if (!strAttempts.equals(""))
				attempts = Integer.parseInt(strAttempts);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return attempts;
	}

	/**
	 * @param nick Username
	 * @return The scoreboard position of specified user. Returns -1 if users is not on scoreboard.
	 */
	static int getScoreboardPosition(String nick) {
		try {
			PreparedStatement stop = Database.getPreparedStatement(PreparedStatementKeys.GET_TONK_USERS);
			ResultSet result = stop.executeQuery();

			int index = 0;
			while (result.next()) {
				index++;
				String user = result.getString(1).replace(tonk_record_key + "_", "");
				if (nick.toLowerCase().equals(user.toLowerCase()))
					return index;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * @return The highest current scoreboard position (last place)
	 */
	private static int getMaxScoreboardPosition() {
		try {
			PreparedStatement stop = Database.getPreparedStatement(PreparedStatementKeys.GET_TONK_USERS);
			ResultSet result = stop.executeQuery();

			int index = 0;
			while (result.next()) {
				index++;
			}
			return index;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * @param position Scoreboard position to get
	 * @return The name of the user in the specified scoreboard position, or null if position doesn't exist.
	 */
	public static String getByScoreboardPosition(int position) {
		try {
			PreparedStatement stop = Database.getPreparedStatement(PreparedStatementKeys.GET_TONK_USERS);
			ResultSet result = stop.executeQuery();

			int index = 0;
			while (result.next()) {
				index++;
				String user = result.getString(1).replace(tonk_record_key + "_", "");
				if (index == position)
					return user;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	static class ScoreRemainingResult {
		public double score;
		public String user;

		ScoreRemainingResult(double score, String user) {
			this.score = score;
			this.user = user;
		}
	}

	static ScoreRemainingResult getScoreRemainingToAdvance(String nick) {
		try {
			PreparedStatement stop = Database.getPreparedStatement(PreparedStatementKeys.GET_TONK_USERS);
			ResultSet result = stop.executeQuery();

			double prev_score = -1;
			String prev_user = null;
			while (result.next()) {
				String user = result.getString(1).replace(tonk_record_key + "_", "");
				if (nick.toLowerCase().equals(user.toLowerCase()))
					return new ScoreRemainingResult(prev_score, prev_user);
				prev_score = result.getDouble(2);
				prev_user = user;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	static class TonkHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {

			String target = t.getRequestURI().toString();
			String response = "";

			try {
				PreparedStatement getCount = Database.getPreparedStatement("getTonkCount");
				ResultSet res = getCount.executeQuery();
				System.out.println(res.getInt(1));
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			String sniperAmmo = "";
			if (TonkSnipe.enableTonkSnipe) {
				sniperAmmo += "<div style='margin-top:4px;'>" +
						"<h2>Tonk Sniping</h2>" +
						"<p>You can now launch attacks against someones tonk score. You have a certain number of each which resets on the complete reset only. You may be able to find more in certain ways. Find out how many by using the command '" + Config.commandprefix + "tonksnipe count' or one of its aliases.</p>" +
						"<p>On a successful hit, determined by rolling a d20 and beating a DC, a percentage of the <b>difference</b> between yours and the targets points are removed from the target and transferred to you.</p>" +
						"<p>Whether the snipe hit or missed the sniper cannot execute another snipe for 24 hours, and the target cannot be the target of a snipe from anyone for 24 hours.</p>";
				sniperAmmo += "<ul>";
				for (TonkSnipeType type : TonkSnipeType.values()) {
					String canTarget = type.canTarget();
					String hit = type.hitDC == 0 ? " - Always hits!" : " - Hit DC: " + type.hitDC;
					sniperAmmo += "<li>" + type.getDisplayName() + hit + (canTarget.equals("") ? "," : ", " + canTarget) + " Starting uses: " + type.maxUses + ", Transfer percentage: " + type.pointTransferPercentage * 100 + "%</li>";
				}
				sniperAmmo += "</ul></div>";
			}

			DecimalFormat format = new DecimalFormat("#");
			String tonkLeaders = "<div>" +
					"<h2>Tonk</h2>" +
					"<p>Tonk is a game about timing, patience and opportunity, mixed with an idle game.</p>" +
					"<p>To tonk successfully you must have waited past the current record. Trying to Tonk too early resets the timer.</p>" +
					"<p>Tonking successfully gives you points depending on the difference between the old record and your new record, unless you held the previous record.</p>" +
					"<p>Instead of Tonking you can Tonkout, this works the same as Tonk, except after claiming the new record, the entire record is converted to points.</p>" +
					"<p>If the number of hours in the record is over 2 hours you get bonus points, with a multiplier based on the number of hours in the record minus 1.</p>" +
					"<p>If you held the previous record you get 100% of the multiplied bonus, if not you get 50%.</p>" +
					"</div>" +
					"<div style='margin-top:4px;'>" +
					"<h2>Failed Tonks</h2>" +
					"<p>If you perform " + maxTonkFails + " mistimed tonks or tonkouts you can no longer attempt it.</p>" +
					"<p>When a successful tonk or tonkout happens everyone gets " + maxTonkFails + " new attempts.</p>" +
					"</div>" +
					sniperAmmo +
					"<table>";
			try {
				PreparedStatement statement = Database.getPreparedStatement("getTonkUsers");
				ResultSet resultSet = statement.executeQuery();
				int count = 0;
				while (resultSet.next()) {
					count++;
					tonkLeaders += "<tr><td>#" + count + " " + resultSet.getString(1).replace(tonk_record_key + "_", "") + "</td><td>" + displayTonkPoints(resultSet.getString(2)) + "</td></tr>";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			tonkLeaders += "</table>";
			List<NameValuePair> paramsList = URLEncodedUtils.parse(t.getRequestURI(), "utf-8");


			String navData = "";
			Iterator it = httpd.pages.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				navData += "<div class=\"innertube\"><h1><a href=\"" + pair.getValue() + "\">" + pair.getKey() + "</a></h1></div>";
			}

			// convert String into InputStream
			InputStream is = new ByteArrayInputStream(html.getBytes());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String line = null;
				while ((line = br.readLine()) != null) {
					response = response + line.replace("#BODY#", target).replace("#BOTNICK#", IRCBot.getOurNick()).replace("#TONKDATA#", tonkLeaders)
							.replace("#NAVIGATION#", navData) + "\n";
				}
			}
			t.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	public static String displayTonkPoints(int points) {
		return displayTonkPoints((double) points);
	}

	public static String displayTonkPoints(String points) {
		return displayTonkPoints(Double.parseDouble(points));
	}

	public static String displayTonkPoints(double points) {
		DecimalFormat dec = new DecimalFormat(numberFormat);
		return dec.format(points / 1000d);
	}

	public static void tonkPointsAdd(String user, int add) {
		String key = tonk_record_key + "_" + user;
		double points = 0;
		try {
			points = Double.parseDouble(Database.getJsonData(key));
			double pointsLastNew = points + add;
			Database.storeJsonData(key, String.valueOf(pointsLastNew));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void tonkPointsRemove(String user, int remove) {
		String key = tonk_record_key + "_" + user;
		double points = 0;
		try {
			points = Double.parseDouble(Database.getJsonData(key));
			double pointsLastNew = points - remove;
			Database.storeJsonData(key, String.valueOf(pointsLastNew));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param time A string containing one or more numbers followed by d, h, m, or s.
	 */
	public static void tonkTimeRemove(String time) {
		Pattern pattern;
		Matcher matcher;
		long removeTime = 0;
		pattern = Pattern.compile("(\\d+)d");
		matcher = pattern.matcher(time);
		if (matcher.find()) {
			double days = Double.parseDouble(matcher.group(1));
			removeTime += days * 24 * 60 * 60 * 1000;
		}
		pattern = Pattern.compile("(\\d+)h");
		matcher = pattern.matcher(time);
		if (matcher.find()) {
			double hours = Double.parseDouble(matcher.group(1));
			removeTime += hours * 60 * 60 * 1000;
		}
		pattern = Pattern.compile("(\\d+)m");
		matcher = pattern.matcher(time);
		if (matcher.find()) {
			double minutes = Double.parseDouble(matcher.group(1));
			removeTime += minutes * 60 * 1000;
		}
		pattern = Pattern.compile("(\\d+)s");
		matcher = pattern.matcher(time);
		if (matcher.find()) {
			double seconds = Double.parseDouble(matcher.group(1));
			removeTime += seconds * 1000;
		}
		try {
			long tonkTime = Long.parseLong(Database.getJsonData(last_tonk_key));
			tonkTime -= removeTime;
			Database.storeJsonData(last_tonk_key, String.valueOf(tonkTime));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param time A string containing one or more numbers followed by d, h, m, or s.
	 */
	public static void tonkTimeAdd(String time) {
		Pattern pattern;
		Matcher matcher;
		long addTime = 0;
		pattern = Pattern.compile("(\\d+)d");
		matcher = pattern.matcher(time);
		if (matcher.find()) {
			double days = Double.parseDouble(matcher.group(1));
			addTime += days * 24 * 60 * 60 * 1000;
		}
		pattern = Pattern.compile("(\\d+)h");
		matcher = pattern.matcher(time);
		if (matcher.find()) {
			double hours = Double.parseDouble(matcher.group(1));
			addTime += hours * 60 * 60 * 1000;
		}
		pattern = Pattern.compile("(\\d+)m");
		matcher = pattern.matcher(time);
		if (matcher.find()) {
			double minutes = Double.parseDouble(matcher.group(1));
			addTime += minutes * 60 * 1000;
		}
		pattern = Pattern.compile("(\\d+)s");
		matcher = pattern.matcher(time);
		if (matcher.find()) {
			double seconds = Double.parseDouble(matcher.group(1));
			addTime += seconds * 1000;
		}
		try {
			long tonkTime = Long.parseLong(Database.getJsonData(last_tonk_key));
			tonkTime += addTime;
			Database.storeJsonData(last_tonk_key, String.valueOf(tonkTime));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class TonkSnipe {
	static final boolean enableTonkSnipe = true;
	static final String shells_key = "tonk_snipe_shells";
	static final String last_hit_key = "tonk_snipe_last_hit";
	static final String last_snipe_key = "tonk_snipe_last_snipe";
	static final int daysBetweenTonkSnipes = 5;

	static String doSnipe(String sniper, String snipeTarget, Tonk.TonkSnipeType type) {
		if (!enableTonkSnipe) {
			return "You chuck the " + type.typeClass.toLowerCase() + " you got forward just before realizing it's just a plastic replica and it clatters to the ground in front of you...";
		}
		if (!canUseType(sniper, type)) {
			return "You are out of " + type.getDisplayName(true);
		}
		long canSnipeIn = canSnipeIn(sniper);
		System.out.println("Can snipe in: " + canSnipeIn);
		if (canSnipeIn > 0)
			return "You can't snipe right now. Try again in " + Helper.parseMilliseconds(canSnipeIn).toString() + ".";
		long canTargetIn = canBeTargetedIn(snipeTarget);
		System.out.println("Can target in: " + canTargetIn);
		if (canTargetIn > 0)
			return "You can't target this user right now. Try again in " + Helper.parseMilliseconds(canTargetIn).toString() + ".";
		spend(sniper, type, 1);
		int rollResult = Helper.rollDice("d20").getSum();
		boolean snipeSuccess = rollResult >= type.hitDC;
		setSnipe(sniper, snipeTarget);
		if (snipeSuccess) {
			String keySniper = Tonk.tonk_record_key + "_" + sniper;
			String keyTarget = Tonk.tonk_record_key + "_" + snipeTarget;
			double pointsSniper = 0;
			double pointsTarget = 0;
			try {
				pointsSniper = Double.parseDouble(Database.getJsonData(keySniper));
				pointsTarget = Double.parseDouble(Database.getJsonData(keyTarget));
			} catch (Exception e) {
				e.printStackTrace();
			}
			double diff = pointsTarget - pointsSniper;
			if (diff > 0) {
				int prePos = Tonk.getScoreboardPosition(sniper);
				diff = diff * type.pointTransferPercentage;
				try {
					double pointsLastNew = pointsSniper + diff;
					double pointsTargetNew = pointsTarget - diff;
					Database.storeJsonData(keySniper, String.valueOf(pointsLastNew));
					Database.storeJsonData(keyTarget, String.valueOf(pointsTargetNew));
					int postPos = Tonk.getScoreboardPosition(sniper);
					String pos = (prePos == postPos ? " Position #" + postPos : " Position #" + prePos + " => #" + postPos);
					String overtook = "";
					if (prePos != postPos)
						overtook = " (Overtook " + Tonk.getByScoreboardPosition(postPos + 1) + ")";

					String advance = "";
					Tonk.ScoreRemainingResult sr = Tonk.getScoreRemainingToAdvance(sniper);
					if (sr != null && sr.user != null) {
						advance = " Need " + Tonk.displayTonkPoints(sr.score - pointsLastNew) + " more points to pass " + Helper.antiPing(sr.user) + "!";
					}
					return "You hit " + snipeTarget + "! They lost " + Tonk.displayTonkPoints(diff) + " tonk points which you gain! Congratulations!" + pos + overtook + advance;
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to save new scores...";
				}
			} else
				return "You hit " + snipeTarget + " but nothing happened... (Point difference was not greater than zero)";
		}
		return "Unfortunately you missed with a " + rollResult + " vs " + type.hitDC + ".";
	}

	static int shellCount(String nick, Tonk.TonkSnipeType type) {
		try {
			String key = shells_key + "_" + nick;
			HashMap<String, Integer> shells = Database.getJsonHashMapInt(key);
			if (shells == null || !shells.containsKey(type.keyword)) {
				System.out.println("No data matched key '" + type.keyword + "'. Assuming default value.");
				return type.maxUses;
			}
			System.out.println(shells);
			return type.maxUses - shells.get(type.keyword);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @param nick A username
	 * @param type The shell type
	 * @return True if user has at least one use of the specified ammo type, otherwise false
	 */
	static boolean canUseType(String nick, Tonk.TonkSnipeType type) {
		return shellCount(nick, type) > 0;
	}

	/**
	 * @param sniper The user who is attempting to snipe
	 * @return The number of milliseconds the sniper needs to wait before they can snipe again.
	 */
	static long canSnipeIn(String sniper) {
		String key = last_snipe_key + "_" + sniper;
		try {
			String lastSnipe = Database.getJsonData(key);
			if (!lastSnipe.equals("")) {
				Date timeOfLastSnipe = new Date(Long.parseLong(lastSnipe));
				Date compareTime = DateTime.now().minusHours(24).toDate();
				return Math.max(0, timeOfLastSnipe.getTime() - compareTime.getTime());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @param target The user targeted by a snipe
	 * @return The number of milliseconds the target has left before they are no longer immune to being sniped.
	 */
	static long canBeTargetedIn(String target) {
		String key = last_hit_key + "_" + target;
		try {
			String lastSniped = Database.getJsonData(key);
			if (!lastSniped.equals("")) {
				Date timeOfHit = new Date(Long.parseLong(lastSniped));
				Date compareTime = DateTime.now().minusHours(24).toDate();
				return Math.max(0, timeOfHit.getTime() - compareTime.getTime());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @param target The target's username
	 * @return Returns true if the user can target this target right now, or false otherwise.
	 */
	static boolean canTarget(String target) {
		return canBeTargetedIn(target) == 0;
	}

	static void spend(String nick, Tonk.TonkSnipeType type) {
		spend(nick, type, 1);
	}

	static void spend(String nick, Tonk.TonkSnipeType type, int uses) {
		try {
			HashMap<String, Integer> snipes;
			String key = shells_key + "_" + nick;
			snipes = Database.getJsonHashMapInt(key);
			if (snipes != null) {
				if (snipes.containsKey(type.keyword))
					snipes.put(type.keyword, snipes.get(type.keyword) + uses);
				else
					snipes.put(type.keyword, uses);
			} else {
				snipes = new HashMap<>();
				snipes.put(type.keyword, uses);
			}
			Database.storeJsonData(key, snipes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void refill(String nick, Tonk.TonkSnipeType type) {
		refill(nick, type, 1);
	}

	static void refill(String nick, Tonk.TonkSnipeType type, int uses) {
		try {
			String key = shells_key + "_" + nick;
			HashMap<String, Integer> snipes = Database.getJsonHashMapInt(key);
			if (snipes != null) {
				if (snipes.containsKey(type.keyword))
					snipes.put(type.keyword, snipes.get(type.keyword) - uses);
				else
					snipes.put(type.keyword, -uses);
			} else {
				snipes = new HashMap<>();
				snipes.put(type.keyword, -uses);
			}
			Database.storeJsonData(key, snipes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void setSnipe(String sniper, String snipeTarget) {
		try {
			String keyTarget = last_hit_key + "_" + snipeTarget;
			String keySniper = last_snipe_key + "_" + sniper;
			Database.storeJsonData(keyTarget, String.valueOf(new Date().getTime()));
			Database.storeJsonData(keySniper, String.valueOf(new Date().getTime()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
