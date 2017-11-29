/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;
import pcl.lc.utils.Item;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Fling extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command, "Fling");
	}

	private void initCommands() {
		local_command = new Command("fling", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Item item = Inventory.getRandomItem(false);

				if (item != null) {
					String user = params;
					if (user == "")
						user = Helper.getRandomUser(event);
					Helper.AntiPings = Helper.getNamesFromTarget(target);
					Helper.sendAction(target, "flings " + item.getName() + " in a random direction. It hits " + user + " " + Helper.get_hit_place() + ". They take " + Helper.rollDiceString("1d6") + " damage.");
					String dust = item.decrementUses(false);
					if (dust != "")
						Helper.sendAction(target, dust);
				} else {
					Helper.sendAction(target, "makes a flinging motion but realizes there was nothing there...");
				}
			}
		};
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
