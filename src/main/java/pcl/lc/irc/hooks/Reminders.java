package pcl.lc.irc.hooks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.pircbotx.Colors;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

public class Reminders extends AbstractListener {

	public String dest;

	public String chan;

	@Override
	protected void initCommands() {
		IRCBot.registerCommand("remindme", "Syntax: " + Config.commandprefix + "remindme [time] [message] Ex: \"" + Config.commandprefix + "remindme 1h20m check your food!\" Will send a reminder in 1 hour and 20 minutes in the channel the command was sent (or PM if you PMed the bot)");
		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		ses.scheduleAtFixedRate(new Runnable() {
		    @Override
		    public void run() {
				try {
					long epoch = System.currentTimeMillis();
					PreparedStatement getReminder = IRCBot.getInstance().getPreparedStatement("getReminder");
					getReminder.setLong(1, epoch);
					ResultSet results = getReminder.executeQuery();
					if (results.next()) {
						if (results.getString(1).equals("query")) {
							IRCBot.getInstance().sendMessage(results.getString(2), results.getString(4));
						} else {
							IRCBot.getInstance().sendMessage(results.getString(1), "REMINDER " + results.getString(2) + " " + results.getString(4));
						}
						PreparedStatement delReminder = IRCBot.getInstance().getPreparedStatement("delReminder");
						delReminder.setLong(1, results.getLong(3));
						delReminder.setString(2, results.getString(2));
						delReminder.execute();
					}
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
		    }
		}, 0, 10, TimeUnit.SECONDS);
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub
		//event.respond("ME" + event.getClass().getName() + " " + event.getMessage());
		if (command.equals(Config.commandprefix + "remindme")) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (command.equals(Config.commandprefix + "remindme")) {
			String message = "";
			if (event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
				dest = chan;
			} else {
				dest = "query";
			}

			for( int i = 1; i < copyOfRange.length; i++)
			{
				message = message + " " + copyOfRange[i];
			}

			long time = addReminder(dest, copyOfRange[0], message);
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
			String newTime = sdf.format(new Date(time));
			String target = null;
			if (dest.equals("query")) {
				target = nick;
			} else {
				target = dest;
			}
			try {
				PreparedStatement addReminder = IRCBot.getInstance().getPreparedStatement("addReminder");
				addReminder.setString(1, dest);
				addReminder.setString(2, nick);
				addReminder.setLong(3, time);
				addReminder.setString(4, message.trim());
				if (addReminder.executeUpdate() > 0) {
					IRCBot.getInstance().sendMessage(target, "I'll remind you about \"" + message.trim() + "\" at " + newTime);
				} else {
					IRCBot.getInstance().sendMessage(target, "Error");
				}
			} catch (Exception e) {
				IRCBot.getInstance().sendMessage(target, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public long addReminder(String dest, String time, String message) {
		PeriodFormatter formatter = new PeriodFormatterBuilder()
				.appendDays().appendSuffix("d")
				.appendHours().appendSuffix("h")
				.appendMinutes().appendSuffix("m")
				.appendSeconds().appendSuffix("s")
				.toFormatter();

		Period p = formatter.parsePeriod(time);
		long millis = p.toStandardDuration().getMillis();
		long epoch = System.currentTimeMillis();
		return(millis + epoch);
	}
}
