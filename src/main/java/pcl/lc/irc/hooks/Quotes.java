package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
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
		IRCBot.registerCommand("addquote", "Adds a quote to the database (Requires BotAdmin, or Channel Op");
		IRCBot.registerCommand("quote", "Returns quotes from the quote database");
		IRCBot.registerCommand("delquote", "Removes a quote from the database (Requires BotAdmin, or Channel Op");
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
						IRCBot.bot.sendIRC().message(event.getChannel().getName(), "Quote #" + results.getString(1) + ": <" + pcl.lc.utils.Helper.antiPing(results.getString(2)) + "> " + results.getString(3));
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
						IRCBot.bot.sendIRC().message(event.getChannel().getName(), "Quote #" + results.getString(1) + ": <" + pcl.lc.utils.Helper.antiPing(key) + "> " + results.getString(2));
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
						IRCBot.bot.sendIRC().message(event.getChannel().getName(), sender + ": " + "Quote added at id: " + addQuote.getGeneratedKeys().getInt(1) );
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
				if (args.length == 1) {
					String key = args[0];
					//String data = StringUtils.join(args, " ", 1, args.length);
					try {
						PreparedStatement removeQuote = IRCBot.getInstance().getPreparedStatement("removeQuote");
						removeQuote.setString(1, key);
						//removeQuote.setString(2, data);
						if (removeQuote.executeUpdate() > 0) {
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


	@Override
	public void handleCommand(String nick, GenericMessageEvent event,
			String command, String[] copyOfRange) {
		// TODO Auto-generated method stub
		
	}
}