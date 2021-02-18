package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.*;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Reminders extends AbstractListener {
	private Command remind;
	private Command remindSomeone;
	private Command list;
	private Command reminders;
	private ScheduledFuture<?> executor;
	@Override
	protected void initHook() {
		remind = new Command("remind", new CommandArgumentParser(2, new CommandArgument("Time", ArgumentTypes.STRING), new CommandArgument("Message", ArgumentTypes.STRING))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws Exception {
				String timeString = this.argumentParser.getArgument("Time");
				String message = this.argumentParser.getArgument("Message");
				if (timeString.equals("later"))
					timeString = Helper.getRandomInt(3, 6).toString() + "h";
				else if (timeString.equals("laterish") || timeString.equals("soon") || timeString.equals("soonish"))
					timeString = Helper.getRandomInt(2,4).toString() + "h";
				long time;
				try {
					time = Helper.getFutureTime(timeString);
				} catch (IllegalArgumentException e) {
					Helper.sendMessage(target, "Unable to parse \"" + timeString + "\" as a time string.", nick);
					return;
				}
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
				String newTime = sdf.format(new Date(time));
				PreparedStatement addReminder = Database.getPreparedStatement("addReminder");
				addReminder.setString(1, target);
				if (event.getUser().getNick().equals("Corded")) {
					nick = "@" + nick;
				}
				addReminder.setString(2, nick.replaceAll("​", ""));
				addReminder.setLong(3, time);
				addReminder.setString(4, message.trim());
				if (addReminder.executeUpdate() > 0) {
					Helper.sendMessage(target, "I'll remind you about \"" + message.trim() + "\" in " + timeString + " at " + newTime);
					return;
				}
				Helper.sendMessage(target, "No reminder was added...", nick);
			}
		};
		remind.registerAlias("remindme");
		remind.setHelpText("'remindme 1h20m check your food!' Will send a reminder in 1 hour and 20 minutes in the channel the command was sent (or PM if you PMed the bot)");

		remindSomeone = new Command("remindthem", new CommandArgumentParser(3, new CommandArgument("Nick", ArgumentTypes.STRING), new CommandArgument("Time", ArgumentTypes.STRING), new CommandArgument("Message", ArgumentTypes.STRING))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws Exception {
				String user = this.argumentParser.getArgument("Nick");
				String timeString = this.argumentParser.getArgument("Time");
				String message = this.argumentParser.getArgument("Message");

				long time = Helper.getFutureTime(timeString);
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
				String newTime = sdf.format(new Date(time));
				PreparedStatement addReminder = Database.getPreparedStatement("addReminder");
				addReminder.setString(1, target);
				if (event.getUser().getNick().equals("Corded")) {
					nick = "@" + nick;
				}
				addReminder.setString(2, user);
				addReminder.setLong(3, time);
				addReminder.setString(4, message.trim());
				if (addReminder.executeUpdate() > 0) {
					Helper.sendMessage(target, "I'll remind " + user + " about \"" + message.trim() + "\" at " + newTime);
					return;
				}
			}
		};
		remindSomeone.setHelpText("'remindthem gamax92 1h20m check your food!' Will send a reminder in 1 hour and 20 minutes in the channel the command was sent (or PM if you PMed the bot) to gamax92");
		
		list = new Command("list") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					PreparedStatement listReminders = Database.getPreparedStatement("listReminders");
					listReminders.setString(1, nick);
					ResultSet results = listReminders.executeQuery();
					int counter = 0;
					Helper.sendMessage(target, "Upcoming reminders", nick);
					while (results.next()) {
						counter++;
						long millis = results.getLong(3);
						SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
						String newTime = sdf.format(new Date(millis));
						Helper.sendMessage(target, results.getString(4) + " At " + newTime, nick);
					}
					if (counter == 0)
						Helper.sendMessage(target, "None. You have no reminders. But did you remember to rotate the fridge?");
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
				Helper.sendMessage(target, "Something went wrong.", nick);
			}
		};
		list.setHelpText("Gives you a list of your next 3 reminders");
		reminders = new Command("reminders") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				list.onExecuteSuccess(command, nick, target, event, params);
			}
		};
		reminders.setHelpText(list.getHelpText());
		IRCBot.registerCommand(remind);
		IRCBot.registerCommand(remindSomeone);
		remind.registerSubCommand(list);

		Database.addStatement("CREATE TABLE IF NOT EXISTS Reminders(dest, nick, time, message)");
		Database.addPreparedStatement("addReminder", "INSERT INTO Reminders(dest, nick, time, message) VALUES (?,?,?,?);");
		Database.addPreparedStatement("getReminder", "SELECT dest, nick, time, message FROM Reminders WHERE time <= ?;");
		Database.addPreparedStatement("listReminders", "SELECT dest, nick, time, message FROM Reminders WHERE nick = ? ORDER BY time DESC LIMIT 3;");
		Database.addPreparedStatement("delReminder", "DELETE FROM Reminders WHERE time = ? AND nick = ?;");
		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		executor = ses.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					long epoch = System.currentTimeMillis();
					PreparedStatement getReminder = Database.getPreparedStatement("getReminder");
					getReminder.setLong(1, epoch);
					ResultSet results = getReminder.executeQuery();
					if (results.next()) {
						if (results.getString(1).equals("query")) {
							Helper.sendMessage(results.getString(2), "REMINDER: " + results.getString(4));
						} else {
							Helper.sendMessage(results.getString(1), results.getString(2) + " REMINDER: " + results.getString(4));
						}
						PreparedStatement delReminder = Database.getPreparedStatement("delReminder");
						delReminder.setLong(1, results.getLong(3));
						delReminder.setString(2, results.getString(2));
						delReminder.execute();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 0, 1, TimeUnit.SECONDS);
	}
}
