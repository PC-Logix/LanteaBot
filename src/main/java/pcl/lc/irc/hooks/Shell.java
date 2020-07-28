/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.*;

import java.util.ArrayList;
import java.util.List;

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
		shell = new Command("shell") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				DiceRoll roll = Helper.rollDice("1d100").getFirstGroupOrNull();
				String shellTarget = null;
				String shellTargetSecondary = null;
				String shellTargetTertriary = null;
				String with = null;

				if (params.length() > 0) {
					String[] split = params.split("with ", 2);
					shellTarget = split[0].trim();
					if (split.length > 1 && !split[1].trim().equals(""))
						with = split[1].trim();

					String[] targets = shellTarget.split(" and ", 3);
					shellTarget = targets[0].trim();
					if (targets.length > 1)
						shellTargetSecondary = targets[1].trim();
					if (targets.length > 2)
						shellTargetTertriary = targets[2].trim();
				}

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
					if (shellTargetSecondary == null)
						shellTargetSecondary = Helper.getRandomUser(event, blacklist);
					blacklist.add(shellTargetSecondary);
					if (shellTargetTertriary == null)
						shellTargetTertriary = Helper.getRandomUser(event, blacklist);
					if (!Helper.doInteractWith(shellTarget) || !Helper.doInteractWith(shellTargetSecondary) || !Helper.doInteractWith(shellTargetTertriary)) {
						Helper.sendAction(target, "kicks " + nick + " into space.");
						return;
					}
					if (potion != null) {
					    Helper.AntiPings = Helper.getNamesFromTarget(target);
					    EffectEntry effect = potion.getEffect(nick, true);
					    Helper.sendMessage(target, nick + " loads " + potion.consistency.getName(true, true) + " " + potion.appearance.getName(false, true) + (potion.isNew ? " (New!)" : "") + " potion into a shell and fires it. It lands and explodes into a cloud of vapour. " + PotionHelper.replaceParamsInEffectString(effect.effectDrink, shellTarget + ", " + shellTargetSecondary + " & " + shellTargetTertriary, nick));
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
			}
		};
		shell.setHelpText("Be a nuisance with your very own mortar! Syntax: " + Config.commandprefix + shell.getCommand() + " [<target>[ and <target>][ and <target>][ with <item>]]  <item> can be a valid potion string or \"random potion\". If [ with <item>] is omitted tries to use a random item from the inventory. Omitted targets are selected randomly from IRC user list.");
	}
}
