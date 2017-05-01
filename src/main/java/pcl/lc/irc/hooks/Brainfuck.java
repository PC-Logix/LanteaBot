package pcl.lc.irc.hooks;

import java.util.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import org.faabtech.brainfuck.BrainfuckEngine;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

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
		IRCBot.registerCommand(local_command, "Brainfuck!");
	}

	private void initCommands() {
		local_command = new Command("bf", 0);
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

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		StringBuilder builder = new StringBuilder();
		for(String s : copyOfRange) {
		    builder.append(s);
		}
		event.respond(parse(builder.toString(), null));
	}

	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		// TODO Auto-generated method stub
		
	}
}