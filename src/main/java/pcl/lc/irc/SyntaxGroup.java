package pcl.lc.irc;

import java.util.ArrayList;
import java.util.Arrays;

public class SyntaxGroup {
	public ArrayList<SyntaxGroup> syntax;
	public boolean groupRequired;
	public String argument;
	public boolean required;
	public boolean staticWord;
	public String description;
	public boolean remainderOfArguments;

	public SyntaxGroup() {
		this.syntax = new ArrayList<>();
		this.groupRequired = true;
	}

	public SyntaxGroup(String argument) {
		this(argument, "");
	}

	public SyntaxGroup(String argument, String description) {
		this(argument, description, true, false);
	}

	public SyntaxGroup(String argument, String description, boolean required, boolean staticWord) {
		this.syntax = new ArrayList<>();
		this.groupRequired = true;
		this.argument = argument;
		this.required = required;
		this.staticWord = staticWord;
		this.description = description;
	}

	public SyntaxGroup(ArrayList<SyntaxGroup> syntax) {
		this(syntax, true);
	}

	public SyntaxGroup(ArrayList<SyntaxGroup> syntax, boolean groupRequired) {
		this.syntax = syntax;
		this.groupRequired = groupRequired;
	}

	public void groupRequired(boolean isRequired) {
		this.groupRequired = isRequired;
	}

	public void addSubArgument(String argument, String description, boolean required, boolean staticWord) {
		this.syntax.add(new SyntaxGroup(argument, description, required, staticWord));
	}

	public String[] getComponentsForPrint() {
		return getComponentsForPrint(true);
	}

	public String[] getComponentsForPrint(boolean first) {
		StringBuilder strSyntax = new StringBuilder();
		ArrayList<String> strDesc = new ArrayList<>();
		if (this.syntax != null && this.syntax.size() != 0 && !first) {
			if (this.groupRequired)
				strSyntax.append("<");
			else
				strSyntax.append("[");
		}
		if (this.argument != null) {
			if (this.staticWord)
				strSyntax.append(this.argument);
			else {
				if (this.required)
					strSyntax.append("<").append(this.argument).append(">");
				else
					strSyntax.append("[").append(this.argument).append("]");
			}
		}
		if (this.argument != null && this.description != null)
			strDesc.add(this.description.replace("{arg}", this.argument));
		for (SyntaxGroup grp : this.syntax) {
			if (strSyntax.length() != 0)
				strSyntax.append(" ");
			String[] group = grp.getComponentsForPrint(false);
			strSyntax.append(group[0]);
			strDesc.add(group[1]);
		}
		if (this.syntax != null && this.syntax.size() != 0 && !first) {
			if (this.groupRequired)
				strSyntax.append(">");
			else
				strSyntax.append("]");
		}
		return new String[] { strSyntax.toString() , String.join(" ", strDesc) };
	}

	public String print() {
		String[] group = getComponentsForPrint();
		return group[0] + " - " + group[1];
	}

	public String getArgumentByName(String argumentName, String arguments) {
		ArrayList<String> args = new ArrayList<>(Arrays.asList(arguments.split(" ")));
		return getArgumentByName(argumentName, args);
	}

	public String getArgumentByName(String argumentName, ArrayList<String> arguments) {
		return getArgumentByName(argumentName, arguments, 0);
	}

	public String getArgumentByName(String argumentName, ArrayList<String> arguments, int index) {
		System.out.println("Working index " + index);
		if (this.argument.equals(argumentName)) {
			System.out.println("Match '" + argumentName + "'! Index: " + index);
			if (this.remainderOfArguments) {
				ArrayList<String> pop = new ArrayList<>();
				for (int i = index; i < arguments.size(); i++) {
					pop.add(arguments.get(i));
				}
				return String.join(" ", pop);
			} else {
				return arguments.get(index);
			}
		} else {
			for (SyntaxGroup snt : this.syntax) {
				index++;
				String ret = snt.getArgumentByName(argumentName, arguments, index);
				if (ret != null)
					return ret;
			}
		}
		return null;
	}
}
