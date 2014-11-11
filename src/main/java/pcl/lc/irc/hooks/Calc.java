package pcl.lc.irc.hooks;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Calc extends ListenerAdapter {
	public Calc() {
		IRCBot.registerCommand("calc");
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		String prefix = IRCBot.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		if (trigger.length() > 1) {
			String[] firstWord = StringUtils.split(trigger);
			String triggerWord = firstWord[0];
			if (triggerWord.equals(prefix + "calc")) {
				String expression = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
				if (expression.equalsIgnoreCase("the meaning of life")) {
					event.respond("42");
				} else {
					Expression e = new ExpressionBuilder(expression).build();
					double result = e.evaluate();
					NumberFormat formatter = new DecimalFormat("#,###.##");
					event.respond(formatter.format(result));
				}
				
			}
		}
	}
}