/**
 *
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.*;
import pcl.lc.utils.*;

import java.util.ArrayList;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Shell extends AbstractListener {
	private Command shell;
	private final int hitChance = 40; //percentage (out of 100)

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(shell);
	}

	private void initCommands() {
		shell = new Command("shell", new CommandArgumentParser(0, new CommandArgument(ArgumentTypes.STRING, "Target1", "If empty targets random IRC user."), new CommandArgument(ArgumentTypes.STRING, "Target2", "If empty targets random IRC user."), new CommandArgument(ArgumentTypes.STRING, "Target3", "If empty targets random IRC user."), new CommandArgument(ArgumentTypes.STRING, "ItemOrPotion", "If item is not specified tries to use random inventory item."))) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				DiceRoll roll = Helper.rollDice("1d100").getFirstGroupOrNull();
				String shellTarget = this.argumentParser.getArgument("Target1");
				String shellTargetSecondary = this.argumentParser.getArgument("Target2");
				String shellTargetTertriary = this.argumentParser.getArgument("Target3");
				String with = this.argumentParser.getArgument("ItemOrPotion");

				PotionEntry potion = PotionEntry.setFromString(with);
				if (potion == null && with != null && with.contains("random potion"))
					potion = PotionHelper.getRandomPotion();

				Item item = null;
				try {
					if (with == null)
						item = Inventory.getRandomItem(false);
					else
						item = new Item(with, false);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				if (item != null || potion != null) {
					ArrayList<String> blacklist = new ArrayList<>();
					blacklist.add(nick);
					if (shellTarget == null || shellTarget.equals(""))
						shellTarget = Helper.getRandomUser(event, blacklist);
					blacklist.add(shellTarget);
					if (shellTargetSecondary == null || shellTargetSecondary.equals(""))
						shellTargetSecondary = Helper.getRandomUser(event, blacklist);
					blacklist.add(shellTargetSecondary);
					if (shellTargetTertriary == null || shellTargetTertriary.equals(""))
						shellTargetTertriary = Helper.getRandomUser(event, blacklist);
					if (!Helper.doInteractWith(shellTarget) || !Helper.doInteractWith(shellTargetSecondary) || !Helper.doInteractWith(shellTargetTertriary)) {
						Helper.sendAction(target, "kicks " + nick + " into space.");
						return new CommandChainStateObject();
					}
					if (potion != null) {
						potion.getEffect(nick, true, shellTarget);
						Helper.AntiPings = Helper.getNamesFromTarget(target);
						String effectStringPrimary = potion.effect.doAction(new EffectActionParameters(shellTarget, nick, true, potion.isNew));
						String effectStringSecondary = potion.effect.doAction(new EffectActionParameters(shellTargetSecondary, nick, true, potion.isNew));
						String effectStringTertriary = potion.effect.doAction(new EffectActionParameters(shellTargetTertriary, nick, true, potion.isNew));
						if (effectStringPrimary == null)
							effectStringPrimary = potion.effect.getEffectStringDiscovered(shellTarget, nick, true);
						if (effectStringSecondary == null)
							effectStringSecondary = potion.effect.getEffectStringDiscovered(shellTargetSecondary, nick, true);
						if (effectStringTertriary == null)
							effectStringTertriary = potion.effect.getEffectStringDiscovered(shellTargetTertriary, nick, true);
//						Helper.sendMessage(target, nick + " loads " + potion.consistency.getName(true, true) + " " + potion.appearance.getName(false, true) + (potion.isNew ? " (New!)" : "") + " potion into a shell and fires it. It lands and explodes into a cloud of vapour. " + PotionHelper.replaceParamsInEffectString(effect.effectDrink, shellTarget + ", " + shellTargetSecondary + " & " + shellTargetTertriary, nick));
						Helper.sendMessage(target, nick + " loads " + potion.consistency.getName(true, true) + " " + potion.appearance.getName(false, true) + (potion.isNew ? " (New!)" : "") + " potion into a shell and fires it. It lands and explodes into a cloud of vapour which engulfs " + shellTarget + ", " + shellTargetSecondary + ", and " + shellTargetTertriary);
						Helper.sendMessage(target, effectStringPrimary);
						Helper.sendMessage(target, effectStringSecondary);
						Helper.sendMessage(target, effectStringTertriary);
					} else {
						int itemDamage = 0;
						String dust;
						String strike = "Seems it was a dud...";
						try {
							boolean hit = false;
							if (roll != null && roll.getSum() < hitChance)
								hit = true;
							DiceRollResult dmg1 = item.getDamage(1, item.getDiceSizeFromItemName() + (hit ? 4 : 2), 4);
							DiceRollResult dmg2 = item.getDamage(1, item.getDiceSizeFromItemName() + 2, 2);
							DiceRollResult dmg3 = item.getDamage(1, item.getDiceSizeFromItemName() + 2, 2);
							String dmgString1 = dmg1.getResultString();
							String dmgString2 = dmg2.getResultString();
							String dmgString3 = dmg3.getResultString();
							itemDamage = (hit ? 1 : 2);

							if (hit) {
								String auxiliary_damage = (dmgString2.equals(dmgString3) ? dmgString2 + " damage each" : dmgString2 + ", and " + dmgString3 + " damage respectively");
								strike = "It strikes " + shellTarget + ". They take " + dmgString1 + " damage. " + shellTargetSecondary + " and " + shellTargetTertriary + " stood too close and take " + auxiliary_damage + ".";
							} else {
								String damage = (dmgString1.equals(dmgString2) && dmgString1.equals(dmgString3) ? dmgString1 + " damage each" : dmgString1 + ", " + dmgString2 + ", and " + dmgString3 + " splash damage respectively");
								strike = "It strikes the ground near " + shellTarget + ", " + shellTargetSecondary + ", and " + shellTargetTertriary + ". They take " + damage + ".";
							}
						} catch (NullPointerException ignored) {
						}
						Helper.AntiPings = Helper.getNamesFromTarget(target);
						Helper.sendMessage(target, nick + " loads " + item.getName(false) + " into a shell and fires it. " + strike);
						dust = item.damage(itemDamage, false, true, true);
						if (!dust.equals("")) {
							Helper.AntiPings = Helper.getNamesFromTarget(target);
							Helper.sendMessage(target, dust);
						}
					}
				} else {
					Helper.AntiPings = Helper.getNamesFromTarget(target);
					Helper.sendMessage(target, nick + " found nothing to load into the shell...");
				}
				return new CommandChainStateObject();
			}
		};
		shell.setHelpText("Be a nuisance with your very own mortar!");
	}
}
