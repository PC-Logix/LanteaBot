/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.DiceRoll;
import pcl.lc.utils.DiceRollGroup;
import pcl.lc.utils.Helper;
import pcl.lc.utils.MathParseExpression;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Dice extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		local_command = new Command("dice", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				params = DiceRoll.rollDiceInString(params);
				MathParseExpression exp = new MathParseExpression(params);

				if (params.equals(String.valueOf(exp.result)))
					Helper.sendMessage(target, String.valueOf(exp.result));
				else
					Helper.sendMessage(target, params + " => " + exp.result);
			}
		}; local_command.setHelpText("Rolls dice and solves mathematical expressions using + - * and /, and even both at the same time.. (Dice are expressed as eg 1d20 or d20)");
		local_command.registerAlias("roll");
		local_command.registerAlias("math");
		local_command.registerAlias("expression");
		local_command.registerAlias("exp");
		IRCBot.registerCommand(local_command);
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
