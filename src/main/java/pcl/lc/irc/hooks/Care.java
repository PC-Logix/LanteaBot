/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.entryClasses.CommandRateLimit;
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

	@Override
	protected void initHook() {
		local_command = new Command("care", new CommandRateLimit(60)) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				Helper.sendMessage(target, Helper.getCareDetectorResponse(), nick);
			}
		};
		local_command.registerAlias("care-o-meter");
		local_command.registerAlias("careometer");
		local_command.registerAlias("doicare");
		local_command.registerAlias("howmuchcare");
		local_command.setHelpText("Measure care levels");
		IRCBot.registerCommand(local_command);
	}
}
