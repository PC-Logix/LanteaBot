package pcl.lc.irc.hooks;

import java.util.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.faabtech.brainfuck.BrainfuckEngine;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;
import pcl.lc.utils.ZeroInputStream;
public class Brainfuck extends AbstractListener {

	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
	}

	private void initCommands() {
		local_command = new Command("bf") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, parse(params, null));
			}
		}; local_command.setHelpText("Does brainfuck");
		IRCBot.registerCommand(local_command);
	}
	public String parse(String code, String message) {
		if (code == null) return "";
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			InputStream is;
			if (message == null)
				is = new ZeroInputStream();
			else
				is = new ByteArrayInputStream(message.getBytes());
			BrainfuckEngine bfe = new BrainfuckEngine(code.length(),os,is);
			bfe.interpret(code);
			return os.toString("UTF-8");
		} catch (Exception e) {e.printStackTrace();}
		return "";
	}
}