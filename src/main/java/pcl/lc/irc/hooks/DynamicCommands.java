/**
 * 
 */
package pcl.lc.irc.hooks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class DynamicCommands extends AbstractListener {
	@Override
	protected void initCommands() {
		IRCBot.registerCommand("addcommand", "Adds a dynamic command to the bot, requires BotAdmin, or Channel Op.");
		IRCBot.registerCommand("delcommand", "Removes a dynamic command to the bot, requires BotAdmin, or Channel Op.");
		try {
			PreparedStatement searchCommands = IRCBot.getInstance().getPreparedStatement("searchCommands");
			ResultSet commands = searchCommands.executeQuery();
			while (commands.next()) {
				IRCBot.registerCommand(commands.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
			IRCBot.log.info("An error occurred while processing this command");
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event,
			String command, String[] copyOfRange) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		String prefix = Config.commandprefix;
		String ourinput = event.getMessage().toLowerCase().trim();
		boolean isOp = IRCBot.getInstance().isOp(event.getBot(), event.getUser());
		if (ourinput.length() > 1) {
			if (!IRCBot.isIgnored(event.getUser().getNick())) {
				if (command.startsWith(prefix)) {
					String[] message = event.getMessage().split(" ", 3);

					if (command.equals(prefix + "addcommand") && (isOp || Helper.isChannelOp(event))) {
						try {
							PreparedStatement addCommand = IRCBot.getInstance().getPreparedStatement("addCommand");
							if (!IRCBot.commands.contains(message[1])) {
								addCommand.setString(1, message[1]);
								addCommand.setString(2, message[2]);
								addCommand.executeUpdate();
								event.respond("Command Added");
								IRCBot.registerCommand(message[1]);
							} else {
								event.respond("Can't override existing commands.");
							}
						} catch (Exception e) {
							e.printStackTrace();
							event.respond("An error occurred while processing this command");
						}
					} else if (command.equals(prefix + "delcommand") && (isOp || Helper.isChannelOp(event))) {
						try {
							PreparedStatement delCommand = IRCBot.getInstance().getPreparedStatement("delCommand");
							delCommand.setString(1, message[1]);
							delCommand.execute();
							event.respond("Command deleted");
							IRCBot.unregisterCommand(message[1]);
						} catch (Exception e) {
							e.printStackTrace();
							event.respond("An error occurred while processing this command");
						}
					} else {
						try {
							PreparedStatement getCommand = IRCBot.getInstance().getPreparedStatement("getCommand");						
							getCommand.setString(1, command.replace(prefix, ""));
							ResultSet command1 = getCommand.executeQuery();
							if(command1.next()){
								event.getBot().sendIRC().message(event.getChannel().getName(), Helper.antiPing(sender) + ": " + command1.getString(1));
							}
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("An error occurred while processing this command");
						}
					}
				}		
			}
		}
	}
}
