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
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.*;
import pcl.lc.utils.*;
import pcl.lc.utils.db_items.CommandItem;
import pcl.lc.utils.db_items.InventoryItem;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Caitlyn
 */
@SuppressWarnings("rawtypes")
public class DynamicCommands extends AbstractListener {
	static ArrayList<String> dynamicCommands;
	private static NashornScriptEngineFactory engineFactory = new NashornScriptEngineFactory();;
	private static final SandboxThreadGroup sandboxGroup = new SandboxThreadGroup("javascript");
	private static final ThreadFactory sandboxFactory = new SandboxThreadFactory(sandboxGroup);

	private Command local_command_add;
	private Command local_command_del;
	private Command local_command_addhelp;
	private Command local_command_print;
	private Command local_command_edit;
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
		Database.addPreparedStatement("searchCommands", "SELECT command, help FROM Commands");
		Database.addPreparedStatement("getCommand", "SELECT return_value, help FROM Commands WHERE command = ?");
		Database.addPreparedStatement("delCommand", "DELETE FROM Commands WHERE command = ?;");
		Database.addPreparedStatement("getCommands", "SELECT * FROM Commands");
		InputStream luain = getClass().getResourceAsStream("/jnlua/luasb.lua");
		try {
			luasb = CharStreams.toString(new InputStreamReader(luain, Charsets.UTF_8));
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		initLua();
		local_command_add = new Command("addcommand", Permissions.TRUSTED) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				try {
					System.out.println("Received params '" + String.join(" ", params) + "'");
					String cmd = params.remove(0).toLowerCase();
					String content = String.join(" ", params);
					if (!IRCBot.commands.containsKey(cmd)) {
						CommandItem item = new CommandItem(cmd, content, null);
						item.Save();
						event.respond("Command Added! Don't forget to set help text with " + local_command_addhelp.getCommand() + "!");
//						IRCBot.registerCommand(cmd, "Dynamic commands module, who knows what it does?!");
						dynamicCommands.add(cmd);
					}
					else {
						event.respond("Can't override existing commands.");
					}
				}
				catch (Exception e) {
					Helper.sendMessage(target, "An error occurred while processing this command");
					e.printStackTrace();
				}
			}
		};
		IRCBot.registerCommand(local_command_add, "Adds a dynamic command to the bot, requires BotAdmin, or Channel Op.");
		local_command_del = new Command("delcommand", Permissions.TRUSTED) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				if (params.size() == 0) {
					Helper.sendMessage(target, "Specify command to delete", nick);
					return;
				}
				try {
					String cmd = params.remove(0).toLowerCase();
					CommandItem item = CommandItem.GetByCommand(cmd);
					if (item != null)
						item.Delete();
					event.respond("Command deleted");
//					IRCBot.unregisterCommand(cmd);
					dynamicCommands.remove(cmd);
				}
				catch (Exception e) {
					e.printStackTrace();
					event.respond("An error occurred while processing this command");
				}
			}
		};
		local_command_print = new Command ("printcommand") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				PreparedStatement getCommand;
				try {
					getCommand = Database.getPreparedStatement("getCommand");
					getCommand.setString(1, params.toLowerCase());
					ResultSet command1 = getCommand.executeQuery();
					if (command1.next()) {
						String message = command1.getString(1);
						Helper.sendMessage(target, message);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		};
		local_command_edit = new Command ("editcommand", Permissions.TRUSTED) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					PreparedStatement addCommand = Database.getPreparedStatement("addCommand");
					PreparedStatement getCommand= Database.getPreparedStatement("getCommand");
					String[] message = params.split(" ", 2);
					getCommand.setString(1, message[0].toLowerCase());
					ResultSet command1 = getCommand.executeQuery();
					if (command1.next()) {
						IRCBot.unregisterCommand(message[0].toLowerCase());
						addCommand.setString(1, message[0].toLowerCase());
						addCommand.setString(2, message[1]);
						addCommand.executeUpdate();
						event.respond("Command Edited");
						IRCBot.registerCommand(message[0].toLowerCase(), command1.getString(2));
					}
					else {
						event.respond("Can't add new commands with edit!");
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					event.respond("An error occurred while processing this command");
				}
			}
		};
		local_command_addhelp = new Command ("addcommandhelp", Permissions.TRUSTED) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				PreparedStatement addCommandHelp;
				try {
					addCommandHelp = Database.getPreparedStatement("addCommandHelp");
					String arr[] = params.split(" ", 2);
					String theCommand = arr[0];
					String theHelp = arr[1]; 
					if (IRCBot.commands.containsKey(theCommand)) {
						try {
							addCommandHelp.setString(1, theHelp);
							addCommandHelp.setString(2, theCommand.toLowerCase());
							addCommandHelp.executeUpdate();
							IRCBot.setHelp(theCommand, theHelp);
							event.respond("Help Set");
						} catch (SQLException e) {
							e.printStackTrace();
							event.respond("fail 1");
						}
					} else {
						event.respond("fail 2 ");
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					event.respond("fail 3");
				}
			}
		};
		local_command_addhelp.setHelpText("Sets help on dynamic commands");
		IRCBot.registerCommand(local_command_del, "Removes a dynamic command to the bot, requires BotAdmin, or Channel Op.");
		IRCBot.registerCommand(local_command_addhelp);

		
		toggle_command = new Command("dyncmd", new CommandRateLimit(10), Permissions.MOD) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (params.equals("disable") || params.equals("enable")) {
					Helper.toggleCommand("dyncmd", target, params);
				} else {
					String isEnabled = Helper.isEnabledHere(target, "dyncmd") ? "enabled" : "disabled";
					Helper.sendMessage(target, "dyncmd is " + isEnabled + " in this channel", nick);
				}
			}
		}; toggle_command.setHelpText("Dynamic command module");
		IRCBot.registerCommand(toggle_command);
		
		try {
			PreparedStatement searchCommands = Database.getPreparedStatement("searchCommands");
			ResultSet commands = searchCommands.executeQuery();
			while (commands.next()) {
				if (commands.getString(2) != null) {
					IRCBot.registerCommand(commands.getString(1), commands.getString(2));
				} else {
					IRCBot.registerCommand(commands.getString(1), "Dynamic commands module, who knows what it does?!");
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			IRCBot.log.info("An error occurred while processing this command");
		}

		//<editor-fold desc="Alias into proper command sub-command structure">
		base_command = new Command("command", Permissions.TRUSTED) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, this.trySubCommandsMessage(params), nick);
			}
		};

		add = new Command("add", local_command_add.getPermissionLevel()) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				System.out.println("Send '" + params + "' to add command");
				local_command_add.forceExecute(nick, target, event, params.split(" "));
			}
		};

		del = new Command("del", local_command_del.getPermissionLevel()) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				local_command_del.forceExecute(nick, target, event, params.split(" "));
			}
		};
		del.registerAlias("delete");
		del.registerAlias("rem");
		del.registerAlias("remove");

		addhelp = new Command("addhelp", local_command_addhelp.getPermissionLevel()) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				local_command_addhelp.forceExecute(nick, target, event, params.split(" "));
			}
		};

		print = new Command("print", local_command_print.getPermissionLevel()) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				local_command_print.forceExecute(nick, target, event, params.split(" "));
			}
		};

		edit = new Command("edit", local_command_edit.getPermissionLevel()) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				local_command_edit.forceExecute(nick, target, event, params.split(" "));
			}
		};

		alias = new Command("alias", Permissions.TRUSTED) {
            @Override
            public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
                Helper.sendMessage(target, "To add an alias create a dynamic command with one or more commands to execute between two % like %command%.");
            }
        };

		placeholders = new Command("placeholders", Permissions.TRUSTED) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "Valid placeholders: %command% - Where 'command' is a different dyn-command. [randomitem] - Inserts a random item from the inventory. [drama] - ??. [argument] - The entire argument string. [nick] - The name of the caller. {n} - Where n is the number of an argument word starting at 0.");
			}
		};

		prefixes = new Command("prefixes", Permissions.TRUSTED) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "Valid prefixes: [js] - Attempts to parse the dyn-command contents as javascript. [lua] - Attempts to parse the dyn-command contents as Lua. [action] - Sends an ACTION instead of a normal message.");
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
				String command = resultSet.getString("command");
				System.out.println("Register dyncommand '" + command + "'");
				Command dynCmd = new Command(command) {
//					final public String message = resultSet.getString("return_value");
					@Override
					public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
						try {
							System.out.println("Executing dyn command (String)");
							Helper.sendMessage(target, "This is a placeholder message");
						} catch (Exception e) {
							e.printStackTrace();
							Helper.sendMessage(target, "Something went wrong.", nick);
						}
					}
				};
				String help = resultSet.getString("help");
				if (help != null && !help.equals("")) {
					dynCmd.setHelpText(help);
					System.out.println("Set dyncommand help to '" + help + "'");
				}
				dynamicCommands.add(dynCmd.getCommand());
			}
			System.out.println("Registered " + count  + " dyn command" + (count == 1 ? "" : "s"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void parseDynCommandAliases(String input, CommandsHaveBeenRun excludeList) {
		StringBuilder output;
		String aliasPattern = "%([a-zA-Z0-9]*?)%";
		Pattern pattern = Pattern.compile(aliasPattern);
		Matcher matcher = pattern.matcher(input);

		while (matcher.find()) {
			for (int i = 1; i <= matcher.groupCount(); i++) {
				String match = matcher.group(i);
				if (!excludeList.hasCommand(match)) {
					excludeList.addCommand(match);
					parseDynCommandAliases(match, excludeList);
				}
				input = input.replace("%" + match + "%", "");
			}
		}
	}

	public static String parseDynCommandPlaceholders(String input, String user, String params) {

		System.out.println("Done with aliases: '" + input + "'");
		if (input.startsWith("[lua]")) {
			output = new StringBuilder();
			output.append(runScriptInSandbox(input.replace("[lua]", "").trim()));
			input = output.toString();
		} else if (input.startsWith("[js]")) {
			if (engineFactory == null) return input;
			NashornScriptEngine engine = (NashornScriptEngine)engineFactory.getScriptEngine(new String[] {"-strict", "--no-java", "--no-syntax-extensions"});
			output = new StringBuilder();
			output.append(eval(engine, input.replace("[js]", "").trim()));
			if (output.length() > 0 && output.charAt(output.length()-1) == '\n')
				output.setLength(output.length()-1);
			input = output.toString().replace("\n", " | ").replace("\r", "");
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
		return input;
	}

	public static void parseDynCommand(String command, String user, String target, String[] arguments){
		String prefix = Config.commandprefix;

		CommandItem com = CommandItem.GetByCommand(command.replace(prefix, "").toLowerCase());
		if (com != null) {
			String message = com.return_value;
			parseDynCommandAliases(message, new CommandsHaveBeenRun());

			message = parseDynCommandPlaceholders(message, user, String.join(" ", arguments));

			message = PotionHelper.replaceParamsInEffectString(message);

			try {
				message = MessageFormat.format(message, (Object[]) arguments);
			} catch (Exception ignored) {}

			Helper.AntiPings = Helper.getNamesFromTarget(target);
			System.out.println("This is what's left: '" + message.replaceAll(" ", "") + "'");
			if (message.replaceAll(" ", "").equals(""))
				return;
			if (message.startsWith("[action]")) {
				message = message.replace("[action]", "");
				Helper.sendAction(target, message);
			} else {
				Helper.sendMessage(target, message, user, true);
			}
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
		luaState = new LuaState(1024*1024);
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
			}
			catch(ScriptException ex) {
				return ex.getMessage();
			}
			return null;
		}
	}

}