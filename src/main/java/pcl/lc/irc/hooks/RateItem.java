package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.DiceRollBonusCollection;
import pcl.lc.utils.Helper;
import pcl.lc.utils.Item;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class RateItem extends AbstractListener {
	private Command local_command;
	private Command sub_command_attack;
	private Command sub_command_defense;
	private Command sub_command_healing;

    private Item makeItem(String itemName) {
        Item item = null;
        try {
            item = new Item(0, itemName, 1, false, "", 0, "", false);
        } catch (Exception e) {
            e.printStackTrace();
            Helper.sendMessage(target, "I had an oopsie while trying to create this item...");
        }
        return item;
    }

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command, "Rates items attack, defense or healing bonuses");
	}

	private void initCommands() {
		local_command = new Command("rateitem", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
                Helper.sendMessage(target, this.trySubCommandsMessage(params), nick);
            }
		};

		sub_command_attack = new Command("attack") {
		    @Override
            public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String itemName) {
		        if (itemName.length() == 0) {
                    Helper.sendMessage(target, "That's a very nice nothing you have there... I rate it 5/7!", nick);
                    return;
                }
                Item item = makeItem(itemName);
                DiceRollBonusCollection bonus = DiceRollBonusCollection.getOffensiveItemBonus(itemName);
                if (bonus.incapable)
                    Helper.sendMessage(target, "This item is incapable of doing damage.");
                else if (bonus.size() == 0)
                    Helper.sendMessage(target, "This item's damage die is a d" + item.getDiceSizeFromItemName() + "! It has no attack bonuses.");
                else
                    Helper.sendMessage(target, "This item's damage die is a d" + item.getDiceSizeFromItemName() + "! It has an attack bonus of " + (bonus.getTotal() > 0 ? "+" : "") + bonus.getTotal() + ", (" + bonus + ").");
            }
        };
		sub_command_attack.registerAlias("att");

        sub_command_defense = new Command("defense") {
		    @Override
            public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String itemName) {
                if (itemName.length() == 0) {
                    Helper.sendMessage(target, "That's a very nice nothing you have there... I rate it 5/7!", nick);
                    return;
                }
                Item item = makeItem(itemName);
                DiceRollBonusCollection bonus = DiceRollBonusCollection.getDefensiveItemBonus(itemName);
                if (bonus.incapable)
                    Helper.sendMessage(target, "This item is incapable of blocking damage.");
                else if (bonus.size() == 0)
                    Helper.sendMessage(target, "This item's damage reduction die is a d" + item.getDiceSizeFromItemName() + "! It has no reduction bonus");
                else
                    Helper.sendMessage(target, "This item's damage reduction die is a d" + item.getDiceSizeFromItemName() + "! It has a reduction bonus of " + (bonus.getTotal() > 0 ? "+" : "") + bonus.getTotal() + ", (" + bonus + ").");
            }
        };
		sub_command_defense.registerAlias("def");

        sub_command_healing = new Command("healing") {
		    @Override
            public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String itemName) {
                if (itemName.length() == 0) {
                    Helper.sendMessage(target, "That's a very nice nothing you have there... I rate it 5/7!", nick);
                    return;
                }
                Item item = makeItem(itemName);
                DiceRollBonusCollection bonus = DiceRollBonusCollection.getHealingItemBonus(itemName);
                if (bonus.incapable)
                    Helper.sendMessage(target, "This item is incapable of healing.");
                else if (bonus.size() == 0)
                    Helper.sendMessage(target, "This item's healing die is a d" + item.getDiceSizeFromItemName() + "! It has no healing bonus");
                else
                    Helper.sendMessage(target, "This item's healing die is a d" + item.getDiceSizeFromItemName() + "! It has a healing bonus of " + (bonus.getTotal() > 0 ? "+" : "") + bonus.getTotal() + ", (" + bonus + ").");
            }
        };
		sub_command_healing.registerAlias("heal");

		local_command.registerSubCommand(sub_command_attack);
		local_command.registerSubCommand(sub_command_defense);
		local_command.registerSubCommand(sub_command_healing);
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
