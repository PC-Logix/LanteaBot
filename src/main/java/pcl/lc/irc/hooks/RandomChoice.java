package pcl.lc.irc.hooks;

import java.util.Random;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

public class RandomChoice extends AbstractListener {
	private String chan;

	@Override
	protected void initCommands() {
		IRCBot.registerCommand("choose", "Randomly picks a choice for you.");
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		String prefix = Config.commandprefix;
		if (command.equals(prefix + "choose")) {
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
			String[] parts = message.split(" or ");
		    int rnd = new Random().nextInt(parts.length);
			IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " + parts[rnd].trim());
		}
	}
	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		// TODO Auto-generated method stub
		
	}
}
