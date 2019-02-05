package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * A module for Tonking
 * Created by Forecaster on 30/03/2017 for the LanteaBot project.
 */
public class Tonk extends AbstractListener {
	private Command local_command;
	private Command reset_command;
	private Command tonkout_command;
	private Command tonkpoints_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
		IRCBot.registerCommand(reset_command);
		IRCBot.registerCommand(tonkout_command);
		IRCBot.registerCommand(tonkpoints_command);		
		Database.addPreparedStatement("storeJSON", "INSERT OR REPLACE INTO JsonData (mykey, store) VALUES (?, ?);");
		Database.addPreparedStatement("retreiveJSON", "SELECT store FROM JsonData WHERE mykey = ?");
	}
	
	public static boolean storeJsonData(String key, String data) {
//		try {
//			statement.executeQuery("CREATE TABLE IF NOT EXISTS JsonData (mykey VARCHAR(255) PRIMARY KEY NOT NULL, store TEXT DEFAULT NULL); CREATE UNIQUE INDEX JsonData_key_uindex ON JsonData (mykey)");
//		} catch (SQLException e) {
//			if (e.getErrorCode() != 101)
//				IRCBot.log.error("Exception is: ", e);
//				e.printStackTrace();
//		}
		try {
			IRCBot.log.info("storeJsonData: ('" + key.toLowerCase() + "', '" + data + "')");
			PreparedStatement stmt = Database.preparedStatements.get("storeJSON");
			stmt.setString(1, key);
			stmt.setString(2, data);
			stmt.execute();

			return true;
		} catch (SQLException e) {
			IRCBot.log.error("Exception is: ", e);
			e.printStackTrace();
		}
		IRCBot.log.error("storeJsonData false");
		return false;
	}

	public static String getJsonData(String key) {
//		try {
//			statement.executeQuery("CREATE TABLE IF NOT EXISTS JsonData (mykey VARCHAR(255) PRIMARY KEY NOT NULL, store TEXT DEFAULT NULL); CREATE UNIQUE INDEX JsonData_key_uindex ON JsonData (mykey)");
//		} catch (SQLException e) {
//			if (e.getErrorCode() != 101)
//				IRCBot.log.error("Exception is: ", e);
//				e.printStackTrace();
//		}
		try {
			PreparedStatement stmt = Database.preparedStatements.get("retreiveJSON");
			stmt.setString(1, key);
			
			ResultSet theResult = stmt.executeQuery();
			if (theResult.next()) {
				String result = theResult.getString(1);
				IRCBot.log.info("JsonData: " + result);
				return result;
			}
			IRCBot.log.error("JsonData was empty, returning empty string");
			return "";
		} catch (SQLException e) {
			IRCBot.log.error("Code: " + e.getErrorCode());
			IRCBot.log.error("Exception is: ", e);
			e.printStackTrace();
		}
		IRCBot.log.error("JsonData try/catch failed");
		return "";
	}
	
	private void initCommands() {
		local_command = new Command("tonk", 60) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String tonkin = getJsonData("lasttonk");
				String tonk_record = getJsonData("tonkrecord");
				long now = new Date().getTime();
				IRCBot.log.info("tonkin :" + tonkin + " tonk_record: " + tonk_record);
				if (tonkin == "" || tonk_record == "") {
					Helper.sendMessage(target, "You got the first Tonk " + nick + ", but this is only the beginning.");
					storeJsonData("tonkrecord", "0;" + nick);
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
						String tonk[] = tonk_record.split(";");
						long tonk_record_long = Long.parseLong(tonk[0]);
						String recorder = tonk[1].trim();

						if (tonk_record_long < diff) {
							IRCBot.log.info("New record");
							IRCBot.log.info("'" + recorder + "' == '" + nick + "' => " + (nick.equals(recorder) ? "true" : "false"));

							Helper.sendMessage(target, Curse.getRandomCurse() + "! " + nick + "! You beat " + (nick.equals(recorder) ? "your own" : recorder + "'s") + " previous record of " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)) + "! I hope you're happy!");
							Helper.sendMessage(target, nick + "'s new record is " + Helper.timeString(Helper.parseMilliseconds(diff)) + "! " + Helper.timeString(Helper.parseMilliseconds(diff - tonk_record_long)) + " gained!");
							storeJsonData("tonkrecord", diff + ";" + nick);
							storeJsonData("lasttonk", String.valueOf(now));
						} else {
							if (nick.equals(recorder)) {
								Helper.sendMessage(target, "You still hold the record " + nick + ", for now... " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)));
							} else {
								IRCBot.log.info("No new record set");
								Helper.sendMessage(target, "I'm sorry " + nick + ", you were not able to beat " + (nick.equals(recorder) ? "your own" : recorder + "'s") + " record of " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)) + " this time.");
								Helper.sendMessage(target, Helper.timeString(Helper.parseMilliseconds(diff)) + " were wasted! Missed by " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long - diff)) + "!");
								storeJsonData("lasttonk", String.valueOf(now));
							}
						}
					} catch (Exception ex) {
						IRCBot.log.info(ex.getClass() + ": " + ex.getMessage());
					}
				}
			}
		};
		local_command.setHelpText("What is tonk? Tonk is life.");
		
		reset_command = new Command("resettonk", 60, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				long now = new Date().getTime();
				Helper.sendMessage(target, "Tonk reset " + nick + ", you are the record holder!");
				storeJsonData("tonkrecord", "0;" + nick);
				storeJsonData("lasttonk", String.valueOf(now));
			}
		};
		reset_command.setHelpText("What is tonk? Tonk is life.");
		reset_command.registerAlias("tonkreset");

		tonkout_command = new Command("tonkout", 60) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String tonkin = getJsonData("lasttonk");
				String tonk_record = getJsonData("tonkrecord");

				String tonk[] = tonk_record.split(";");
				long tonk_record_long = Long.parseLong(tonk[0]);
				String recorder = tonk[1].trim();

				if (nick.equals(recorder)) {
					String personal_record_key = "tonkrecord_" + nick;

					System.out.println("Record long: " + String.valueOf(tonk_record_long));
					int hours = (int)Math.floor(tonk_record_long / 1000 / 60 / 60);
					System.out.println("Hours: " + hours);

					double tonk_record_personal = 0;
					try {
						tonk_record_personal = Double.parseDouble(getJsonData(personal_record_key));
					} catch (Exception ignored) {}

					tonk_record_personal += hours;

					storeJsonData(personal_record_key, String.valueOf(tonk_record_personal));

					Helper.sendMessage(target, nick + " has tonked out! Tonk has been reset! They gained " + (hours / 1000d) + " tonk points! Current score: " + (tonk_record_personal / 1000d));

					long now = new Date().getTime();
					storeJsonData("tonkrecord", "0;" + nick);
					storeJsonData("lasttonk", String.valueOf(now));
				} else {
					Helper.sendMessage(target, "You are not the current record holder. It is " + recorder + ".");
				}
			}
		};
		tonkout_command.setHelpText("Cash in your tonks!");

		tonkpoints_command = new Command("tonkpoints") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String data = getJsonData("tonkrecord_" + nick);
				if (data != null && !data.isEmpty()) {
					Helper.sendMessage(target, "You currently have " + (Double.parseDouble(data) / 1000d) + " points!", nick);
				} else {
					Helper.sendMessage(target, "I can't find any record, so you have 0 points.", nick);
				}
			}
		};
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		target = Helper.getTarget(event);
		if (target.contains("#")) {
			local_command.tryExecute(command, nick, target, event, copyOfRange);
			reset_command.tryExecute(command, nick, target, event, copyOfRange);
			tonkout_command.tryExecute(command, nick, target, event, copyOfRange);
			tonkpoints_command.tryExecute(command, nick, target, event, copyOfRange);
		}
	}
}
