package pcl.lc.irc.hooks;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
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
		local_command = new Command("calc") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (params.equalsIgnoreCase("the meaning of life")) {
					Helper.sendMessage(target, "42", nick);
				} else {
					Expression e = new ExpressionBuilder(params).build();
					double result = e.evaluate();
					NumberFormat formatter = new DecimalFormat("#,###.##");
					formatter.setRoundingMode(RoundingMode.DOWN);
					Helper.sendMessage(target, formatter.format(result), nick);
				}
			}
		}; local_command.setHelpText("Does basic math on the expression passed to the command Ex: 2+2");
		IRCBot.registerCommand(local_command);
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, final MessageEvent event, String command, String[] args, String callingRelay) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event,	String command, String[] copyOfRange, String callingRelay) {
		target = Helper.getTarget(event);
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}