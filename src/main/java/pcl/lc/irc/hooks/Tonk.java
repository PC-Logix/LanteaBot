package pcl.lc.irc.hooks;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
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
    private static String numberFormat = "#.########";
	private static String tonk_record_key = "tonkrecord";
	private static String last_tonk_key = "lasttonk";
	private static String tonk_attempts_key = "tonkattempt";
	private static boolean applyBonusPoints = true;
	private static int maxTonkFails = 2;
	private Command local_command;
	private Command reset_command;
	private Command tonkout_command;
	private Command tonkpoints_command;
	private Command wind_back_command;
	private Command tonkreseteverything_command;
	private Command tonk_attempts_remaining;

	@Override
	protected void initHook() {
		initCommands();
		httpd.registerContext("/tonk", new TonkHandler(), "Tonk");
		IRCBot.registerCommand(local_command);
		IRCBot.registerCommand(reset_command);
		IRCBot.registerCommand(tonkout_command);
		IRCBot.registerCommand(tonkpoints_command);
		IRCBot.registerCommand(tonk_attempts_remaining);
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
		local_command = new Command("tonk", 900) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				nick = parseNick(nick);

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
									Database.storeJsonData(personal_record_key, String.valueOf(tonk_record_personal));
								} else {
									System.out.println("No points gained because nick equals record holder (Lost: " + hours + " * " + record_hours + " = " + (hours * record_hours) + ")");
								}

								Helper.sendMessage(target, Curse.getRandomCurse() + "! " + nick + "! You beat " + (nick_is_recorder ? "your own" : recorder + "'s") + " previous record of " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)) + " (By " + Helper.timeString(Helper.parseMilliseconds(diff - tonk_record_long)) + ")! I hope you're happy!");
								DecimalFormat dec = new DecimalFormat(numberFormat);
								Helper.sendMessage(target, nick + "'s new record is " + Helper.timeString(Helper.parseMilliseconds(diff)) + "! " + ((Helper.round(hours / 1000d, 8) > 0) ? (!nick_is_recorder ? (" " + nick + " also gained " + dec.format((hours * record_hours) / 1000d) + (record_hours > 1 ? " (" + dec.format(hours / 1000d) + " x " + record_hours + ")" : "") + " tonk points for stealing the tonk.") : " No points gained for stealing from yourself. (Lost out on " + dec.format(hours / 1000d) + (record_hours > 1 ? " x " + record_hours + " = " + dec.format((hours * record_hours) / 1000d) : "") + ")") : ""));
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
		
		reset_command = new Command("resettonk", 60, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				long now = new Date().getTime();
				nick = parseNick(nick);
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

		wind_back_command = new Command("tonkback", 60, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					long tonk_time = Long.parseLong(Database.getJsonData(last_tonk_key));
					tonk_time -= 1000 * 60 * 60;
					Database.storeJsonData(last_tonk_key, String.valueOf(tonk_time));
					Helper.sendMessage(target, "Last tonk has been rewound by one hour!");
				} catch (Exception ex) {
					ex.printStackTrace();
					Helper.sendMessage(target, "Something went wrong.");
				}
			}
		};
		reset_command.setHelpText("Used for testing.");

		tonkout_command = new Command("tonkout", 900) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
                nick = parseNick(nick);

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

								Database.storeJsonData(personal_record_key, String.valueOf(tonk_record_personal));

								DecimalFormat dec = new DecimalFormat(numberFormat);
								Helper.sendMessage(target, Curse.getRandomCurse() + "! " + nick + "! You beat " + (nick_is_recorder ? "your own" : recorder + "'s") + " previous record of " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)) + " (By " + Helper.timeString(Helper.parseMilliseconds(diff - tonk_record_long)) + ")! I hope you're happy!");
								if (nick_is_recorder)
									Helper.sendMessage(target, nick + " has tonked out! Tonk has been reset! They gained " + dec.format(hours / 1000d) + " tonk points!" + (applyPoints ? " plus " + dec.format((2d * (hours - 1)) / 1000d) + " bonus points for consecutive hours!" : "") + " Current score: " + dec.format(tonk_record_personal / 1000d));
								else
									Helper.sendMessage(target, nick + " has stolen the tonkout! Tonk has been reset! They gained " + dec.format(hours / 1000d) + " tonk points!" + (applyPoints ? " plus " + dec.format(((2d * (hours - 1)) * 0.5d) / 1000d) + " bonus points for consecutive hours! (Reduced to 50% because stealing)" : "") + " Current score: " + dec.format(tonk_record_personal / 1000d));

								Database.storeJsonData(tonk_record_key, "0;" + nick);
								Database.storeJsonData(last_tonk_key, String.valueOf(now));
								try {
									Database.getPreparedStatement(PreparedStatementKeys.CLEAR_TONK_FAILS).executeUpdate();
								} catch (Exception ex) {
									Helper.sendMessage(target, "Failed to reset tonk fail counters.");
									ex.printStackTrace();
								}
							} else {
								Helper.sendMessage(target, "I'm sorry " + nick + ", you were not able to beat " + recorder + "'s record of " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)) + " this time. " +
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
				nick = parseNick(nick);

				try {
					String data = Database.getJsonData(tonk_record_key + "_" + nick);
					if (data != null && !data.isEmpty()) {
						DecimalFormat dec = new DecimalFormat(numberFormat);
						Helper.sendMessage(target, "You currently have " + dec.format(Double.parseDouble(data) / 1000d) + " points!", nick);
					} else {
						Helper.sendMessage(target, "I can't find a record, so you have 0 points.", nick);
					}
				} catch (Exception ex) {
					Helper.sendMessage(target, "An error occurred while trying to retrieve score.");
					ex.printStackTrace();
				}
			}
		};

		tonkreseteverything_command = new Command("tonkreseteverything", 0, Permissions.ADMIN) {
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
				nick = parseNick(nick);
				int attempts = maxTonkFails - getTonkFails(nick);

				if (attempts <= 0)
					Helper.sendMessage(target, "You have no attempts left. When a successful tonk or tonkout happens everyone gets " + maxTonkFails + " new attempts.");
				else
					Helper.sendMessage(target, "You have " + Math.max(0, attempts) + " attempt" + (attempts == 1 ? "" : "s") + " left.");
			}
		};
	}

	static int GetHours(long tonk_time) {
//		System.out.println("Record long: " + tonk_time);
		int hours = (int)Math.floor(tonk_time / 1000d / 60d / 60d);
//		System.out.println("Hours: " + hours);

		return hours;
	}

	public static double GetHoursDouble(long tonk_time, int decimals) {
//		System.out.println("Record long: " + tonk_time);
		double hours = tonk_time / 1000d / 60d / 60d;
//		System.out.println("Hours: " + hours);

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
                    "<table>";
			try {
				PreparedStatement statement = Database.getPreparedStatement("getTonkUsers");
				ResultSet resultSet = statement.executeQuery();
				int count = 0;
				while (resultSet.next()) {
					count++;
					DecimalFormat dec = new DecimalFormat(numberFormat);
					tonkLeaders += "<tr><td>#" + count + " " + resultSet.getString(1).replace(tonk_record_key + "_",  "") + "</td><td>" + dec.format(Double.parseDouble(resultSet.getString(2)) / 1000d) + "</td></tr>";
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

	private String parseNick(String nick) {
		return nick.replaceAll("\\p{C}", "");
	}
	
	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args, String callingRelay) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange, String callingRelay) {
		target = Helper.getTarget(event);
		if (target.contains("#")) {
			local_command.tryExecute(command, nick, target, event, copyOfRange);
			reset_command.tryExecute(command, nick, target, event, copyOfRange);
			tonkout_command.tryExecute(command, nick, target, event, copyOfRange);
			tonkpoints_command.tryExecute(command, nick, target, event, copyOfRange);
			wind_back_command.tryExecute(command, nick, target, event, copyOfRange);
			tonkreseteverything_command.tryExecute(command, nick, target, event, copyOfRange);
			tonk_attempts_remaining.tryExecute(command, nick, target, event, copyOfRange);
		}
	}
}
