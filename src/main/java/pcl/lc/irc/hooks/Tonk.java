package pcl.lc.irc.hooks;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.joda.time.DateTime;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.httpd.httpd;
import pcl.lc.irc.*;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.*;

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
	private static final String tonk_record_key = "tonkrecord";
	private static final String last_tonk_key = "lasttonk";
	private static final String tonk_attempts_key = "tonkattempt";
	private static final boolean applyBonusPoints = true;
	private static final boolean enableTonkSnipe = true;
	private static final double tonkSnipeHitChancePercent = 5;
	private static final double pointTransferPercentage = 0.2;
	private static final int daysBetweenTonkSnipes = 5;
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
	private CommandRateLimit rateLimit;

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
		rateLimit = new CommandRateLimit(0, 15, 0);
		local_command = new Command("tonk", rateLimit) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				int attempts = getTonkFails(nick);
				if (attempts >= maxTonkFails) {
					Helper.sendMessage(target, "A sad trumpet plays for an uncomfortably long time...");
					return;
				}

				try {
					String tonkin = Database.getJsonData(last_tonk_key);
					String tonk_record = Database.getJsonData(tonk_record_key);
					long now = new Date().getTime();
					IRCBot.log.info("tonkin :" + tonkin + " tonk_record: " + tonk_record);
					if (tonkin.equals("") || tonk_record.equals("")) {
						Helper.sendMessage(target, "You got the first Tonk " + nick + ", but this is only the beginning.");
						try {
							Database.storeJsonData(tonk_record_key, "0;" + nick);
							Database.storeJsonData(last_tonk_key, String.valueOf(now));
						} catch (Exception ex) {
							Helper.sendMessage(target, "An error occurred while updating values.");
							ex.printStackTrace();
						}
						IRCBot.log.info("No previous tonk found");
					} else {
						long lasttonk = 0;
						try {
							lasttonk = Long.parseLong(tonkin);
						} catch (Exception ex) {
							System.out.println(ex.getMessage());
							IRCBot.log.info(ex.getClass() + ": " + ex.getMessage());
						}

						long diff = now - lasttonk;

						String position = "";
						String advance = "";
						String overtook = "";

						try {
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

								Helper.sendMessage(target, CurseWord.getRandomCurse() + "! " + Helper.antiPing(nick) + "! You beat " + (nick_is_recorder ? "your own" : Helper.antiPing(recorder) + "'s") + " previous record of " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)) + " (By " + Helper.timeString(Helper.parseMilliseconds(diff - tonk_record_long)) + ")! I hope you're happy!");
								Helper.sendMessage(target, nick + "'s new record is " + Helper.timeString(Helper.parseMilliseconds(diff)) + "! " + ((Helper.round(hours / 1000d, 8) > 0) ? (!nick_is_recorder ? (" " + nick + " also gained " + displayTonkPoints(hours * record_hours) + (record_hours > 1 ? " (" + displayTonkPoints(hours) + " x " + record_hours + ")" : "") + " tonk points for stealing the tonk.") : " No points gained for stealing from yourself. (Lost out on " + displayTonkPoints(hours) + (record_hours > 1 ? " x " + record_hours + " = " + displayTonkPoints(hours * record_hours) : "") + ")") : "") + position + overtook + advance);
								Database.storeJsonData(tonk_record_key, diff + ";" + nick);
								Database.storeJsonData(last_tonk_key, String.valueOf(now));
								try {
									Database.getPreparedStatement(PreparedStatementKeys.CLEAR_TONK_FAILS).executeUpdate();
								} catch (Exception ex) {
									Helper.sendMessage(target, "Failed to reset tonk fail counters.");
									ex.printStackTrace();
								}
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
						} catch (Exception ex) {
							IRCBot.log.info(ex.getClass() + ": " + ex.getMessage());
						}
					}
				} catch (Exception ex) {
					Helper.sendMessage(target, "Failed to retrieve values.");
					ex.printStackTrace();
				}
			}
		};
		local_command.setHelpText("What is tonk? Tonk is life. For a description of the rules see " + Config.commandprefix + "tonkleaders");
		
		reset_command = new Command("resettonk", Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				long now = new Date().getTime();
				Helper.sendMessage(target, "Tonk reset " + nick + ", you are the record holder!");
				try {
					Database.storeJsonData(tonk_record_key, "0;" + nick);
					Database.storeJsonData(last_tonk_key, String.valueOf(now));
					Database.getPreparedStatement(PreparedStatementKeys.CLEAR_TONK_FAILS).executeUpdate();
				} catch (Exception e) {
					Helper.sendMessage(target, "An error occurred while updating values.");
					e.printStackTrace();
				}
			}
		};
		reset_command.setHelpText("Reset current tonk.");
		reset_command.registerAlias("tonkreset");

		wind_back_command = new Command("tonkback", Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					long remove_time = 0;
					String unit = "hour";
					try {
						if (params.contains("m")) {
							params = params.replace("m", "");
							remove_time = 1000 * 60 * Integer.parseInt(params);
							unit = "minute";
						} else {
							params = params.replace("h", "");
							remove_time = 1000 * 60 * 60 * Integer.parseInt(params);
						}
					} catch (Exception ignored) {}
					long tonk_time = Long.parseLong(Database.getJsonData(last_tonk_key));
					tonk_time -= remove_time;
					Database.storeJsonData(last_tonk_key, String.valueOf(tonk_time));
					Helper.sendMessage(target, "Last tonk has been rewound by " + params + " " + unit + (Integer.parseInt(params) == 1 ? "": "s") + "!");
				} catch (Exception ex) {
					ex.printStackTrace();
					Helper.sendMessage(target, "Something went wrong.");
				}
			}
		};
		reset_command.setHelpText("Used for testing.");

		tonkout_command = new Command("tonkout", rateLimit) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				int attempts = getTonkFails(nick);
				if (attempts >= maxTonkFails) {
					Helper.sendMessage(target, "A sad flute plays for an uncomfortably long time...");
					return;
				}

				try {
					String tonkin = Database.getJsonData(last_tonk_key);
					String tonk_record = Database.getJsonData(tonk_record_key);
					long now = new Date().getTime();
					IRCBot.log.info("tonkin :" + tonkin + " tonk_record: " + tonk_record);
					if (tonkin.equals("") || tonk_record.equals("")) {
						Helper.sendMessage(target, "You got the first Tonk " + nick + ", but this is only the beginning.");
						try {
							Database.storeJsonData(tonk_record_key, "0;" + nick);
							Database.storeJsonData(last_tonk_key, String.valueOf(now));
						} catch (Exception ex) {
							Helper.sendMessage(target, "An error occurred while updating values.");
							ex.printStackTrace();
						}
						IRCBot.log.info("No previous tonk found");
					} else {
						long lasttonk = 0;
						try {
							lasttonk = Long.parseLong(tonkin);
						} catch (Exception ex) {
							System.out.println(ex.getMessage());
							IRCBot.log.info(ex.getClass() + ": " + ex.getMessage());
						}

						long diff = now - lasttonk;

						try {
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

								Helper.sendMessage(target, CurseWord.getRandomCurse() + "! " + Helper.antiPing(nick) + "! You beat " + (nick_is_recorder ? "your own" : Helper.antiPing(recorder) + "'s") + " previous record of " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)) + " (By " + Helper.timeString(Helper.parseMilliseconds(diff - tonk_record_long)) + ")! I hope you're happy!");
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
//                        } else {
//                            Helper.sendMessage(target, "You are not the current record holder. It is " + recorder + ".", nick);
//                        }
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				} catch (Exception ex) {
					Helper.sendMessage(target, "Failed to retrieve values.");
					ex.printStackTrace();
				}
            }
		};
		tonkout_command.setHelpText("What is tonk? Tonk is life. For a description of the rules see " + Config.commandprefix + "tonkleaders");
		tonkout_command.registerAlias("tonktonk");

		tonkpoints_command = new Command("tonkpoints") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
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
				} catch (Exception ex) {
					Helper.sendMessage(target, "An error occurred while trying to retrieve score.");
					ex.printStackTrace();
				}
			}
		};
		tonkpoints_command.registerAlias("tonkscore");

		tonkreseteverything_command = new Command("tonkreseteverything", Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
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
				} catch (Exception e) {
					e.printStackTrace();
				}
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
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> param) {
				try {
					String from_key = tonk_record_key + "_" + param.get(1);
					String to_key = tonk_record_key + "_" + param.get(0);
					double from = Double.parseDouble(Database.getJsonData(from_key));
					double to = Double.parseDouble(Database.getJsonData(to_key));

					Database.storeJsonData(to_key, String.valueOf(to + from));
					Database.destroyJsonData(from_key);
					Helper.sendMessage(target, "Merge successful! " + param.get(1) + ": " + displayTonkPoints(from) + " + " + param.get(0) + ": " + displayTonkPoints(to) + " => " + param.get(0) + ": " + displayTonkPoints(to + from));
				} catch (Exception e) {
					e.printStackTrace();
					Helper.sendMessage(target, "Merge failed!");
				}
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

		tonk_snipe = new Command("tonksnipe"/*new CommandRateLimit(60)*/) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (!enableTonkSnipe) {
					Helper.sendMessage(target, "You chuck the shell you got forward just before realizing it's made out of cheap plastic and it clatters to the ground in front of you...", nick);
					return;
				}
				int maxScoreboardPosition = getMaxScoreboardPosition();
				int scoreboardPosition = getScoreboardPosition(nick);
				if (scoreboardPosition == -1)
					Helper.sendMessage(target, "You are not on the scoreboard.", nick);
				else if (maxScoreboardPosition == 1)
					Helper.sendMessage(target, "You're the only one on the scoreboard...", nick);
				else if (scoreboardPosition == maxScoreboardPosition) {
					Helper.sendMessage(target, "You're last! You get a shell!", nick);
					if (tonkSnipeCanSnipe(nick)) {
						boolean snipeSuccess = Helper.getRandomInt(0, 100) >= tonkSnipeHitChancePercent;
						if (snipeSuccess) {
							String leader = getByScoreboardPosition(1);
							if (leader == null) {
								Helper.sendMessage(target, "Seems there's nobody in first position...");
								return;
							}
							String key_last = tonk_record_key + "_" + nick;
							String key_leader = tonk_record_key + "_" + leader;
							double points_last = 0;
							double points_leader = 0;
							try {
								points_last = Double.parseDouble(Database.getJsonData(key_last));
								points_leader = Double.parseDouble(Database.getJsonData(key_leader));
							} catch (Exception e) {
								e.printStackTrace();
							}
							double diff = points_leader - points_last;
							if (diff > 0) {
								int prePos = getScoreboardPosition(nick);
								diff = diff * pointTransferPercentage;
								try {
									double points_last_new = points_last + diff;
									double points_leader_new = points_leader + diff;
									Database.storeJsonData(key_last, String.valueOf(points_last_new));
									Database.storeJsonData(key_leader, String.valueOf(points_leader_new));
									int postPos = getScoreboardPosition(nick);
									String pos = (prePos == postPos ? " Position #" + postPos : " Position #" + prePos + " => #" + postPos);
									String overtook = "";
									if (prePos != postPos)
										overtook = " (Overtook " + getByScoreboardPosition(postPos - 1) + ")";

									String advance = "";
									ScoreRemainingResult sr = getScoreRemainingToAdvance(nick);
									if (sr != null && sr.user != null) {
										advance = " Need " + displayTonkPoints(sr.score - points_last_new) + " more points to pass " + Helper.antiPing(sr.user) + "!";
									}
									Helper.sendMessage(target, "You hit " + leader + "! They lost " + displayTonkPoints(diff) + " tonk points which you gain! Congratulations!" + pos + overtook + advance);
								} catch (Exception e) {
									Helper.sendMessage(target, "Unable to save new scores...", nick);
									e.printStackTrace();
								}
							} else
								Helper.sendMessage(target, "You hit " + leader + " but nothing happened... (Point difference was 0)", nick);
						} else
							Helper.sendMessage(target, "Unfortunately you missed.", nick);
						tonkSnipeSetSnipe(nick, snipeSuccess);
					} else {
						Helper.sendMessage(target, "You have already attempted a snipe. Try again in " + tonkSnipeDaysUntilRetry() + " days.", nick);
					}
				} else
					Helper.sendMessage(target, "You're not last on the tonk scoreboard. You cannot have a shell.", nick);
			}
		};
		tonk_snipe.registerAlias("tonkshell");
		tonk_snipe.registerAlias("blueshell");
		DecimalFormat format = new DecimalFormat("#");
		tonk_snipe.setHelpText("If you are in last place on the tonk scoreboard you can attempt to snipe whoever is in first place. A successful snipe will remove " + format.format(pointTransferPercentage * 100) + "% of the difference between your and their points, and give it to you. You can only succeed once. If it fails you can try again after " + daysBetweenTonkSnipes + " days.");
	}

	static boolean tonkSnipeCanSnipe(String nick) {
		try {
			String tonksnipe = Database.getJsonData("tonksnipe");
			if (tonksnipe.equals(""))
				return true;
			String[] data = tonksnipe.split(";");
			if (!data[0].equals(nick))
				return true;
			DateTime now = new DateTime(Long.parseLong(data[1]));
			now.plusDays(daysBetweenTonkSnipes);
			if (now.isAfterNow())
				return false;
			if (data[2].equals("true"))
				return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	static void tonkSnipeSetSnipe(String nick, boolean successfull) {
		try {
			Database.storeJsonData("tonksnipe", nick + ";" + new Date().getTime() + ";" + (successfull ? "true" : "false"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static int tonkSnipeDaysUntilRetry() {
		try {
			String tonksnipe = Database.getJsonData("tonksnipe");
			if (tonksnipe.equals(""))
				return 0;
			String[] data = tonksnipe.split(";");
			return (int) Math.floor((new Date().getTime() - Long.parseLong(data[1])) / 1000 / 60 / 60 / 24) + daysBetweenTonkSnipes;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	static int GetHours(long tonk_time) {
		return (int)Math.floor(tonk_time / 1000d / 60d / 60d);
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

	private static int getScoreboardPosition(String nick) {
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
	private static ScoreRemainingResult getScoreRemainingToAdvance(String nick) {
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

			DecimalFormat format = new DecimalFormat("#");
			String tonkLeaders = "<div>" +
					"<ul>" +
					"<li>Tonk is a game about timing, patience and opportunity, mixed with an idle game.</li>" +
					"<li>To tonk successfully you must have waited past the current record. Trying to Tonk too early resets the timer.</li>" +
					"<li>Tonking successfully gives you points depending on the difference between the old record and your new record, unless you held the previous record.</li>" +
					"<li>Instead of Tonking you can Tonkout, this works the same as Tonk, except after claiming the new record, the entire record is converted to points.</li>" +
					"<li>If the number of hours in the record is over 2 hours you get bonus points, with a multiplier based on the number of hours in the record minus 1.</li>" +
					"<li>If you held the previous record you get 100% of the multiplied bonus, if not you get 50%.</li>" +
					"</ul>" +
					"</div>" +
					"<div style='margin-top:4px;'>" +
					"<ul>" +
					"<li>If you perform " + maxTonkFails + " mistimed tonks or tonkouts you can no longer attempt it.</li>" +
					"<li>When a successful tonk or tonkout happens everyone gets " + maxTonkFails + " new attempts.</li>" +
					"</ul>" +
					"</div>" +
					"<div style='margin-top:4px;'>" +
					"<ul>" +
					"<li>If you are in the very last place of the scoreboard you can use the tonksnipe command to attempt to steal " + format.format(pointTransferPercentage * 100) + "% of the difference between your and whoever is in first place's points.</li>" +
					"<li>If you fail you can retry after " + daysBetweenTonkSnipes + " days.</li>" +
					"</ul>" +
					"</div>" +
					"<table>";
			try {
				PreparedStatement statement = Database.getPreparedStatement("getTonkUsers");
				ResultSet resultSet = statement.executeQuery();
				int count = 0;
				while (resultSet.next()) {
					count++;
					tonkLeaders += "<tr><td>#" + count + " " + resultSet.getString(1).replace(tonk_record_key + "_",  "") + "</td><td>" + displayTonkPoints(resultSet.getString(2)) + "</td></tr>";
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			tonkLeaders += "</table>";
			List<NameValuePair> paramsList = URLEncodedUtils.parse(t.getRequestURI(),"utf-8");


			String navData = "";
		    Iterator it = httpd.pages.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pair = (Map.Entry)it.next();
		        navData += "<div class=\"innertube\"><h1><a href=\""+ pair.getValue() +"\">"+ pair.getKey() +"</a></h1></div>";
		    }

			// convert String into InputStream
			InputStream is = new ByteArrayInputStream(html.getBytes());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String line = null;
				while ((line = br.readLine()) != null) {
					response = response + line.replace("#BODY#", target).replace("#BOTNICK#", IRCBot.getOurNick()).replace("#TONKDATA#", tonkLeaders)
							.replace("#NAVIGATION#", navData)+"\n";
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
}
