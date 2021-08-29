package pcl.lc.irc.hooks;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaException;
import com.naef.jnlua.LuaState;
import org.apache.commons.io.IOUtils;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.*;
import pcl.lc.utils.CommandChainStateObject;
import pcl.lc.utils.Helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class JNLuaSandbox extends AbstractListener {
	private Command command_lua;
	private Command command_selene;
	private Command command_reset_state;

	private static LuaState luaState;
	private String luasb;
	private String sparser;
	private String selene;

	private StringBuilder output;

	@Override
	protected void initHook() {
		try {
			InputStream luain = getClass().getResourceAsStream("/jnlua/luasb.lua");
			luasb = CharStreams.toString(new InputStreamReader(luain, Charsets.UTF_8));

			InputStream sparserin = getClass().getResourceAsStream("/jnlua/selene/parser.lua");
			sparser = CharStreams.toString(new InputStreamReader(sparserin, Charsets.UTF_8));

			InputStream selenein = getClass().getResourceAsStream("/jnlua/selene/init.lua");
			selene = CharStreams.toString(new InputStreamReader(selenein, Charsets.UTF_8));
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		initLua();
		initCommands();
		IRCBot.registerCommand(command_lua);
		IRCBot.registerCommand(command_selene);
	}

	private static String stackToString(LuaState luaState) {
		int top = luaState.getTop();
		if (top > 0) {
			StringBuilder results = new StringBuilder();
			for (int i = 1; i <= top; i++) {
				String result;
				if (luaState.isString(i) || luaState.isNumber(i))
					result = luaState.toString(i);
				else if (luaState.isBoolean(i))
					result = luaState.toBoolean(i) ? "true" : "false";
				else if (luaState.isNil(i))
					result = luaState.typeName(i);
				else
					result = String.format("%s: 0x%x", luaState.typeName(i), luaState.toPointer(i));
				results.append(result);
				if (i < top)
					results.append(", ");
			}
			return results.toString();
		} else {
			return "";
		}
	}

	protected void initLua() {
		if (luaState != null) {
			luaState.close();
		}
		luaState = new LuaState(1024*1024);
		luaState.openLib(LuaState.Library.BASE);
		luaState.openLib(LuaState.Library.COROUTINE);
		luaState.openLib(LuaState.Library.TABLE);
		luaState.openLib(LuaState.Library.STRING);
		luaState.openLib(LuaState.Library.BIT32);
		luaState.openLib(LuaState.Library.MATH);
		luaState.openLib(LuaState.Library.OS);
		luaState.openLib(LuaState.Library.PACKAGE);
		luaState.openLib(LuaState.Library.DEBUG);
		luaState.setTop(0); // Remove tables from loaded libraries
		luaState.pushJavaFunction(new JavaFunction() {
			@Override
			public int invoke(LuaState luaState) {
				String results = stackToString(luaState);
				output.append(results + "\n");
				return 0;
			}
		});
		luaState.setGlobal("print");

		luaState.load(luasb, "=luasb");
		luaState.call(0, 0);

		System.out.println(runScriptInSandbox("selene = (function()\n" + selene + "\nend)()"));
		System.out.println(runScriptInSandbox("selene.parser =(function()\n" + sparser + "\nend)()"));
		System.out.println(runScriptInSandbox("selene.load()"));
	}

	static String runScriptInSandbox(String script) {
		luaState.setTop(0); // Ensure stack is clean
		luaState.getGlobal("lua");
		luaState.pushString(script);
		try {
			luaState.call(1, LuaState.MULTRET);
		} catch (LuaException error) {
			luaState.setTop(0);
			return error.getMessage();
		}
		String results = stackToString(luaState);
		luaState.setTop(0); // Remove results from stack
		return results.toString();
	}

	static String getSnippetIfUrl(String input) {
		if (input.startsWith("http://") || input.startsWith("https://")) {
			URL url = null;
			try {
				Pattern urlPattern = Pattern.compile(
					"(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
						+ "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
						+ "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
				Matcher matcher = urlPattern.matcher(input);
				if (matcher.find()) {
					System.out.println(matcher.group());
				}
				url = new URL(matcher.group());

				URLConnection con = url.openConnection();
				InputStream in = con.getInputStream();
				String encoding = con.getContentEncoding();  // ** WRONG: should use "con.getContentType()" instead but it returns something like "text/html; charset=UTF-8" so this value must be parsed to extract the actual encoding
				encoding = encoding == null ? "UTF-8" : encoding;
				String body = IOUtils.toString(in, encoding);
				System.out.println("Body:" + body);
				return body;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return input;
	}

	private void initCommands() {
		command_lua = new Command("lua", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Snippet")), new CommandRateLimit(10, true, true)) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String snippet) {
				if (snippet != null && !snippet.equals("")) {
					snippet = getSnippetIfUrl(snippet);

					output = new StringBuilder();
					output.append(runScriptInSandbox(snippet));

					// Trim last newline
					if (output.length() > 0 && output.charAt(output.length() - 1) == '\n')
						output.setLength(output.length() - 1);
					String luaOut = output.toString().replace("\n", " | ").replace("\r", "");

					if (luaOut.length() > 0) {
						Helper.AntiPings = Helper.getNamesFromTarget(target);
						Helper.sendMessage(target, luaOut);
					}
				} else {
					Helper.sendMessage(target, "No snippet provided.");
				}
				return new CommandChainStateObject();
			}
		};
		command_lua.setHelpText("Run Lua code snippets (or a file by providing a url)");

		command_selene = new Command("selene", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Snippet")), new CommandRateLimit(10, true, true)) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String snippet) {
				snippet = getSnippetIfUrl(snippet);
				output = new StringBuilder();
				output.append(runScriptInSandbox(runScriptInSandbox("selene.parse([==========[" + snippet + "]==========])")));
				// Trim last newline
				if (output.length() > 0 && output.charAt(output.length() - 1) == '\n')
					output.setLength(output.length() - 1);
				String luaOut = output.toString().replace("\n", " | ").replace("\r", "");

				if (luaOut.length() > 0) {
					Helper.AntiPings = Helper.getNamesFromTarget(target);
					Helper.sendMessage(target, luaOut);
				}
				return new CommandChainStateObject();
			}
		};
		command_selene.setHelpText("Run snippets through Selene (also supports urls)");

		command_reset_state = new Command("reset") {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				initLua();
				Helper.sendMessage(target, "Sandbox reset");
				return new CommandChainStateObject();
			}
		};
		command_reset_state.setHelpText("Reset the Lua sandbox state.");
		command_lua.registerSubCommand(command_reset_state);
	}
}
