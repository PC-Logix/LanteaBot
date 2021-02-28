package pcl.lc.irc.hooks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.faabtech.brainfuck.BrainfuckEngine;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.Helper;
import pcl.lc.utils.ZeroInputStream;

public class Brainfuck extends AbstractListener {

	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
	}

	private void initCommands() {
		local_command = new Command("bf", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING))) {
			@Override
			public CommandChainState onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				Helper.sendMessage(target, parse(this.argumentParser.getArgument(0), null));
				return CommandChainState.FINISHED;
			}
		};
		local_command.setHelpText("Does brainfuck");
		local_command.registerAlias("brainfuck");
		IRCBot.registerCommand(local_command);
	}

	public String parse(String code, String message) throws Exception {
		if (code == null) return "";
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		InputStream is;
		if (message == null)
			is = new ZeroInputStream();
		else
			is = new ByteArrayInputStream(message.getBytes());
		BrainfuckEngine bfe = new BrainfuckEngine(code.length(), os, is);
		bfe.interpret(code);
		return os.toString("UTF-8");
	}
}