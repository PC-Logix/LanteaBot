package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.*;
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
	private Command list;
	private Command reminders;
	private ScheduledFuture<?> executor;
	@Override
	protected void initHook() {
		remind = new Command("remind", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				if (params.size() > 1) {
					long time = Helper.getFutureTime(params.get(0));
					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
					String newTime = sdf.format(new Date(time));
					String message = "";
					for (int i = 1; i < params.size(); i++) {
						message += " " + params.get(i);
					}
					message = message.trim();
					try {
						PreparedStatement addReminder = Database.getPreparedStatement("addReminder");
						addReminder.setString(1, target);
						if (nick.equals("Corded")) {
							nick = "@" + nick;
						}
						addReminder.setString(2, nick.replaceAll("​", ""));
						addReminder.setLong(3, time);
						addReminder.setString(4, message.trim());
						if (addReminder.executeUpdate() > 0) {
							Helper.sendMessage(target, "I'll remind you about \"" + message.trim() + "\" at " + newTime);
							return;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					Helper.sendMessage(target, "Something went wrong", nick);
				} else if (params.size() == 0) {
					Helper.sendMessage(target, "Specify time (eg 1h20m10s)", nick);
				} else if (params.size() == 1) {
					Helper.sendMessage(target, "Specify a remind message after the time.", nick);
				}
			}
		};
		remind.registerAlias("remindme");
		remind.setHelpText("'remindme 1h20m check your food!' Will send a reminder in 1 hour and 20 minutes in the channel the command was sent (or PM if you PMed the bot)");
		list = new Command("list", 0, true) {
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
		reminders = new Command("reminders", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				list.onExecuteSuccess(command, nick, target, event, params);
			}
		};
		reminders.setHelpText(list.getHelpText());
		IRCBot.registerCommand(remind);
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

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		target = Helper.getTarget(event);
		remind.tryExecute(command, nick, target, event, copyOfRange);
		reminders.tryExecute(command, nick, target, event, copyOfRange);
	}

	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {}
}
