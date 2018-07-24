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
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;
import pcl.lc.utils.SandboxThreadFactory;
import pcl.lc.utils.SandboxThreadGroup;

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
	private NashornScriptEngineFactory engineFactory = new NashornScriptEngineFactory();;
	private final SandboxThreadGroup sandboxGroup = new SandboxThreadGroup("javascript");
	private final ThreadFactory sandboxFactory = new SandboxThreadFactory(sandboxGroup);

	private Command local_command_add;
	private Command local_command_del;
	private Command local_command_addhelp;

	private Command local_command_print;
	private Command local_command_edit;

	private Command toggle_command;
	
	public String luasb;

	private static LuaState luaState;
	public StringBuilder output;

	public class CommandsHaveBeenRun {
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
		Database.addPreparedStatement("addCommand", "INSERT OR REPLACE INTO Commands(command, return) VALUES (?, ?);");
		Database.addPreparedStatement("addCommandHelp", "UPDATE Commands SET help = ? WHERE command = ?");
		Database.addPreparedStatement("searchCommands", "SELECT command, help FROM Commands");
		Database.addPreparedStatement("getCommand", "SELECT return, help FROM Commands WHERE command = ?");
		Database.addPreparedStatement("delCommand", "DELETE FROM Commands WHERE command = ?;");
		InputStream luain = getClass().getResourceAsStream("/jnlua/luasb.lua");
		try {
			luasb = CharStreams.toString(new InputStreamReader(luain, Charsets.UTF_8));
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		initLua();
		local_command_add = new Command("addcommand", 0);
		IRCBot.registerCommand(local_command_add, "Adds a dynamic command to the bot, requires BotAdmin, or Channel Op.");
		local_command_del = new Command("delcommand", 0);
		local_command_print = new Command ("printcommand", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				PreparedStatement getCommand;
				try {
					if (!Helper.isEnabledHere(target, "dyncmd")) {
						return;
					}
					getCommand = Database.getPreparedStatement("getCommand");
					getCommand.setString(1, params.toLowerCase());
					ResultSet command1 = getCommand.executeQuery();
					if (command1.next()) {
						String message = command1.getString(1);
						Helper.sendMessage(target, message);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		local_command_edit = new Command ("editcommand", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					if (!Helper.isEnabledHere(target, "dyncmd")) {
						return;
					}
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
		local_command_addhelp = new Command ("addcommandhelp", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (!Helper.isEnabledHere(target, "dyncmd")) {
					return;
				}
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
							// TODO Auto-generated catch block
							e.printStackTrace();
							event.respond("fail 1");
						}
					} else {
						event.respond("fail 2 ");
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					event.respond("fail 3");
				}
			}
		};
		local_command_addhelp.setHelpText("Sets help on dynamic commands");
		IRCBot.registerCommand(local_command_del, "Removes a dynamic command to the bot, requires BotAdmin, or Channel Op.");
		IRCBot.registerCommand(local_command_addhelp);

		
		toggle_command = new Command("dyncmd", 10, Permissions.MOD) {
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
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		target = Helper.getTarget(event);
		if (!Helper.isEnabledHere(target, "dyncmd")) {
			return;
		}
		try {
			CommandsHaveBeenRun blacklist = new CommandsHaveBeenRun();
			parseDynCommand(command, copyOfRange, nick, blacklist);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("An error occurred while processing this command");
		}
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {

		String ourinput = event.getMessage().toLowerCase().trim();
		chan = event.getChannel().getName();
		target = Helper.getTarget(event);
		local_command_addhelp.tryExecute(command, sender, event.getChannel().getName(), event, args);
		local_command_print.tryExecute(command, sender, event.getChannel().getName(), event, args);
		local_command_edit.tryExecute(command, sender, event.getChannel().getName(), event, args);
		toggle_command.tryExecute(command, sender, event.getChannel().getName(), event, args);
		if (ourinput.length() > 1) {
			if (!IRCBot.isIgnored(event.getUser().getNick())) {
				String[] message = event.getMessage().split(" ", 3);

				long shouldExecute = local_command_add.shouldExecute(command, event);
				if (shouldExecute == 0) {
					boolean isOp = Permissions.isOp(event.getBot(), event.getUser());
					if (isOp || Helper.isChannelOp(event)) {
						try {
							PreparedStatement addCommand = Database.getPreparedStatement("addCommand");
							if (!IRCBot.commands.containsKey(message[1])) {
								addCommand.setString(1, message[1].toLowerCase());
								addCommand.setString(2, message[2]);
								addCommand.executeUpdate();
								event.respond("Command Added");
								IRCBot.registerCommand(message[1].toLowerCase(), "Dynamic commands module, who knows what it does?!");
							}
							else {
								event.respond("Can't override existing commands.");
							}
						}
						catch (Exception e) {
							e.printStackTrace();
							event.respond("An error occurred while processing this command");
						}
					}
				}
				else {
					shouldExecute = local_command_del.shouldExecute(command, event);
					if (shouldExecute == 0) {
						boolean isOp = Permissions.isOp(event.getBot(), event.getUser());
						if (isOp || Helper.isChannelOp(event)) {
							try {
								PreparedStatement delCommand = Database.getPreparedStatement("delCommand");
								delCommand.setString(1, message[1].toLowerCase());
								delCommand.execute();
								event.respond("Command deleted");
								IRCBot.unregisterCommand(message[1].toLowerCase());
							}
							catch (Exception e) {
								e.printStackTrace();
								event.respond("An error occurred while processing this command");
							}
						}
					}
				}
			}
		}
	}

	private void parseDynCommand(String command, String[] arguments, String nick, CommandsHaveBeenRun blacklist) throws Exception {
		String prefix = Config.commandprefix;

		PreparedStatement getCommand = Database.getPreparedStatement("getCommand");
		getCommand.setString(1, command.replace(prefix, "").toLowerCase());
		ResultSet command1 = getCommand.executeQuery();
		if (command1.next()) {
			String message = command1.getString(1);
		
			StringBuilder output;
			String aliasPattern = "%(.*?)%";
			Pattern pattern = Pattern.compile(aliasPattern);
			Matcher matcher = pattern.matcher(message);

			while (matcher.find()) {
				for (int i = 1; i <= matcher.groupCount(); i++) {
					String match = matcher.group(i);
					if (!blacklist.hasCommand(match)) {
						blacklist.addCommand(match);
						parseDynCommand(match, arguments, nick, blacklist);
					}
					message = message.replace("%" + match + "%", "");
				}
			}

			System.out.println("Done with aliases: '" + message + "'");

			if (message.startsWith("[lua]")) {
				output = new StringBuilder();
				output.append(runScriptInSandbox(message.replace("[lua]", "").trim()));
				message = output.toString();
			}else if (message.startsWith("[js]")) {
				if (engineFactory == null) return;
				NashornScriptEngine engine = (NashornScriptEngine)engineFactory.getScriptEngine(new String[] {"-strict", "--no-java", "--no-syntax-extensions"});
				output = new StringBuilder();
				output.append(eval(engine, message.replace("[js]", "").trim()));
				if (output.length() > 0 && output.charAt(output.length()-1) == '\n')
					output.setLength(output.length()-1);
				message = output.toString().replace("\n", " | ").replace("\r", "");
			}
			String msg = "";
			if (message.contains("[randomitem]")) {
				message = msg.replace("[randomitem]", Inventory.getRandomItem().getName());
			}
			if (message.contains("[drama]")) {
				message = msg.replace("[drama]", Drama.dramaParse());
			}
			if (message.contains("[nick]")) {
				message = message.replaceAll("\\[nick\\]", nick);
			}
			message = MessageFormat.format(message, (Object[]) arguments);
			Helper.AntiPings = Helper.getNamesFromTarget(target);
			System.out.println("This is what's left: '" + message.replaceAll(" ", "") + "'");
			if (message.replaceAll(" ", "").equals(""))
				return;
			if (message.startsWith("[action]")) {
				message = message.replace("[action]", "");
				Helper.sendAction(target, message);
			} else {
				Helper.sendMessage(target, message, nick, true);
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