package pcl.lc.irc.hooks;

import org.jvnet.inflector.Pluralizer;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Defend extends AbstractListener {
	public static ArrayList<String> defendEventLog;

	private static String damageFormat = "#";
	private static final int reactionTimeMinutes = 2;

	private Command local_command;
	private HashMap<String, ActionType> actions;
	private static final String BLOCK = "block";
	private static final String GUARD = "guard";
	private static final String DEFLECT = "deflect";
	private static final String PARRY = "parry";
	private static final String COUNTERSPELL = "counterspell";
	private static final String DODGE = "dodge";

	private static String actionList = "";

	@Override
	protected void initHook() {
		defendEventLog = new ArrayList<>();
		actions = new HashMap<>();
		actions.put(BLOCK, new ActionType("Blocking", "Blocking", "Block", "Blocked"));
		actions.put(GUARD, new ActionType("Guarding", "Guarding", "Guard", "Guarded against"));
		actions.put(DEFLECT, new ActionType("Deflecting", "Deflecting", "Deflect", "Deflected"));
		actions.put(PARRY, new ActionType("Parrying", "Parrying", "Parry", "Parried"));
		actions.put(COUNTERSPELL, new ActionType("Counterspell", "Counterspelling", "Counterspell", "Counterspelled"));
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
					String[] attack = getEventFor(nick);
					if (attack != null) {
						String eventTriggerer = attack[0];
						String eventTarget = attack[1];
						String time = attack[2];
						int damage = Integer.parseInt(attack[3]);
						String implement = attack[4];
						Item implementItem = null;
						if (!implement.equals(""))
							implementItem = new Item(implement, false);
						String type = attack[5];

						DecimalFormat dec = new DecimalFormat(damageFormat);

						Item defenseItem = null;
						if (params.size() > 0) {
							if (params.get(0).equals("with"))
								params.remove(0);
							defenseItem = new Item(String.join(" ", params), false);
						}

						int baseDC = 10;

//						String implementString = implement.equals("") ? " attack" : implement + " attack";
						String implementWield = "";
						if (type.equals("attack")) {
							baseDC = 10;
							implementWield = "wielding ";
						}
						String implementString = implementItem == null ? "" : implementWield + implementItem.getName(true);

						String avoidString = "";
						if (type.equals("attack"))
							avoidString = "damage";

						DiceRollBonusCollection attackBonus = DiceRollBonusCollection.getOffensiveItemBonus(implement);
						int dc = baseDC + attackBonus.getTotal();
						String dcString = attackBonus.toString();
						dcString = dc + (dcString.equals("") ? "" : " (" + dcString + ")");

						DiceRollBonusCollection defenseBonus = new DiceRollBonusCollection();
						if (defenseItem != null)
							DiceRollBonusCollection.getDefensiveItemBonus(defenseItem);
						int result = new DiceRoll(20).getSum() + defenseBonus.getTotal();
						String resultString = defenseBonus.toString();
						resultString = result + (resultString.equals("") ? "" : " (" + resultString + ")");
						if (result >= (dc + 5)) {
							Helper.sendMessage(target, nick + " successfully " + actions.get(method).actionNamePast.toLowerCase() + " " + eventTriggerer + " " + implementString + (defenseItem == null ? "" : " using " + defenseItem.getName(true)) + ". With " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + " " + nick + " avoided all of the " + avoidString + "! (" + dec.format(damage) + ")");
						} else if (result >= dc) {
							damage = (int) Math.max(1, Math.floor(damage / 2d));
							Helper.sendMessage(target, nick + " managed to partially " + actions.get(method).actionNameWill.toLowerCase() + " " + eventTriggerer + " " + implementString + (defenseItem == null ? "" : " using " + defenseItem.getName(true)) + ". With " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + " " + nick + " only takes half " + avoidString + ". (" + dec.format(damage) + ")");
						} else {
							damage = 0;
							Helper.sendMessage(target, nick + " failed to " + actions.get(method).actionNameWill.toLowerCase() + " " + eventTriggerer + " " + implementString + (defenseItem == null ? "" : " using " + defenseItem.getName(true)) + ". With " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + " " + nick + " takes all of the " + avoidString + ". (" + dec.format(damage) + ")");
						}
						clearEventFor(nick);
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

	/**
	 *
	 * @param trigger String
	 * @param target String
	 * @param damage int
	 * @param implement String
	 * @param type String Supported types are `attack`, & `pet`
	 */
	public static void addEvent(String trigger, String target, int damage, String implement, String type) {
		ArrayList<String> newEventData = new ArrayList<>();
		if (!eventExistsFor(target)) {
			defendEventLog.add(trigger + "," + target + "," + new Date().getTime() + "," + damage + "," + implement + "," + type);
		} else {
			for (String att : defendEventLog) {
				String[] event = att.split(",");
				String dataTrigger = event[0];
				String dataTarget = event[1];
				String dataTime = event[2];
				String dataDamage = event[3];
				String dataImplement = event[4];
				String dataType = event[5];
				if (!dataTarget.equals(target)) {
					newEventData.add(dataTrigger + "," + dataTarget + "," + dataTime + "," + dataDamage + "," + dataImplement + "," + dataType);
				}
			}
			newEventData.add(trigger + "," + target + "," + new Date().getTime() + "," + damage + "," + implement + "," + type);
			defendEventLog = newEventData;
		}
	}

	public static String[] getEventFor(String player) {
		return getEventFor(player, 2);
	}

	public static String[] getEventFor(String player, int maxReactionTimeMinutes) {
		for (String att : defendEventLog) {
			String[] event = att.split(",");
			String trigger = event[0];
			String target = event[1];
			String time = event[2];
			String damage = event[3];
			String implement = event[4];
			String type = event[5];
			if (target.equals(player)) {
				Date date = new Date();
				date.setTime(Long.parseLong(time));
				Date now = new Date(new Date().getTime() - (maxReactionTimeMinutes * 60 * 1000));
				if (date.after(now)) {
					return new String[] {trigger, target, time, damage, implement, type};
				}
			}
		}
		return null;
	}

	public static boolean eventExistsFor(String player) {
		return eventExistsFor(player, 2);
	}

	public static boolean eventExistsFor(String player, int maxReactionTimeMinutes) {
		return getEventFor(player, maxReactionTimeMinutes) != null;
	}

	public static void clearEventFor(String player) {
		ArrayList<String> newEventData = new ArrayList<>();
		for (String att : defendEventLog) {
			String[] event = att.split(",");
			String dataTrigger = event[0];
			String dataTarget = event[1];
			String dataTime = event[2];
			String dataDamage = event[3];
			String dataImplement = event[4];
			String dataType = event[5];
			if (!dataTarget.equals(player)) {
				newEventData.add(dataTrigger + "," + dataTarget + "," + dataTime + "," + dataDamage + "," + dataImplement + "," + dataType);
			}
		}
		defendEventLog = newEventData;
	}
}
