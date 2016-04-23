package pcl.lc.irc.hooks;

import java.util.Random;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings({ "rawtypes" })
public class EightBall extends AbstractListener {
	@Override
	protected void initCommands() {
		IRCBot.registerCommand("8ball", "Gives vauge answes to vauge questions.");
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		String prefix = Config.commandprefix;
		if (command.length() > 1) {
			if (command.equals(prefix + "8ball")) {
				if (!IRCBot.isIgnored(event.getUser().getNick())) {
					if (args.length > prefix.length() + "8ball".length()) {
						Random generator = new Random();
						String[] ballmessages = new String[] {"Signs point to yes", "Without a doubt", "Reply hazy, try again", "Ask again later", "My reply is no", "Outlook not so good"};
						int randommessage = generator.nextInt( 4 );
						event.getBot().sendIRC().message(event.getChannel().getName(), Helper.antiPing(sender) + ": " + ballmessages[randommessage]);
					}
				}
			}			
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		System.out.println(command);
		
	}
}
