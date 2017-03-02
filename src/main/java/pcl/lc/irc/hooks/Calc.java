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
		local_command = new Command("calc", 0);
		IRCBot.registerCommand(local_command, "Does basic math on the expression passed to the command Ex: " + Config.commandprefix + "calc 2+2");
	}

	@Override
	public void handleCommand(String sender, final MessageEvent event, String command, String[] args) {
		long shouldExecute = local_command.shouldExecute(command);
		if (shouldExecute == 0) {
			String expression;
			expression = StringUtils.join(args," ");
			if (!IRCBot.isIgnored(event.getUser().getNick())) {
				if (expression.equalsIgnoreCase("the meaning of life")) {
					event.respond("42");
				} else {
					Expression e = new ExpressionBuilder(expression).build();
					double result = e.evaluate();
					NumberFormat formatter = new DecimalFormat("#,###.##");
					formatter.setRoundingMode(RoundingMode.DOWN);
					event.getBot().sendIRC().message(event.getChannel().getName(), Helper.antiPing(sender) + ": " + formatter.format(result));
				}
			}
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event,
			String command, String[] copyOfRange) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		// TODO Auto-generated method stub
		
	}
}