package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.DiceRoll;
import pcl.lc.utils.Helper;
import pcl.lc.utils.Item;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Bonk extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("bonk") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (params.length() == 0)
					Helper.sendWorldAction(target, nick + " swings at the void");
				else {
					String[] split = params.split(" with ", 2);
					String bonkTarget = split[0].trim();
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

						if (with != null && with.equals("nothing"))
						    item = null;

						String dmgString;
						if (item == null)
							dmgString = "1d4 damage";
						else
							dmgString = "1d" + item.getDiceSizeFromItemName() + " damage";
						dmgString = DiceRoll.rollDiceInString(dmgString, true);

						if (item != null)
							if (nick.equals(bonkTarget))
								Helper.sendMessage(target, nick + " bonks themselves on the head with " + item.getName(true) + " for " + dmgString + "!");
							else
								Helper.sendMessage(target, nick + " bonks " + bonkTarget + " on the head with " + item.getName(true) + " for " + dmgString + "!");
						else {
							if (nick.equals(bonkTarget))
								Helper.sendMessage(target, nick + " baps themselves on the head for " + dmgString + "!");
							else
								Helper.sendMessage(target, nick + " bonks " + bonkTarget + " on the head for " + dmgString + "!");
						}
					} else {
						Helper.sendAction(target, "bonks " + nick + " on the head preemptively!");
					}
				}
			}
		};
		local_command.setHelpText("Hit someone on the head! Syntax: " + Config.commandprefix + local_command.getCommand() + " <target> [with <item>] If [with <item>] is omitted tries to use a random item from the inventory.");
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
