package pcl.lc.irc.hooks;

import com.google.common.collect.Lists;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Garbage extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("garbage") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (params.equals("")) {
					Helper.sendAction(target, "kicks a can " + Helper.getGarbageDisposal());
				} else if (params.equals("^")) {
					List<Entry<UUID, List<String>>> list = new ArrayList<>(IRCBot.messages.entrySet());
					for (Entry<UUID, List<String>> entry : Lists.reverse(list)) {
						if (entry.getValue().get(0).equals(target)) {
							Helper.sendAction(target, "throws '" + entry.getValue().get(2) + "' " + Helper.getGarbageDisposal() + ", it was never seen again.");
							return;
						}
					}
				} else {
					Helper.sendAction(target, "throws '" + params + "' " + Helper.getGarbageDisposal() + ", it was never seen again.");
				}
			}
		};
		local_command.registerAlias("gb");
		local_command.setHelpText("It goes in the garbage");
	}
}
