package pcl.lc.irc.hooks;

import java.io.StringWriter;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */

@SuppressWarnings("rawtypes")
public class JavaScript  extends ListenerAdapter {
	public JavaScript() {
		IRCBot.registerCommand("js");
	}

	String script = null;

	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		String prefix = Config.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		if (trigger.length() > 1) {
			String[] firstWord = StringUtils.split(trigger);
			String triggerWord = firstWord[0];
			if (triggerWord.equals(prefix + "js")) {
				script = event.getMessage().substring(event.getMessage().indexOf("js") + 2).trim();
				ScriptEngineManager factory = new ScriptEngineManager();
				ScriptEngine engine = factory.getEngineByName("JavaScript");
				ScriptContext context = engine.getContext();
				StringWriter writer = new StringWriter();
				context.setWriter(writer);
				engine.eval(script);
				String output = writer.toString();
				Invocable invokeEngine = (Invocable) engine;
				Runnable runner = invokeEngine.getInterface(Runnable.class);
				Thread t = new Thread(runner);
				t.start();
				t.join();
				event.respond(output);
				t.stop();
				event.respond("Thread count: " + java.lang.Thread.activeCount());
			}
		}
	}

}