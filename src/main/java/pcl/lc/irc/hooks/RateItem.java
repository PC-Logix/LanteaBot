package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.DiceRollBonusCollection;
import pcl.lc.utils.Helper;

import java.util.ArrayList;

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
                DiceRollBonusCollection bonus = DiceRollBonusCollection.getOffensiveItemBonus(itemName);
                if (bonus.incapable)
                    Helper.sendMessage(target, "This item is incapable of doing damage.");
                else if (bonus.size() == 0)
                    Helper.sendMessage(target, "This has no attack bonus");
                else
                    Helper.sendMessage(target, "This has an attack bonus of " + (bonus.getTotal() > 0 ? "+" : "") + bonus.getTotal() + ", (" + bonus + ").");
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
                DiceRollBonusCollection bonus = DiceRollBonusCollection.getDefensiveItemBonus(itemName);
                if (bonus.incapable)
                    Helper.sendMessage(target, "This item is incapable of blocking damage.");
                else if (bonus.size() == 0)
                    Helper.sendMessage(target, "This has no damage reduction bonus");
                else
                    Helper.sendMessage(target, "This has a damage reduction bonus of " + (bonus.getTotal() > 0 ? "+" : "") + bonus.getTotal() + ", (" + bonus + ").");
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
                DiceRollBonusCollection bonus = DiceRollBonusCollection.getHealingItemBonus(itemName);
                if (bonus.incapable)
                    Helper.sendMessage(target, "This item is incapable of healing.");
                else if (bonus.size() == 0)
                    Helper.sendMessage(target, "This has no healing bonus");
                else
                    Helper.sendMessage(target, "This has a healing bonus of " + (bonus.getTotal() > 0 ? "+" : "") + bonus.getTotal() + ", (" + bonus + ").");
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
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		target = Helper.getTarget(event);
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}
