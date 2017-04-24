package pcl.lc.irc.hooks;

import java.util.Random;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings({ "rawtypes" })
public class EightBall extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		local_command = new Command("8ball", 0);
		IRCBot.registerCommand(local_command, "Gives vauge answers to vauge questions.");
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (local_command.shouldExecute(command, event) >= 0) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		long shouldExecute = local_command.shouldExecute(command, event);
		if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
			target = nick;
		} else {
			target = chan;
		}
		if (shouldExecute == 0) {
			String message = "";
			for( int i = 0; i < copyOfRange.length; i++)
			{
				message = message + " " + copyOfRange[i];
			}
			if (message.length() > 6) {
				Random generator = new Random();
				String[] ballmessages = new String[] {"Signs point to yes", "Without a doubt", "Reply hazy, try again", "Ask again later", "My reply is no", "Outlook not so good"};
				int randommessage = generator.nextInt( 4 );
				Helper.sendMessage(target, Helper.antiPing(nick) + ": " +  ballmessages[randommessage]);
			}
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