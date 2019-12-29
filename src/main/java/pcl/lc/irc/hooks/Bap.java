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
public class Bap extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("bap", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (params.length() == 0)
					Helper.sendAction(target, "flails at the darkness");
				else {
					String[] split = params.split(" with ", 2);
					String bapTarget = split[0].trim();
					String with = null;
					if (split.length > 1)
						with = split[1].trim();

					if (Helper.doInteractWith(params)) {
						Item item = null;
						if (with == null)
							item = Inventory.getRandomItem(false);
						else {
							try {
								item = new Item(with, false);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						if (item != null)
							if (nick.equals(bapTarget))
								Helper.sendAction(target, nick + " baps themselves with " + item.getName(true) + "!");
							else
								Helper.sendAction(target, nick + " baps " + bapTarget + " with " + item.getName(true) + "!");
						else {
							if (nick.equals(bapTarget))
								Helper.sendAction(target, nick + " baps themselves!");
							else
								Helper.sendAction(target, nick + " baps " + bapTarget + "!");
						}
					} else {
						Helper.sendAction(target, "smacks " + nick + "!");
					}
				}
			}
		};
		local_command.setHelpText("Bap a fool harmlessly");
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
