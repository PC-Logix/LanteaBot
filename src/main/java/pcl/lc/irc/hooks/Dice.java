/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.*;
import pcl.lc.irc.IRCBot;
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
		local_command = new Command("dice", new CommandArgumentParser(1, new CommandArgument("Expression", ArgumentTypes.STRING))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String expression = this.argumentParser.getArgument("Expression");
				expression = DiceRoll.rollDiceInString(expression);
				if (params.contains("=>")) {
					Helper.sendMessage(target, expression);
					return;
				}

				MathParseExpression exp = new MathParseExpression(expression);

				if (expression.equals(String.valueOf(exp.result)))
					Helper.sendMessage(target, String.valueOf(exp.result));
				else
					Helper.sendMessage(target, expression + " => " + exp.result);
			}
		}; local_command.setHelpText("Rolls dice and solves mathematical expressions using + - * and /, and even both at the same time.. (Dice are expressed as eg 1d20 or d20 and supports the following additional parameters which can be chained in this order: k# or kh# - keep highest # of results, kl# - keep lowest # of results, ! or !! - ! rolls exploding dice separately and !! adds the result onto the original dice, <# - count results equal to or lower than # as successes, ># - count results equal to or higher than # as successes.)");
		local_command.registerAlias("roll");
		local_command.registerAlias("math");
		local_command.registerAlias("expression");
		local_command.registerAlias("exp");
		local_command.registerAlias("calc");
		IRCBot.registerCommand(local_command);
	}
}
