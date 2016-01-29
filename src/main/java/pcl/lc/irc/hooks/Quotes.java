package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

@SuppressWarnings("rawtypes")
public class Quotes extends AbstractListener {

	@Override
	protected void initCommands() {
		IRCBot.registerCommand("addquote");
		IRCBot.registerCommand("quote");
		IRCBot.registerCommand("delquote");
	}


	@Override
	public void handleCommand(String sender, final MessageEvent event, String command, String[] args) {
		String prefix = Config.commandprefix;
		System.out.println(command);
		if (command.equals(prefix + "quote") || command.equals(prefix + "q")) {
			if (args.length == 0) {
				try {
					PreparedStatement getAnyQuote = IRCBot.getInstance().getPreparedStatement("getAnyQuote");
					ResultSet results = getAnyQuote.executeQuery();
					if (results.next()) {
						IRCBot.bot.sendIRC().message(event.getChannel().getName(), sender + ": " + pcl.lc.utils.Helper.antiPing(results.getString(1)) + " " + results.getString(2));
					}
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (args.length == 1) {
				String key = args[0];
				try {
					PreparedStatement getQuote = IRCBot.getInstance().getPreparedStatement("getUserQuote");
					getQuote.setString(1, key);
					ResultSet results = getQuote.executeQuery();
					if (results.next()) {
						IRCBot.bot.sendIRC().message(event.getChannel().getName(), sender + ": " + pcl.lc.utils.Helper.antiPing(key) + " " + results.getString(1));
					} else {
						IRCBot.bot.sendIRC().message(event.getChannel().getName(), sender + ": " + "No quotes found for " + pcl.lc.utils.Helper.antiPing(key));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (command.equals(prefix + "addquote")) {
			if (args.length > 1) {
				String key = args[0];
				String data = StringUtils.join(args, " ", 1, args.length);
				try {
					PreparedStatement addQuote = IRCBot.getInstance().getPreparedStatement("addQuote");
					addQuote.setString(1, key);
					addQuote.setString(2, data);
					if (addQuote.executeUpdate() > 0) {
						IRCBot.bot.sendIRC().message(event.getChannel().getName(), sender + ": " + "Quote added.");
					} else {
						IRCBot.bot.sendIRC().message(event.getChannel().getName(), sender + ": " + "An error occurred while trying to set the value.");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (command.equals(prefix + "delquote")) {
			boolean isOp = IRCBot.getInstance().isOp(event.getBot(), event.getUser());
			if (isOp || event.getChannel().isOp(event.getUser())) {
				if (args.length > 1) {
					String key = args[0];
					String data = StringUtils.join(args, " ", 1, args.length);
					try {
						PreparedStatement addQuote = IRCBot.getInstance().getPreparedStatement("removeQuote");
						addQuote.setString(1, key);
						addQuote.setString(2, data);
						if (addQuote.executeUpdate() > 0) {
							event.respond("Quote removed.");
						} else {
							event.respond("An error occurred while trying to set the value.");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}