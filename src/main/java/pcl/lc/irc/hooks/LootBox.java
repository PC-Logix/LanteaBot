package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.*;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class LootBox extends AbstractListener {
	private Command local_command;
	private Map<Integer, String> rarities;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);

		rarities = new HashMap<>();
		rarities.put(100, "Normal");
		rarities.put(50, "Rare");
		rarities.put(25, "Magic");
		rarities.put(10, "Shiny");
		rarities.put(5, "Legendary");
		rarities.put(1, "Cursed");
	}

	private void initCommands() {
		local_command = new Command("lootbox", new CommandArgumentParser(0, new CommandArgument(ArgumentTypes.STRING, "LootTarget")), new CommandRateLimit(60)) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				int rarity_value = Helper.rollDice("1d100").getSum();
				String item_name;
				String rarity = "Normal";
				String rarity_str = "??";
				int rarity_perc;
				for (Map.Entry<Integer, String> rar : rarities.entrySet()) {
					if (rarity_value < rar.getKey()) {
						rarity = rar.getValue();
						rarity_perc = rar.getKey();
						rarity_str = rarity_perc == 100 ? "Junk" : rarity_perc + "%";
					}
				}
				if (rarity == "Normal") {
					item_name = TablesOfRandomThings.getRandomGarbageItem(true, true) + ".";
				} else {
					Item item = Inventory.getRandomItem(true);
					if (item == null) {
						item_name = TablesOfRandomThings.getRandomGarbageItem(true, true) + ".";
					} else {
						boolean curse = (rarity == "Cursed");
						String[] strings = Helper.solvePrefixes(item.getNameRaw());
						if (strings != null)
							item_name = strings[0] + " " + rarity + " " + strings[1] + "! (" + rarity_str + ")";
						else
							item_name = "a " + rarity + " " + item.getNameWithoutPrefix() + "! (" + rarity_str + ")";
						String added_by = (curse ? "The Curse (" + item.getAdded_by() + ")" : item.getAdded_by());
						Inventory.addRawItem(new Item(0, item_name, item.getUsesLeft() + 15, false, added_by, item.getAddedRaw(), nick, curse));
						item.destroy();
					}
				}
				if (item_name.contains("randompotion")) {
					PotionEntry potion = PotionHelper.getRandomPotion();
					item_name = potion.consistency.getName() + " " + potion.appearance.getName() + " potion";
				}
				String prefix = "You get a loot box! It contains {item}";
				String lootTarget = this.argumentParser.getArgument("LootTarget");
				if (lootTarget != null && !lootTarget.equals("")) {
					if (!Helper.doInteractWith(lootTarget)) {
						Helper.sendAction(target, "Kicks " + nick + " into the tentacle pit.");
						return new CommandChainStateObject();
					}
					prefix = "You stab " + lootTarget + "! It dropped {item}!";
				}
				String item_string = item_name + " (" + rarity_str + ")";
				Helper.sendMessage(target, prefix.replace("{item}", item_string), nick);
				return new CommandChainStateObject();
			}
		};
		local_command.setHelpText("Get a loot box! What could be inside!");
		local_command.registerAlias("loot");
	}
}
