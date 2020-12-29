package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.*;
import pcl.lc.utils.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Forecaster
 */
@SuppressWarnings("rawtypes")
public class Defend extends AbstractListener {
	public static ArrayList<DefendEvent> defendEventLog;
	private ScheduledFuture<?> executor;

	private static final String damageFormat = "#";
	public static final int reactionTimeMinutes = 5;

	public static String getReactionTimeString() {
		return reactionTimeMinutes + " minute" + (reactionTimeMinutes == 1 ? "" : "s");
	}

	public enum EventTypes {
		ATTACK(12),
		PET(8),
		POTION(14),
		MISC(10),
		FLING(10);

		private final int baseDC;

		EventTypes(int dc) {
			this.baseDC = dc;
		}
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

		Actions(String command, ActionType type) {
			this.command = command;
			this.type = type;
		}
	}

	private Command local_command;

	private static String actionList = "";

	@Override
	protected void initHook() {
		defendEventLog = new ArrayList<>();

		ArrayList<String> acts = new ArrayList<>();
		for (Actions act : Actions.values())
			if (act.command != null)
				acts.add(act.command);
		actionList = String.join(", ", acts);

		initCommands();
		IRCBot.registerCommand(local_command);

		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		executor = ses.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				Date expiredIfAfter = new Date(new Date().getTime() - (reactionTimeMinutes * 60 * 1000));
				for (DefendEvent event : defendEventLog) {
					try {
						if (expiredIfAfter.after(event.time)) {
							Helper.sendMessage(event.target, event.result);
							defendEventLog.remove(event);
						}
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						String pasteURL = PasteUtils.paste(sw.toString(), PasteUtils.Formats.NONE);
						Helper.sendMessage("#MichiBot", "An exception occurred parsing a scheduled defend entry: " + pasteURL);
						System.out.println("An exception occurred parsing a scheduled defend entry:");
						e.printStackTrace();
					}
				}
			}
		}, 0, 15, TimeUnit.SECONDS);
	}

	private Actions getActionByType(String type) {
		try {
			return Actions.valueOf(type.toUpperCase());
		} catch (Exception ignored) {
		}
		return Actions.FLAIL;
	}

	private void initCommands() {
		local_command = new Command("defend", new CommandArgumentParser(0, new CommandArgument("Action", "String"), new CommandArgument("Item", "String"))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws Exception {
				String method = this.argumentParser.getArgument("Action");
				if (method == null || !actionList.contains(method.toLowerCase())) {
					Helper.sendMessage(target, "Specify an action as the first parameter: " + actionList);
					return;
				}
				ArrayList<DefendEvent> defendEvents = getEventsFor(nick);
				if (defendEvents.size() > 0) {
					DefendEvent defendEvent = defendEvents.get(0);
					defendEventLog.remove(defendEvent);

					int damage = defendEvent.damage;
					int effectiveDamage = defendEvent.damage;
					String implement = defendEvent.implement;
					Item implementItem = null;
					if (!implement.equals(""))
						implementItem = new Item(implement, false);

					DecimalFormat dec = new DecimalFormat(damageFormat);

					String item = this.argumentParser.getArgument("Item");
					Item defenseItem = null;
					if (item != null) {
						defenseItem = new Item(String.join(" ", params), false);
					}

					DiceRollBonusCollection attackBonus = DiceRollBonusCollection.getOffensiveItemBonus(implement);
					int dc = defendEvent.type.baseDC + attackBonus.getTotal();
					String dcString = attackBonus.toString();
					dcString = dc + (dcString.equals("") ? "" : " (" + dcString + ")");

					DiceRollBonusCollection defenseBonus = new DiceRollBonusCollection();
					if (defenseItem != null)
						DiceRollBonusCollection.getDefensiveItemBonus(defenseItem);
					int result = new DiceRoll(20).getSum() + defenseBonus.getTotal();
					String resultString = defenseBonus.toString();
					resultString = result + (resultString.equals("") ? "" : " (" + resultString + ")");
					if (defendEvent.type == EventTypes.ATTACK) {
						String implementString = implementItem == null ? "" : "wielding " + implementItem.getName(true);
						if (result >= (dc + 5)) {
							Helper.sendMessage(target, nick + " successfully " + getActionByType(method).type.actionNamePast.toLowerCase() + " " + defendEvent.triggeringUser + (implementString.equals("") ? "" : " " + implementString) + (defenseItem == null ? "" : " using " + defenseItem.getName(true)) + ". With " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + " " + nick + " avoided all of the damage!");
						} else if (result >= dc) {
							effectiveDamage = (int) Math.max(1, Math.floor(damage / 2d));
							Helper.sendMessage(target, nick + " managed to partially " + getActionByType(method).type.actionNameWill.toLowerCase() + " " + defendEvent.triggeringUser + (implementString.equals("") ? "" : " " + implementString) + (defenseItem == null ? "" : " using " + defenseItem.getName(true)) + ". With " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + " " + nick + " only takes half of the " + dec.format(damage) + " damage.");
						} else {
							Helper.sendMessage(target, nick + " failed to " + getActionByType(method).type.actionNameWill.toLowerCase() + " " + defendEvent.triggeringUser + (implementString.equals("") ? "" : " " + implementString) + (defenseItem == null ? "" : " using " + defenseItem.getName(true)) + ". With " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + " " + nick + " takes the full " + dec.format(damage) + " damage.");
						}
					} else if (defendEvent.type == EventTypes.POTION) {
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
									String effectString = PotionHelper.replaceParamsInEffectString(effectEntry.effectDrink, altTarget);
									potionString = " " + effectString.substring(0, 1).toUpperCase() + effectString.substring(1);
								}
							}
							Helper.sendMessage(target, nick + " manages to " + getActionByType(method).type.actionNameWill.toLowerCase() + " the " + defendEvent.implement + " " + defendEvent.triggeringUser + " threw with " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + ". It splashes onto " + altTarget + " that was standing next to you." + potionString);
						} else
							Helper.sendMessage(target, nick + " fails to " + getActionByType(method).type.actionNameWill + " the " + defendEvent.implement + " " + defendEvent.triggeringUser + " threw with " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + ".");
					} else if (defendEvent.type == EventTypes.FLING) {
						if (result >= dc + 5) {
							Helper.sendMessage(target, nick + " successfully " + getActionByType(method).type.actionNamePast.toLowerCase() + " the " + defendEvent.implement + " flung at them by " + defendEvent.triggeringUser + " with " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + ", avoiding all the damage.");
						} else if (result >= dc) {
							effectiveDamage = (int) Math.max(1, Math.floor(damage / 2d));
							Helper.sendMessage(target, nick + " successfully " + getActionByType(method).type.actionNamePast.toLowerCase() + " the " + defendEvent.implement + " flung at them by " + defendEvent.triggeringUser + " with " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + ", taking only half of " + dec.format(damage) + " damage.");
						} else {
							Helper.sendMessage(target, nick + " fails to " + getActionByType(method).type.actionNameWill.toLowerCase() + " the " + defendEvent.implement + " flung at them by " + defendEvent.triggeringUser + " with " + Helper.getNumberPrefix(result) + " " + resultString + " vs " + dcString + ", taking the full " + dec.format(effectiveDamage) + " damage.");
						}
					}
				} else {
					Helper.sendMessage(target, "Nothing to defend against right now.", nick);
				}
			}
		};
		local_command.setHelpText("Defend against things! Getting stabbed? Things thrown at you? No problem! Just defend!");
		for (Actions action : Actions.values()) {
			if (action.command != null)
				local_command.registerAlias(action.command, action.command);
		}
	}

	/**
	 * @param triggeringUser String
	 * @param targetUser     String
	 * @param implement      String
	 * @param type           String Supported types are `attack`, & `pet`
	 */
	public static void addEvent(String triggeringUser, String targetUser, String target, String implement, EventTypes type, String result) {
		addEvent(triggeringUser, targetUser, target, 0, implement, type, result);
	}

	/**
	 * @param triggeringUser String
	 * @param targetUser     String
	 * @param damage         int
	 * @param implement      String
	 * @param type           String Supported types are `attack`, & `pet`
	 */
	public static void addEvent(String triggeringUser, String targetUser, String target, int damage, String implement, EventTypes type, String result) {
		targetUser = targetUser.toLowerCase();
		defendEventLog.add(new DefendEvent(Helper.cleanNick(triggeringUser), Helper.cleanNick(targetUser), target, new Date(), damage, implement, type, result));
	}

	public static ArrayList<DefendEvent> getEventsFor(String user) {
		user = user.toLowerCase();
		ArrayList<DefendEvent> events = new ArrayList<>();
		for (DefendEvent event : defendEventLog) {
			if (event.targetUser.equals(user))
				events.add(event);
		}
		return events;
	}
}
