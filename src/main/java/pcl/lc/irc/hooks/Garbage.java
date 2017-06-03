package pcl.lc.irc.hooks;

import com.google.common.collect.Lists;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
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
		IRCBot.registerCommand(local_command, "It goes in the garbage");
	}

	private void initCommands() {
		local_command = new Command("garbage", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (params.equals("")) {
					Helper.sendAction(target, "kicks a can " + Helper.get_garbage());
				} else if (params.equals("^")) {
					List<Entry<UUID, List<String>>> list = new ArrayList<>(IRCBot.messages.entrySet());
					for (Entry<UUID, List<String>> entry : Lists.reverse(list)) {
						if (entry.getValue().get(0).equals(target)) {
							Helper.sendAction(target, "throws '" + entry.getValue().get(2) + "' " + Helper.get_garbage() + ", it was never seen again.");
							return;
						}
					}
				} else {
					Helper.sendAction(target, "throws '" + params + "' " + Helper.get_garbage() + ", it was never seen again.");
				}
			}
		};
		local_command.registerAlias("gb");
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		target = Helper.getTarget(event);
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}
