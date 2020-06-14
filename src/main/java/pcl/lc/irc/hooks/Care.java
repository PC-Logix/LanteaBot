/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.CommandRateLimit;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import java.util.ArrayList;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Care extends AbstractListener {
	private Command local_command;
	private double base_drop_chance = .20; // 0.08 is added for every item included

	@Override
	protected void initHook() {
		local_command = new Command("care", new CommandRateLimit(60)) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				Helper.sendMessage(target, Helper.getCareDetectorResponse(), nick);
			}
		};
		IRCBot.registerCommand(local_command, "Measure care levels");
		local_command.registerAlias("care-o-meter");
		local_command.registerAlias("careometer");
		local_command.registerAlias("doicare");
		local_command.registerAlias("howmuchcare");
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args, String callingRelay) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange, String callingRelay) {
		target = Helper.getTarget(event);
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}
