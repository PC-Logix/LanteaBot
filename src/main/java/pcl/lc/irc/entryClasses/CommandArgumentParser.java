package pcl.lc.irc.entryClasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandArgumentParser {
	public final int requiredFirstNum;
	public final ArrayList<CommandArgument> arguments;

	Pattern patternEscapedString = Pattern.compile("^\"(.*?)\"");
	Pattern patternString = Pattern.compile("^([\\w-.:;\\\\/^]*)");
	Pattern patternInteger = Pattern.compile("^(\\d+) ");
	Pattern patternDouble = Pattern.compile("^(\\d+\\.?\\d*)");
	Pattern patternBoolean = Pattern.compile("^(true|false|1|0)");

	/**
	 *
	 * @param required The number of required arguments from the start of the list. A value of 0 or less indicates no arguments are required.
	 * @param arguments Specify each argument type in sequence using one of the following types:
	 *                  `String`  - Matches a single word containing [a-zA-Z0-9_-.], or multiple words encased in unescaped double-quotes such as "This is my argument" which can contain any character except un-escaped double quotes.
	 *                  `Integer` -	Matches an integer value such as 1 or 1112
	 *                  `Double`  - Matches an integer value or a double value such as 1.1
	 *                  `Boolean` - Matches the strings "true" or "false", or the integer values 1 or 0
	 *                  `List`		- Parses any remaining arguments as String, creating an ArrayList gettable with getList. This causes any additional CommandArguments after this one to be ignored.
	 */
	public CommandArgumentParser(int required, CommandArgument... arguments) {
		this.requiredFirstNum = required;
		this.arguments = new ArrayList<>();
		this.arguments.addAll(Arrays.asList(arguments));
	}

	public int parseArguments(ArrayList<String> arguments) {
		return parseArguments(String.join(" ", arguments));
	}

	public int parseArguments(String[] arguments) {
		return parseArguments(String.join(" ", arguments));
	}

	public int parseArguments(String arguments) {
		for (CommandArgument argType : this.arguments) {
			argType.arg = null;
			argType.argList = new ArrayList<>();
		}
		int argumentCount = 0;
		for (CommandArgument argType : this.arguments) {
			if (argType.type.equals("Integer")) {
				Matcher matcher = patternInteger.matcher(arguments);
				if (matcher.find()) {
//					System.out.print("`" + arguments + "` matches Integer!");
					String arg = matcher.group(1);
//					System.out.print(" => `" + arg + "`");
					if (!arg.equals("")) {
						argType.arg = arg;
						argumentCount++;
					}
					arguments = arguments.replaceFirst(arg + " ?", "");
//					System.out.println(" Remainder: `" + arguments + "`");
				} else if (argumentCount < this.requiredFirstNum) {
//					System.out.println("`" + arguments + "` doesn't match Integer.");
					return argumentCount;
				}
			} else if (argType.type.equals("Double")) {
				Matcher matcher = patternDouble.matcher(arguments);
				if (matcher.find()) {
//					System.out.print("`" + arguments + "` matches Double!");
					String arg = matcher.group(1);
//					System.out.print(" => `" + arg + "`");
					if (!arg.equals("")) {
						argType.arg = arg;
						argumentCount++;
					}
					arguments = arguments.replaceFirst(arg + " ?", "");
//					System.out.println(" Remainder: `" + arguments + "`");
				} else if (argumentCount < this.requiredFirstNum) {
//					System.out.println("`" + arguments + "` doesn't match Double.");
					return argumentCount;
				}
			} else if (argType.type.equals("Boolean")) {
				Matcher matcher = patternBoolean.matcher(arguments);
				if (matcher.find()) {
//					System.out.print("`" + arguments + "` matches Boolean!");
					String arg = matcher.group(1);
//					System.out.print(" => `" + arg + "`");
					if (!arg.equals("")) {
						argType.arg = arg;
						argumentCount++;
					}
					arguments = arguments.replaceFirst(arg + " ?", "");
					System.out.println(" Remainder: `" + arguments + "`");
				} else if (argumentCount < this.requiredFirstNum)
					return argumentCount;
			} else if (argType.type.equals("String")) {
				Matcher matcher = patternEscapedString.matcher(arguments);
				if (matcher.find()) {
//					System.out.print("`" + arguments + "` matches EscapedString!");
					String arg = matcher.group(1);
//					System.out.print(" => `" + arg + "`");
					if (!arg.equals("")) {
						argType.arg = arg;
						argumentCount++;
					}
					arguments = arguments.replaceFirst("\"" + arg + "\" ?", "");
					System.out.println(" Remainder: `" + arguments + "`");
				} else {
					matcher = patternString.matcher(arguments);
					if (matcher.find()) {
//						System.out.print("`" + arguments + "` matches String!");
						String arg = matcher.group(1);
//						System.out.print(" => `" + arg + "`");
						if (!arg.equals("")) {
							argType.arg = arg;
							argumentCount++;
						}
						arguments = arguments.replaceFirst(arg + " ?", "");
//						System.out.println(" Remainder: `" + arguments + "`");
						if (argumentCount == this.arguments.size()) {
							argType.arg += " " + arguments;
							return argumentCount;
						}
					} else {
//						System.out.println("`" + arguments + "` doesn't match String.");
						return argumentCount;
					}
				}
			} else if (argType.type.equals("List")) {
				while (!arguments.replaceAll(" ", "").isEmpty()) {
					Matcher matcher = patternEscapedString.matcher(arguments);
					if (matcher.find()) {
//						System.out.print("`" + arguments + "` matches EscapedString!");
						String arg = matcher.group(1);
//						System.out.print(" => `" + arg + "`");
						argType.argList.add(arg);
						argumentCount++;
						arguments = arguments.replaceFirst("\"" + arg + "\" ?", "");
//						System.out.println(" Remainder: `" + arguments + "`");
					} else {
						matcher = patternString.matcher(arguments);
						if (matcher.find()) {
//							System.out.print("`" + arguments + "` matches String!");
							String arg = matcher.group(1);
//							System.out.print(" => `" + arg + "`");
							argType.argList.add(arg);
							argumentCount++;
							arguments = arguments.replaceFirst(arg + " ?", "");
//							System.out.println(" Remainder: `" + arguments + "`");
						}
					}
				}
				return argumentCount;
			}
			if (arguments.equals(""))
				return argumentCount;
		}
		return argumentCount;
	}

	public boolean validateArguments(String arguments) {
		return parseArguments(arguments) >= this.requiredFirstNum;
	}

	public boolean validateArguments(int argumentCount) {
		return argumentCount >= this.requiredFirstNum;
	}

	private String getTypeShortName(String type) {
		switch (type) {
			case "Integer":
				return "int";
			case "Double":
				return "double";
			case "String":
				return "string";
			case "Boolean":
				return "bool";
			case "List":
				return "string...";
		}
		return type;
	}

	public String getArgumentSyntax() {
		StringBuilder syntax = new StringBuilder();
		int currentArgument = 0;
		for (CommandArgument arg : this.arguments) {
			if (currentArgument == this.requiredFirstNum)
				syntax.append("[");
			syntax.append(getTypeShortName(arg.type));
			if (arg.name != null) {
				syntax.append(" ");
				syntax.append(arg.name);
			}
			if (currentArgument < this.arguments.size() -1)
				syntax.append(", ");
			currentArgument++;
		}
		if (currentArgument > this.requiredFirstNum)
			syntax.append("]");
		return syntax.toString();
	}

	/**
	 *
	 * @param index Starts at 0
	 * @return Returns the argument with index or null if it doesn't exist.
	 */
	public String getArgument(int index) {
		if (this.arguments.size() < index)
			return null;
		return this.arguments.get(index).arg;
	}

	public String getArgument(String name) {
		for (CommandArgument arg : this.arguments) {
			if (arg.name.equals(name))
				return arg.arg;
		}
		return null;
	}

	public int getInt(int index) {
		try {
			return Integer.parseInt(getArgument(index));
		} catch (NumberFormatException ex) {
			return Integer.MIN_VALUE;
		}
	}

	public int getInt(String name) {
		try {
			return Integer.parseInt(getArgument(name));
		} catch (NumberFormatException ex) {
			return Integer.MIN_VALUE;
		}
	}

	public double getDouble(int index) {
		try {
			return Double.parseDouble(getArgument(index));
		} catch (NumberFormatException ex) {
			return Double.MIN_VALUE;
		}
	}

	public double getDouble(String name) {
		try {
			return Double.parseDouble(getArgument(name));
		} catch (NumberFormatException ex) {
			return Double.MIN_VALUE;
		}
	}

	public boolean getBool(int index) {
		return ((getArgument(index).equals("true") || getArgument(index).equals("1")));
	}

	public boolean getBool(String name) {
		return ((getArgument(name).equals("true") || getArgument(name).equals("1")));
	}

	public ArrayList<String> getList(int index) {
		if (this.arguments.size() < index)
			return null;
		return this.arguments.get(index).argList;
	}

	public ArrayList<String> getList(String name) {
		for (CommandArgument arg : this.arguments) {
			if (arg.name.equals(name))
				return arg.argList;
		}
		return null;
	}
}
