package pcl.lc.irc.entryClasses;

import java.util.ArrayList;

public class CommandArgument {
	public String name;
	public String description;
	public String type;
	public String arg;
	public ArrayList<String> argList;

	public CommandArgument(String type) {
		this.name = null;
		this.description = null;
		this.type = type;
	}

	public CommandArgument(String type, String name) {
		this.name = name;
		this.description = null;
		this.type = type;
	}

	public CommandArgument(String type, String name, String description) {
		this.name = name;
		this.description = description;
		this.type = type;
		this.arg = null;
		this.argList = new ArrayList<>();
	}
}
