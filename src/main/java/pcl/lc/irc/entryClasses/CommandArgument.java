package pcl.lc.irc.entryClasses;

import java.util.ArrayList;

public class CommandArgument {
	public String name;
	public String description;
	public String type;
	public String arg;
	public ArrayList<String> argList;

	/**
	 *
	 * @param type The argument type, see docs of CommandArgumentParser constructor for list
	 */
	public CommandArgument(String type) {
		this.name = null;
		this.description = null;
		this.type = type;
	}

	/**
	 *
	 * @param type The argument type, see docs of CommandArgumentParser constructor for list
	 * @param name The argument name displayed in command help contexts
	 */
	public CommandArgument(String type, String name) {
		this.name = name;
		this.description = null;
		this.type = type;
	}

	/**
	 *
	 * @param type The argument type, see docs of CommandArgumentParser constructor for list
	 * @param name The argument name displayed in context help strings
	 * @param description Argument description displayed in command help contexts
	 */
	public CommandArgument(String type, String name, String description) {
		this.name = name;
		this.description = description;
		this.type = type;
		this.arg = null;
		this.argList = new ArrayList<>();
	}
}
