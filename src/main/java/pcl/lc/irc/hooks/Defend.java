package pcl.lc.irc.hooks;

import org.jvnet.inflector.Pluralizer;
import org.pircbotx.hooks.Event;
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
	public static HashMap<String, DefendEvent> defendEventLog;

	private static String damageFormat = "#";
	private static final int reactionTimeMinutes = 8;

	public enum EventTypes {
		ATTACK(12),
		PET(8),
		POTION(14),
		MISC(10),
		FLING(10);

		private final int baseDC;

		EventTypes(int dc) { this.baseDC = dc; }
	}

	private enum Actions {
		BLOCK("block", new ActionType("Blocking", "Blocking", "Block", "Blocked")),
		GUARD("guard", new ActionType("Guarding", "Guarding", "Guard", "Guarded against")),
		DEFLECT("deflect", new ActionType("Deflecting", "Deflecting", "Deflect", "Deflected")),
		PARRY("parry", new ActionType("Parrying", "Parrying", "Parry", "Parried")),
		COUNTERSPELL("counterspell", new ActionType("Counterspell", "Counterspelling", "Counterspell", "Counterspelled")),
		DODGE("dodge", new ActionType("Dodging", "Dodging", "Dodge", "Dodged")),
		FLAIL(null, new ActionType("Flailing", "Flailing", "Flail", "Flailed"));

		private final String command;
		private ActionType type;

		Actions(String command, ActionType type) { this.command = command; this.type = type; }
	}

	private Command local_command;

	private static String actionList = "";

	@Override
	protected void initHook() {
		defendEventLog = new HashMap<>();

		ArrayList<String> acts = new ArrayList<>();
		for (Actions act : Actions.values())
			if (act.command != null)
				acts.add(act.command);
		actionList = String.join(", ", acts);

		initCommands();
		IRCBot.registerCommand(local_command);
	}

	private Actions getActionByType(String type) {
		try {
			return Actions.valueOf(type.toUpperCase());
		} catch (Exception ignored) {}
		return Actions.FLAIL;
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
					if (!actionList.contains(method.toLowerCase())) {
						Helper.sendMessage(target, "Specify an action as the first parameter: " + actionList);
						return;
					}
					DefendEvent attack = getEventFor(nick, reactionTimeMinutes);
					if (attack != null) {
						int damage = attack.damage;
						String implement = attack.implement;
						Item implementItem = null;
						if (!implement.equals(""))
							implementItem = new Item(implement, false);

						DecimalFormat dec = new DecimalFormat(damageFormat);

						Item defenseItem = null;
						if (params.size() > 0) {
							if (params.get(0).equals("with"))
								params.remove(0);
							defenseItem = new Item(String.join(" ", params), false);
						}

						DiceRollBonusCollection attackBonus = DiceRollBonusCollection.getOffensiveItemBonus(implement);
						int dc = attack.type.baseDC + attackBonus.getTotal();
						String dcString = attackBonus.toString();
						dcString = dc + (dcString.equals("") ? "" : " (" + dcString + ")");

						DiceRollBonusCollection defenseBonus = new DiceRollBonusCollection();
						if (defenseItem != null)
							DiceRollBonusCollection.getDefensiveItemBonus(defenseItem);
						int result = new DiceRoll(20).getSum() + defenseBonus.getTotal();
						String resultString = defenseBonus.toString();
						resultString = result + (resultString.equals("") ? "" : " (" + resultString + ")");
						if (attack.type == EventTypes.ATTACK) {
							String implementString = implementItem == null ? "" : "wielding " + implementItem.getName(true);
							if (result >= (dc + 5)) {
								Helper.sendMessage(target, nick + " successfully " + getActionByType(method).type.actionNamePast.toLowerCase() + " " + attack.triggerer + " " + implementString + (defenseItem == null ? "" : " using " + defenseItem.getName(true)) + ". With " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + " " + nick + " avoided all of the damage! (" + dec.format(damage) + ")");
							} else if (result >= dc) {
								damage = (int) Math.max(1, Math.floor(damage / 2d));
								Helper.sendMessage(target, nick + " managed to partially " + getActionByType(method).type.actionNameWill.toLowerCase() + " " + attack.triggerer + " " + implementString + (defenseItem == null ? "" : " using " + defenseItem.getName(true)) + ". With " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + " " + nick + " only takes half damage. (" + dec.format(damage) + ")");
							} else {
								Helper.sendMessage(target, nick + " failed to " + getActionByType(method).type.actionNameWill.toLowerCase() + " " + attack.triggerer + " " + implementString + (defenseItem == null ? "" : " using " + defenseItem.getName(true)) + ". With " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + " " + nick + " takes all of the damage. (" + dec.format(damage) + ")");
							}
						} else if (attack.type == EventTypes.POTION) {
							if (result >= dc) {
								String altTarget = Helper.getRandomTransformation(true, true, false, true);
								AppearanceEntry con = PotionHelper.findConsistencyInString(implement);
								AppearanceEntry app = PotionHelper.findAppearanceInString(implement);
								String potionString = "";
								if (app != null && con != null) {
									System.out.println("App: '" + app.Name + "', Con: '" + con.Name + "'");
									EffectEntry effectEntry = PotionHelper.getCombinationEffect(con, app);
									if (effectEntry != null) {
										String[] prefix = Helper.solvePrefixes(altTarget);
										if (prefix != null)
											altTarget = "the " + prefix[1];
										String effectString = PotionHelper.replaceParamsInEffectString(effectEntry.Effect, altTarget);
										potionString = " " + effectString.substring(0, 1).toUpperCase() + effectString.substring(1);
									}
								}
								Helper.sendMessage(target, nick + " manages to " + getActionByType(method).type.actionNameWill.toLowerCase() + " the " + attack.implement + " " + attack.triggerer + " threw with " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + ". It splashes onto " + altTarget + " that was standing next to you." + potionString);
							} else
								Helper.sendMessage(target, nick + " fails to " + getActionByType(method).type.actionNameWill + " the " + attack.implement + " " + attack.triggerer + " threw with " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + ".");
						} else if (attack.type == EventTypes.FLING) {
							if (result >= dc + 5) {
								Helper.sendMessage(target, nick + " successfully " + getActionByType(method).type.actionNamePast.toLowerCase() + " the " + attack.implement + " flung at them by " + attack.triggerer + " with " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + ", avoiding all the damage (" + dec.format(damage) + ")");
							} else if (result >= dc) {
								damage = (int) Math.max(1, Math.floor(damage / 2d));
								Helper.sendMessage(target, nick + " successfully " + getActionByType(method).type.actionNamePast.toLowerCase() + " the " + attack.implement + " flung at them by " + attack.triggerer + " with " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + ", taking only half damage. (" + dec.format(damage) + ")");
							} else {
								Helper.sendMessage(target, nick + " fails to " + getActionByType(method).type.actionNameWill.toLowerCase() + " the " + attack.implement + " flung at them by " + attack.triggerer + " with " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + ", taking all of the damage. (" + dec.format(damage) + ")");
							}
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
		local_command.setHelpText("Defend against things! Getting stabbed? Things thrown at you? No problem! Just defend!");
		for (Actions action : Actions.values()) {
			if (action.command != null)
				local_command.registerAlias(action.command, action.command);
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
	public static void addEvent(String trigger, String target, int damage, String implement, EventTypes type) {
		ArrayList<String> newEventData = new ArrayList<>();
		if (eventExistsFor(target))
			defendEventLog.remove(target);
		defendEventLog.put(target, new DefendEvent(trigger, target, new Date(), damage, implement, type));
	}

	public static void addEvent(String trigger, String target, String implement, EventTypes type) {
		addEvent(trigger, target, 0, implement, type);
	}

	public static DefendEvent getEventFor(String player) {
		return getEventFor(player, 2);
	}

	public static DefendEvent getEventFor(String player, int maxReactionTimeMinutes) {
		if (defendEventLog.containsKey(player)) {
			DefendEvent event = defendEventLog.get(player);
			Date now = new Date(new Date().getTime() - (maxReactionTimeMinutes * 60 * 1000));
			if (event.time.after(now))
				return defendEventLog.get(player);
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
		defendEventLog.remove(player);
	}
}
