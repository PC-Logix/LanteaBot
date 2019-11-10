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

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command, "Rates items attack, defense or healing bonuses");
	}

	private void initCommands() {
		local_command = new Command("rateitem", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				int number = 0;
				StringBuilder sb = new StringBuilder();
				for (String str : params.subList(1, params.size()))
					sb.append(str).append(" ");
				String itemName = sb.toString().trim();
				if (params.size() > 1) {
					DiceRollBonusCollection bonus;
					switch (params.get(0)) {
						case "attack":
						case "att":
							bonus = DiceRollBonusCollection.getOffensiveItemBonus(itemName);
							if (bonus.incapable)
								Helper.sendMessage(target, "This item cannot do damage.");
							else if (bonus.size() == 0)
								Helper.sendMessage(target, "This has no attack bonus");
							else
								Helper.sendMessage(target, "This has an attack bonus of " + (bonus.getTotal() > 0 ? "+" : "") + bonus.getTotal() + ", (" + bonus + ").");
							return;
						case "defense":
						case "def":
							bonus = DiceRollBonusCollection.getDefensiveItemBonus(itemName);
							if (bonus.incapable)
								Helper.sendMessage(target, "This item cannot block damage.");
							else if (bonus.size() == 0)
								Helper.sendMessage(target, "This has no damage reduction bonus");
							else
								Helper.sendMessage(target, "This has a damage reduction bonus of " + (bonus.getTotal() > 0 ? "+" : "") + bonus.getTotal() + ", (" + bonus + ").");
							return;
						case "healing":
						case "heal":
						case "health":
							bonus = DiceRollBonusCollection.getHealingItemBonus(itemName);
							if (bonus.incapable)
								Helper.sendMessage(target, "This item cannot heal.");
							else if (bonus.size() == 0)
								Helper.sendMessage(target, "This has no healing bonus");
							else
								Helper.sendMessage(target, "This has an healing bonus of " + (bonus.getTotal() > 0 ? "+" : "") + bonus.getTotal() + ", (" + bonus + ").");
					}
				}
				else if (params.size() == 0)
					Helper.sendMessage(target, "Parameter one should be 'att', 'def' or 'heal' followed by an item name");
				else
					Helper.sendMessage(target, "That's a very nice nothing you have there... I rate it 5/7!", nick);
			}
		};
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
