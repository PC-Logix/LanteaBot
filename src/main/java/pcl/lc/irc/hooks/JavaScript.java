package pcl.lc.irc.hooks;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;
import pcl.lc.utils.SandboxThreadFactory;
import pcl.lc.utils.SandboxThreadGroup;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.*;

/**
 * @author Caitlyn
 *
 */

@SuppressWarnings("rawtypes")
public class JavaScript  extends ListenerAdapter {
	private NashornScriptEngineFactory engineFactory = new NashornScriptEngineFactory();;
	private final SandboxThreadGroup sandboxGroup = new SandboxThreadGroup("javascript");
	private final ThreadFactory sandboxFactory = new SandboxThreadFactory(sandboxGroup);

	public JavaScript() {
		IRCBot.registerCommand("js", "Do the Javascript? I dunno.");
	}

	String code = null;

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
				code = event.getMessage().substring(event.getMessage().indexOf("js") + 2).trim();
				if (engineFactory == null || code == null) return;
				StringBuilder output = new StringBuilder();
				NashornScriptEngine engine = (NashornScriptEngine)engineFactory.getScriptEngine(new String[] {"-strict", "--no-java", "--no-syntax-extensions"});
				output.append(eval(engine, code));
				if (output.length() > 0 && output.charAt(output.length()-1) == '\n')
					output.setLength(output.length()-1);
				Helper.sendMessage(event.getChannel().getName(), output.toString().replace("\n", " | ").replace("\r", ""));
			}
		}
	}
	
	public String eval(NashornScriptEngine engine, String code) {
		CompiledScript cs;
		try {
			cs = engine.compile(code);
		} catch (ScriptException e) {
			return e.getMessage();
		}
		JSRunner r = new JSRunner(cs);

		final ExecutorService service = Executors.newSingleThreadExecutor(sandboxFactory);
		TimeUnit unit = TimeUnit.SECONDS;
		String output = null;
		try {
			Future<String> f = service.submit(r);
		    output = f.get(5, unit);
		}
		catch(TimeoutException e) {
		    output = "Script timed out";
		}
		catch(Exception e) {
			e.printStackTrace();
			output = e.getMessage();
		}
		finally {
		    service.shutdown();
		}
		if (output == null)
			output = "";

		return output;
	}

	public class JSRunner implements Callable<String> {
		
		private final CompiledScript cs;
		
		public JSRunner(CompiledScript cs) {
			this.cs = cs;
		}

		@Override
		public String call() throws Exception {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ScriptContext context = cs.getEngine().getContext();
				context.setWriter(pw);
				context.setErrorWriter(pw);
				
				try {
					Object out = cs.eval();
					if (sw.getBuffer().length() != 0)
						return sw.toString();
					if (out != null)
						return out.toString();
				}
				catch(ScriptException ex) {
					return ex.getMessage();
				}
				return null;
		}
	}
	
}