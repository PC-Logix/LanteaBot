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
	private String chan;

	@Override
	protected void initCommands() {
		IRCBot.registerCommand("8ball", "Gives vauge answers to vauge questions.");
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (command.equals(Config.commandprefix + "rot13")) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		String prefix = Config.commandprefix;
		if (command.equals(prefix + "8ball")) {
			if (!IRCBot.isIgnored(nick)) {
				String message = "";
				for( int i = 0; i < copyOfRange.length; i++)
				{
					message = message + " " + copyOfRange[i];
				}
				String target;
				if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
					target = nick;
				} else {
					target = chan;
				}
				if (message.length() > prefix.length() + "8ball".length()) {
					Random generator = new Random();
					String[] ballmessages = new String[] {"Signs point to yes", "Without a doubt", "Reply hazy, try again", "Ask again later", "My reply is no", "Outlook not so good"};
					int randommessage = generator.nextInt( 4 );
					IRCBot.getInstance().sendMessage(target, ballmessages[randommessage]);
				}
			}
		}
	}
}
