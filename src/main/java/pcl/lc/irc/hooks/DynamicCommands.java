/**
 * 
 */
package pcl.lc.irc.hooks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class DynamicCommands extends ListenerAdapter {

	public DynamicCommands() {
		IRCBot.registerCommand("addcommand");
		IRCBot.registerCommand("delcommand");
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

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);

		String prefix = Config.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		String[] firstWord = StringUtils.split(trigger);
		String triggerWord = firstWord[0];
		String firstCharacter = String.valueOf(triggerWord.charAt(0));
		boolean isOp = IRCBot.getInstance().isOp(event.getBot(), event.getUser());
		if (trigger.length() > 1) {
			if (!IRCBot.isIgnored(event.getUser().getNick())) {
				if (firstCharacter == prefix) {
					if (triggerWord.equals(prefix + "addcommand") && (isOp || Helper.isChannelOp(event))) {
						try {
							PreparedStatement addCommand = IRCBot.getInstance().getPreparedStatement("addCommand");
							String[] message = event.getMessage().split(" ", 3);

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
					} else if (triggerWord.equals(prefix + "delcommand") && (isOp || Helper.isChannelOp(event))) {
						try {
							PreparedStatement delCommand = IRCBot.getInstance().getPreparedStatement("delCommand");
							String[] message = event.getMessage().split(" ", 2);
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
							getCommand.setString(1, triggerWord.replace(prefix, ""));
							ResultSet command = getCommand.executeQuery();
							if(command.next()){
								event.respond(command.getString(1));
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
