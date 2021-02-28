package pcl.lc.irc.hooks;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.Helper;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Calc extends AbstractListener {
	private Command local_command;
	
	@Override
	protected void initHook() {
		local_command = new Command("calc", new CommandArgumentParser(1, new CommandArgument("Expression", ArgumentTypes.STRING))) {
			@Override
			public CommandChainState onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String expression = this.argumentParser.getArgument("Expression");
				if (expression.equalsIgnoreCase("the meaning of life")) {
					Helper.sendMessage(target, "42", nick);
				} else {
					Expression e = new ExpressionBuilder(expression).build();
					double result = e.evaluate();
					NumberFormat formatter = new DecimalFormat("#,###.##");
					formatter.setRoundingMode(RoundingMode.DOWN);
					Helper.sendMessage(target, formatter.format(result), nick);
				}
				return CommandChainState.FINISHED;
			}
		}; local_command.setHelpText("Does basic math on the expression passed to the command Ex: 2+2");
// 		This is handled by the dice command now.
//		IRCBot.registerCommand(local_command);
	}
}