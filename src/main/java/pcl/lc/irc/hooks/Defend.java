package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.ActionType;
import pcl.lc.utils.DiceRoll;
import pcl.lc.utils.DiceRollResult;
import pcl.lc.utils.Helper;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Defend extends AbstractListener {
	public static ArrayList<String> attackLog;

	private Command local_command;
	private HashMap<String, ActionType> actions;
	private static final String BLOCK = "block";
	private static final String GUARD = "guard";
	private static final String DEFLECT = "deflect";
	private static final String PARRY = "parry";
	private static final String DISPEL = "dispel";
	private static final String DODGE = "dodge";

	private static String actionList = "";

	@Override
	protected void initHook() {
		attackLog = new ArrayList<>();
		actions = new HashMap<>();
		actions.put(BLOCK, new ActionType("Blocking", "Blocking", "Block", "Blocked"));
		actions.put(GUARD, new ActionType("Guarding", "Guarding", "Guard", "Guarded against"));
		actions.put(DEFLECT, new ActionType("Deflecting", "Deflecting", "Deflect", "Deflected"));
		actions.put(PARRY, new ActionType("Parrying", "Parrying", "Parry", "Parried"));
		actions.put(DISPEL, new ActionType("Dispelling", "Dispelling", "Dispel", "Dispelled"));
		actions.put(DODGE, new ActionType("Dodging", "Dodging", "Dodge", "Dodged"));

		ArrayList<String> acts = new ArrayList<>();
		for (Map.Entry<String, ActionType> act : actions.entrySet())
			acts.add(act.getValue().actionNameWill);
		actionList = String.join(", ", acts);

		initCommands();
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("defend", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				if (params.size() == 0) {
					Helper.sendMessage(target, "Specify an action as the first parameter: " + actionList);
					return;
				}
				try {
					String method = params.remove(0);
					if (!actions.containsKey(method.toLowerCase())) {
						Helper.sendMessage(target, "Specify an action as the first parameter: " + actionList);
						return;
					}
					String[] attack = Helper.getAttackFor(nick);
					if (attack != null) {
						String attacker = attack[0];
						String attackTarget = attack[1];
						String time = attack[2];
						String damage = attack[3];
						String implement = attack[4];

						String implementString = implement.equals("") ? " attack" : implement + " attack";
						int result = new DiceRoll(20).getSum();
						if (result >= 14)
							Helper.sendMessage(target, nick + " is " + actions.get(method).actionNamePast.toLowerCase() + " " + attacker + "'s " + implementString + " and avoided all of the damage! (" + damage + ")");
						else if (result >= 8)
							Helper.sendMessage(target, nick + " managed to partially " + actions.get(method).actionNameWill.toLowerCase() + " " + attacker + "'s " + implementString + " and takes half damage. (" + Math.floor(Integer.parseInt(damage) / 2) + ")");
						else
							Helper.sendMessage(target, nick + " failed to " + actions.get(method).actionNameWill.toLowerCase() + " " + attacker + "'s " + implementString + " and takes all of the damage. (" + damage + ")");
						Helper.clearAttackFor(nick);
					} else {
						Helper.sendMessage(target, "There's nothing to defend against, or you were too slow...");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					Helper.sendMessage(target, "Something went wrong...");
				}
			}
		};
		local_command.setHelpText("Defend against attacks.");
		for (String action : actions.keySet()) {
			local_command.registerAlias(action, action);
		}
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
