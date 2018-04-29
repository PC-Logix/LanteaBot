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

import java.util.ArrayList;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Attack extends AbstractListener {
	private Command local_command;
	private ArrayList<String> actions;

	@Override
	protected void initHook() {
		actions = new ArrayList<>();
		// New entries should be things that can end with "s" since that is automatically added when sending the action
		// eg. stab => stabs
		actions.add("stab");
		actions.add("hit");
		actions.add("shiv");
		actions.add("strike");
		actions.add("slap");
		actions.add("poke");
		actions.add("prod");

		local_command = new Command("attack", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				if (params.size() == 0) {
					Helper.sendMessage(target, "Valid \"attacks\": " + actions.toString().replace("[", "").replace("]", ""));
					return;
				}
				try
				{
					String method = params.remove(0);
					String message = "";
					for (String aParam : params)
					{
						message += " " + aParam;
					}

					Item item = Inventory.getRandomItem(false);
					String dust = "";
					if (item != null) {
						dust = item.decrementUses();
					}

					int action;
					if (actions.indexOf(method.toLowerCase()) != -1)
						action = actions.indexOf(method.toLowerCase());
					else {
						Helper.sendMessage(target, "Valid \"attacks\": " + actions.toString().replace("[", "").replace("]", ""));
						return;
					}
					//action = Helper.getRandomInt(0, actions.size() - 1);

					String attackTarget = message.trim();

					if (attackTarget == "")
						Helper.sendAction(target,"flails at nothingness" + (item != null ? " with " + item.getName() : ""));
					else if (Helper.doInteractWith(attackTarget)) {
						Helper.AntiPings = Helper.getNamesFromTarget(target);
						int[] dmg = {0,0,0};
						if (item != null)
							dmg = item.getDamage();
						Helper.sendAction(target,Helper.antiPing(actions.get(action)) + "s " + attackTarget + (item != null ? " with " + item.getName() : "") + " doing " + Item.stringifyDamageResult(dmg) + dust);
					} else {
						Helper.AntiPings = Helper.getNamesFromTarget(target);
						Helper.sendAction(target,"uses " + (item != null ? item.getName() : Helper.parseSelfReferral("his") + " orbital death ray") + " to vaporize " + Helper.antiPing(nick) + dust);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		IRCBot.registerCommand(local_command, "Attack things with things. First argument is attack type: " + actions.toString().replace("[","").replace("]","") + " or random if invalid.");
		for (String action : actions) {
			local_command.registerAlias(action, action);
		}
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
