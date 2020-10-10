package pcl.lc.utils;

import pcl.lc.irc.Config;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class CommandHelper {
	/**
	 *
	 * @param input A string containing a message to be searched for a command
	 * @return Finds the first word that begins with the configured command prefix in the given input string, returns an ArrayList containing zero or more ArrayLists each containing the command (including prefix) followed by the parameters
	 */
	public static ArrayList<ArrayList<String>> findCommandInString(String input) {
		ArrayList<ArrayList<String>> commands = new ArrayList<>();
		ArrayList<String> currentCommand = null;
		String[] splt = input.split(" ");
		for (String in : splt) {
			if (in.startsWith(Config.commandprefix)) {
				if (currentCommand != null && currentCommand.size() > 0)
					commands.add(currentCommand);
				currentCommand = new ArrayList<>();
			}
			if (currentCommand != null) {
				currentCommand.add(in);
			}
		}
		if (currentCommand.size() > 0)
			commands.add(currentCommand);
		System.out.println("Found " + commands.size() + " commands!");
		return commands;
	}
}
