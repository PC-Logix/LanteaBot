/**
 *
 */
package pcl.lc.irc.hooks;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaException;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaState.Library;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.*;
import pcl.lc.irc.entryClasses.*;
import pcl.lc.utils.*;
import pcl.lc.utils.db_items.DbCommand;
import pcl.lc.utils.db_items.DbInventoryItem;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Caitlyn
 */
@SuppressWarnings("rawtypes")
public class DynamicCommands extends AbstractListener {
	static ArrayList<String> dynamicCommands;
	private static NashornScriptEngineFactory engineFactory = new NashornScriptEngineFactory();
	private static final SandboxThreadGroup sandboxGroup = new SandboxThreadGroup("javascript");
	private static final ThreadFactory sandboxFactory = new SandboxThreadFactory(sandboxGroup);

	public static final String defaultHelpText = "Dynamic command with no help text set.";

	private Command toggle_command;

	private Command base_command;
	private Command add;
	private Command del;
	private Command addhelp;
	private Command print;
	private Command edit;
	private Command alias;
	private Command placeholders;
	private Command prefixes;

	public String luasb;

	private static LuaState luaState;
	public static StringBuilder output;

	public static class CommandsHaveBeenRun {
		private ArrayList commands;

		CommandsHaveBeenRun() {
			this.commands = new ArrayList<>();
		}

		protected void addCommand(String command) {
			this.commands.add(command);
		}

		protected boolean hasCommand(String command) {
			return this.commands.contains(command);
		}
	}

	@Override
	protected void initHook() {
		Database.addStatement("CREATE TABLE IF NOT EXISTS Commands(command STRING UNIQUE PRIMARY KEY, return, help)");
		Database.addUpdateQuery(5, "ALTER TABLE Commands ADD help STRING DEFAULT NULL NULL;");
		Database.addUpdateQuery(6, "BEGIN TRANSACTION;\n" +
				"DROP INDEX sqlite_autoindex_Commands_1;\n" +
				"CREATE TABLE Commands0d9e\n" +
				"(\n" +
				"    command STRING PRIMARY KEY,\n" +
				"    return_value STRING,\n" +
				"    help STRING DEFAULT NULL\n" +
				");\n" +
				"CREATE UNIQUE INDEX sqlite_autoindex_Commands_1 ON Commands0d9e (command, return_value);\n" +
				"INSERT INTO Commands0d9e(command, return_value, help) SELECT command, return, help FROM Commands;\n" +
				"DROP TABLE Commands;\n" +
				"ALTER TABLE Commands0d9e RENAME TO Commands;\n" +
				"COMMIT;");
		Database.addPreparedStatement("addCommand", "INSERT OR REPLACE INTO Commands(command, return_value) VALUES (?, ?);");
		Database.addPreparedStatement("addCommandHelp", "UPDATE Commands SET help = ? WHERE command = ?");
		Database.addPreparedStatement("searchCommands", "SELECT command, help, return_value FROM Commands");
		Database.addPreparedStatement("getCommand", "SELECT return_value, help FROM Commands WHERE command = ?");
		Database.addPreparedStatement("delCommand", "DELETE FROM Commands WHERE command = ?;");
		Database.addPreparedStatement("getCommands", "SELECT * FROM Commands");
		InputStream luain = getClass().getResourceAsStream("/jnlua/luasb.lua");
		try {
			luasb = CharStreams.toString(new InputStreamReader(luain, Charsets.UTF_8));
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		initLua();

		toggle_command = new Command("dyncmd", new CommandRateLimit(10), Permissions.MOD) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (params.equals("disable") || params.equals("enable")) {
					Helper.toggleCommand("dyncmd", target, params);
				} else {
					String isEnabled = Helper.isEnabledHere(target, "dyncmd") ? "enabled" : "disabled";
					Helper.sendMessage(target, "dyncmd is " + isEnabled + " in this channel", nick);
				}
				return new CommandChainStateObject();
			}
		};
		toggle_command.setHelpText("Dynamic command module");
		IRCBot.registerCommand(toggle_command);

		try {
			PreparedStatement searchCommands = Database.getPreparedStatement("searchCommands");
			ResultSet commands = searchCommands.executeQuery();
			while (commands.next()) {
				if (commands.getString(2) != null) {
					registerDynamicCommand(commands.getString(1), commands.getString(2));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			IRCBot.log.info("An error occurred while processing this command");
		}

		//<editor-fold desc="Alias into proper command sub-command structure">
		base_command = new Command("command", Permissions.TRUSTED) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, this.trySubCommandsMessage(params), nick);
				return new CommandChainStateObject();
			}
		};

		add = new Command("add", new CommandArgumentParser(2, new CommandArgument(ArgumentTypes.STRING, "Command"), new CommandArgument(ArgumentTypes.STRING, "Content")), Permissions.TRUSTED) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String cmd = this.argumentParser.getArgument("Command");
				String content = this.argumentParser.getArgument("Content");
				if (!IRCBot.dynamicCommands.containsKey(cmd)) {
					DbCommand item = new DbCommand(cmd, content, null);
					item.Save();
					event.respond("Command Added! Don't forget to set help text with " + Config.commandprefix + base_command.getCommand() + " " + addhelp.getCommand() + "!");
					registerDynamicCommand(cmd, content);
				} else {
					event.respond("Can't override existing commands.");
				}
				return new CommandChainStateObject();
			}
		};
		add.setHelpText("Adds a dynamic command.");

		del = new Command("del", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Command")), Permissions.TRUSTED) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String cmd = this.argumentParser.getArgument("Command");
				DbCommand item = DbCommand.GetByCommand(cmd);
				if (item != null) {
					item.Delete();
					event.respond("Command deleted");
					IRCBot.unregisterCommand(cmd);
				} else {
					event.respond("Unable to find command '" + cmd + "'");
				}
				return new CommandChainStateObject();
			}
		};
		del.registerAlias("delete");
		del.registerAlias("rem");
		del.registerAlias("remove");
		del.setHelpText("Removes a dynamic command.");

		addhelp = new Command("addhelp", new CommandArgumentParser(2, new CommandArgument(ArgumentTypes.STRING, "Command"), new CommandArgument(ArgumentTypes.STRING, "Text")), Permissions.TRUSTED) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				PreparedStatement addCommandHelp = Database.getPreparedStatement("addCommandHelp");
				String theCommand = this.argumentParser.getArgument("Command");
				String theHelp = this.argumentParser.getArgument("Text");
				if (IRCBot.dynamicCommands.containsKey(theCommand)) {
					try {
						addCommandHelp.setString(1, theHelp);
						addCommandHelp.setString(2, theCommand.toLowerCase());
						addCommandHelp.executeUpdate();
						IRCBot.dynamicCommands.get(theCommand).setHelpText(theHelp);
						event.respond("Help Set");
					} catch (SQLException e) {
						e.printStackTrace();
						event.respond("fail 1");
					}
				} else {
					event.respond("fail 2 ");
				}
				return new CommandChainStateObject();
			}
		};
		addhelp.registerAlias("sethelp");
		addhelp.registerAlias("help");
		addhelp.setHelpText("Sets help on dynamic commands");

		print = new Command("print", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Command")), Permissions.TRUSTED) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				try {
					String cmd = this.argumentParser.getArgument("Command");
					PreparedStatement getCommand = Database.getPreparedStatement("getCommand");
					getCommand.setString(1, cmd);
					ResultSet command1 = getCommand.executeQuery();
					if (command1.next()) {
						String message = command1.getString(1);
						Helper.sendMessage(target, message);
						if (!IRCBot.commands.containsKey(cmd))
							Helper.sendMessage(target, "Command is not registered!");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return new CommandChainStateObject();
			}
		};

		edit = new Command("edit", new CommandArgumentParser(2, new CommandArgument(ArgumentTypes.STRING, "Command"), new CommandArgument(ArgumentTypes.STRING, "Content")), Permissions.TRUSTED) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				PreparedStatement addCommand = Database.getPreparedStatement("addCommand");
				PreparedStatement getCommand = Database.getPreparedStatement("getCommand");
				String cmd = this.argumentParser.getArgument("Command").toLowerCase();
				String content = this.argumentParser.getArgument("Content");
				String[] message = params.split(" ", 2);
				getCommand.setString(1, cmd);
				ResultSet command1 = getCommand.executeQuery();
				if (command1.next()) {
					IRCBot.unregisterCommand(cmd);
					addCommand.setString(1, cmd);
					addCommand.setString(2, content);
					addCommand.executeUpdate();
					unregisterDynamicCommand(cmd);
					registerDynamicCommand(cmd, content);
					event.respond("Command Edited");
				} else {
					event.respond("Can't add new commands with edit!");
				}
				return new CommandChainStateObject();
			}
		};
		edit.registerAlias("update");
		edit.registerAlias("change");
		edit.registerAlias("set");

		alias = new Command("alias", Permissions.TRUSTED) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "To add an alias create a dynamic command with one or more commands to execute between two % like %command%.");
				return new CommandChainStateObject();
			}
		};

		placeholders = new Command("placeholders", Permissions.TRUSTED) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "Valid placeholders: %command% - Where 'command' is a different dyn-command. [randomitem] - Inserts a random item from the inventory. [drama] - ??. [argument] - The entire argument string. [nick] - The name of the caller. {n} - Where n is the number of an argument word starting at 0.");
				return new CommandChainStateObject();
			}
		};

		prefixes = new Command("prefixes", Permissions.TRUSTED) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "Valid prefixes: [js] - Attempts to parse the dyn-command contents as javascript. [lua] - Attempts to parse the dyn-command contents as Lua. [action] - Sends an ACTION instead of a normal message.");
				return new CommandChainStateObject();
			}
		};

		base_command.registerSubCommand(add);
		base_command.registerSubCommand(del);
		base_command.registerSubCommand(addhelp);
		base_command.registerSubCommand(print);
		base_command.registerSubCommand(edit);
		base_command.registerSubCommand(placeholders);
		base_command.registerSubCommand(prefixes);
		IRCBot.registerCommand(base_command);
		//</editor-fold>

		dynamicCommands = new ArrayList<>();
		try {
			PreparedStatement getCommands = Database.getPreparedStatement("getCommands");
			ResultSet resultSet = getCommands.executeQuery();
			int count = 0;
			while (resultSet.next()) {
				count++;
				registerDynamicCommand(resultSet.getString("command"), resultSet.getString("return_value"), resultSet.getString("help"));
			}
			System.out.println("Registered " + count + " dyn command" + (count == 1 ? "" : "s"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void registerDynamicCommand(String command, String content) {
		registerDynamicCommand(command, content, null);
	}

	public static void registerDynamicCommand(String command, String content, String helpText) {
		System.out.println("Register dynamic command '" + command + "'");
		DynamicCommand dynCmd = new DynamicCommand(command, content, new CommandArgumentParser(0, new CommandArgument(ArgumentTypes.STRING, "Target"), new CommandArgument(ArgumentTypes.STRING, "Params")));
		if (helpText != null && !helpText.equals(""))
			dynCmd.setHelpText(helpText);
		else
			dynCmd.setHelpText(defaultHelpText);
//		dynamicCommands.add(dynCmd.getCommand());
		IRCBot.registerCommand(dynCmd);
	}

	public static void unregisterDynamicCommand(String command) {
		IRCBot.unregisterCommand(command);
	}

	public static ArrayList<String> parseDynCommandAliases(String[] input, CommandsHaveBeenRun excludeList) {
		ArrayList<String> commands = new ArrayList<>();
		String aliasPattern = "%(\\w*?)%";
		Pattern pattern = Pattern.compile(aliasPattern);
		Matcher matcher = pattern.matcher(input[0]);

		while (matcher.find()) {
			for (int i = 1; i <= matcher.groupCount(); i++) {
				String match = matcher.group(i);
				System.out.println("Match alias '" + match + "'");
				if (!excludeList.hasCommand(match)) {
					commands.add(match);
				}
				input[0] = input[0].replace("%" + match + "%", "");
			}
		}
		return commands;
	}

	public static String parseDynCommandPlaceholders(String input, String user, String params) {
		System.out.println("Parsing DynTags in: '" + input + "'");
		if (input.contains("[randomitem]")) {
			ArrayList<DbInventoryItem> items = DbInventoryItem.GetRandomItems(1);
			if (items.size() == 1)
				input = input.replace("[randomitem]", items.get(0).item_name);
		}
		if (input.contains("[drama]")) {
			input = input.replace("[drama]", Drama.dramaParse());
		}
		if (input.contains("[argument]")) {
			input = input.replaceAll("\\[argument\\]", String.join(" ", params));
		}
		if (input.contains("[nick]")) {
			input = input.replaceAll("\\[nick\\]", user);
		}
		if (input.startsWith("[lua]")) {
			output = new StringBuilder();
			output.append(runScriptInSandbox(input.replace("[lua]", "").trim()));
			input = output.toString();
		} else if (input.startsWith("[js]")) {
			if (engineFactory == null) return input;
			NashornScriptEngine engine = (NashornScriptEngine) engineFactory.getScriptEngine(new String[]{"-strict", "--no-java", "--no-syntax-extensions"});
			output = new StringBuilder();
			output.append(eval(engine, input.replace("[js]", "").trim()));
			if (output.length() > 0 && output.charAt(output.length() - 1) == '\n')
				output.setLength(output.length() - 1);
			input = output.toString().replace("\n", " | ").replace("\r", "");
		}
		return input;
	}

	public static void parseDynCommand(String command, String user, String target, String[] arguments) {
		parseDynCommand(command, user, target, arguments, new CommandsHaveBeenRun());
	}

	public static void parseDynCommand(String command, String user, String target, String[] arguments, CommandsHaveBeenRun excludeList) {
		String prefix = Config.commandprefix;

		if (excludeList.hasCommand(command))
			return;

		ArrayList<String> commandAliases;
		DbCommand com = DbCommand.GetByCommand(command.replace(prefix, "").toLowerCase());
		if (com != null) {
			String message = com.return_value;
			String[] msg = new String[]{message};
			commandAliases = parseDynCommandAliases(msg, excludeList);

			message = parseDynCommandPlaceholders(msg[0], user, String.join(" ", arguments));

			message = PotionHelper.replaceParamsInEffectString(message);

			try {
				message = MessageFormat.format(message, (Object[]) arguments);
			} catch (Exception ignored) {
			}

			Helper.AntiPings = Helper.getNamesFromTarget(target);
//			System.out.println("This is what's left after aliases: '" + message.replaceAll(" ", "") + "'");
			if (!message.replaceAll(" ", "").equals("")) {
				if (message.startsWith("[action]")) {
					message = message.replace("[action]", "");
					Helper.sendAction(target, message);
				} else {
					Helper.sendMessage(target, message, null, true);
				}
			} else {
				System.out.println("Message empty in command '" + command + "'. Skipping sendMessage.");
			}
			for (String cmd : commandAliases) {
				System.out.println("Execute command from alias '" + cmd + "'");
				parseDynCommand(cmd, user, target, arguments, excludeList);
				excludeList.addCommand(cmd);
			}
		} else {
			System.out.println("No dynCommand '" + command + "' found.");
		}
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
		luaState = new LuaState(1024 * 1024);
		luaState.openLib(Library.BASE);
		luaState.openLib(Library.COROUTINE);
		luaState.openLib(Library.TABLE);
		luaState.openLib(Library.STRING);
		luaState.openLib(Library.BIT32);
		luaState.openLib(Library.MATH);
		luaState.openLib(Library.OS);
		luaState.openLib(Library.PACKAGE);
		luaState.openLib(Library.DEBUG);
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

	public static String eval(NashornScriptEngine engine, String code) {
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
		} catch (TimeoutException e) {
			output = "Script timed out";
		} catch (Exception e) {
			e.printStackTrace();
			output = e.getMessage();
		} finally {
			service.shutdown();
		}
		if (output == null)
			output = "";

		return output;
	}

	public static class JSRunner implements Callable<String> {

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
			} catch (ScriptException ex) {
				return ex.getMessage();
			}
			return null;
		}
	}

}
