package pcl.lc.irc.hooks;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.*;
import pcl.lc.utils.CommandChainStateObject;
import pcl.lc.utils.Helper;
import pcl.lc.utils.SandboxThreadFactory;
import pcl.lc.utils.SandboxThreadGroup;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.*;
import java.util.concurrent.*;

/**
 * @author Caitlyn
 *
 */

@SuppressWarnings("rawtypes")
public class JavaScript  extends AbstractListener {
	private NashornScriptEngineFactory engineFactory = new NashornScriptEngineFactory();;
	private final SandboxThreadGroup sandboxGroup = new SandboxThreadGroup("javascript");
	private final ThreadFactory sandboxFactory = new SandboxThreadFactory(sandboxGroup);

	public JavaScript() {
		IRCBot.registerCommand("js", "Do the Javascript? I dunno.");
	}

	private Command command_js;
	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(command_js);
	}

	private void initCommands() {
		command_js = new Command("js", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Snippet")), new CommandRateLimit(10, true, true)) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String snippet) {
				if (snippet != null && !snippet.equals("")) {
					snippet = Helper.getSnippetIfUrl(snippet);

					if (engineFactory == null || snippet == null) return new CommandChainStateObject();
					StringBuilder output = new StringBuilder();
					NashornScriptEngine engine = (NashornScriptEngine)engineFactory.getScriptEngine(new String[] {"-strict", "--no-java", "--no-syntax-extensions"});
					output.append(eval(engine, snippet));

					// Trim last newline
					if (output.length() > 0 && output.charAt(output.length() - 1) == '\n')
						output.setLength(output.length() - 1);
					String jsOut = output.toString().replace("\n", " | ").replace("\r", "");

					if (jsOut.length() > 0) {
						Helper.AntiPings = Helper.getNamesFromTarget(target);
						Helper.sendMessage(target, jsOut);
					}

				} else {
					Helper.sendMessage(target, "No snippet provided.");
				}
				return new CommandChainStateObject();
			}
		};
		command_js.setHelpText("Run JS code snippets (or a file by providing a url)");
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