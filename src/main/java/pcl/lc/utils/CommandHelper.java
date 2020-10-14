package pcl.lc.utils;

import pcl.lc.irc.Config;

import java.util.ArrayList;
import java.util.Hashtable;

public class CommandHelper {
	/**
	 *
	 * @param input A string containing a message to be searched for a command
	 * @return Finds the first word that begins with the configured command prefix in the given input string, returns an ArrayList containing zero or more ArrayLists each containing the command (including prefix) followed by the parameters
	 */
	public static ArrayList<ArrayList<String>> findCommandInString(String input) {
		Hashtable<String, Integer> distinctCounter = new Hashtable<>();
		ArrayList<ArrayList<String>> commands = new ArrayList<>();
		ArrayList<String> currentCommand = null;
		String[] splt = input.split(" ");
		boolean ignoreCurrentCommand = false;
		for (String in : splt) {
			if (in.startsWith(Config.commandprefix)) {
				if (!distinctCounter.containsKey(in))
					distinctCounter.put(in, 0);
				if (distinctCounter.get(in) < Config.maxNumberOfCommandsPerMessage) {
					distinctCounter.put(in, distinctCounter.get(in) + 1);
					ignoreCurrentCommand = false;
					if (currentCommand != null && currentCommand.size() > 0)
						commands.add(currentCommand);
					currentCommand = new ArrayList<>();
				} else {
					ignoreCurrentCommand = true;
				}
			}
			if (currentCommand != null && !ignoreCurrentCommand) {
				currentCommand.add(in);
			}
		}
		if (currentCommand.size() > 0)
			commands.add(currentCommand);
		System.out.println("Found " + commands.size() + " commands!");
		return commands;
	}
}
