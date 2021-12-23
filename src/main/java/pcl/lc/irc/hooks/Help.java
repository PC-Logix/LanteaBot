package pcl.lc.irc.hooks;

import com.github.kevinsawicki.timeago.TimeAgo;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import pcl.lc.httpd.httpd;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.entryClasses.DynamicCommand;
import pcl.lc.utils.Helper;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class Help extends AbstractListener {

	static String html;

	@Override
	protected void initHook() {
		try {
			httpd.registerContext("/help", new HelpHandler(), "Help");
			InputStream htmlIn = getClass().getResourceAsStream("/html/help.html");
			html = CharStreams.toString(new InputStreamReader(htmlIn, Charsets.UTF_8));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	static ArrayList<String> includedCommands = new ArrayList<>();

	public static String getHelpRow(Command command, String permFilter) {
		if (includedCommands.contains(command.getCommand()))
			return "";
		int permLevelFilter = Permissions.getPermLevel(permFilter);
		int permLevelCommand = Permissions.getPermLevel(command.getPermissionLevel());
		if (permLevelFilter < permLevelCommand)
			return "";
		String item = "";
		String help = StringEscapeUtils.escapeHtml4(command.getHelpText());
		if (command instanceof DynamicCommand && help.equals(DynamicCommands.defaultHelpText)) {
			help = "<span class='fad'>No description, raw command:</span> " + ((DynamicCommand) command).content;
		} else if (help.isEmpty()) {
				help = "<span class='fad'>No help text set for this command.</span>";
		}
		ArrayList<String> extraInfoEntries = new ArrayList<>();
		if (Permissions.getPermLevel(command.getPermissionLevel()) > 0)
			extraInfoEntries.add("Permission: " + command.getPermissionLevel());
		else
			extraInfoEntries.add("Permission: Anyone");
		if (command.getRateLimit() != null)
			extraInfoEntries.add("Cooldown: " + Helper.timeString(Helper.parseSeconds(command.getRateLimit().getLimit())));
		String extraInfo = "<div class='fad'>" + String.join(" | ", extraInfoEntries) + "</div>";
		String argumentSyntax = "";
		if (command.argumentParser != null) {
			argumentSyntax = "<div class='fad'>Argument" + (command.argumentParser.argumentCount == 1 ? "" : "s") + ": " + command.argumentParser.getArgumentSyntax(true) + "</div>";
		}
		item += "<tr><td style='white-space: nowrap;'>" + Config.commandprefix + command.getCommand() + "</td><td>" + help + argumentSyntax + extraInfo + "</td><td style='white-space: nowrap;'>" + String.join("<br/>", command.getAliasesDisplay()) + "</td></tr>";
		int i = 0;
		ArrayList<Command> printableSubCommands = new ArrayList<>();
		for (Command subCommand : command.getSubCommands()) {
			int permLevelSubCommand = Permissions.getPermLevel(subCommand.getPermissionLevel());
			if (permLevelFilter >= permLevelSubCommand)
				printableSubCommands.add(subCommand);
		}

		for (Command subCommand : printableSubCommands) {
			String character = "├";
			if (i == (printableSubCommands.size() - 1)) {
				character = "└";
			}

			String subHelp = StringEscapeUtils.escapeHtml4(subCommand.getHelpText());
			if (subHelp.isEmpty())
				subHelp = "<span class='fad'>No help text set for this sub-command.</span>";
			extraInfoEntries = new ArrayList<>();
			if (Permissions.getPermLevel(subCommand.getPermissionLevel()) > 0)
				extraInfoEntries.add("Permission: " + subCommand.getPermissionLevel());
			else if (Permissions.getPermLevel(command.getPermissionLevel()) > 0)
				extraInfoEntries.add("Permission: " + command.getPermissionLevel());
			else
				extraInfoEntries.add("Permission: Anyone");
			if (command.getRateLimit() != null)
				extraInfoEntries.add("Cooldown: " + Helper.timeString(Helper.parseSeconds(command.getRateLimit().getLimit())));
			extraInfo = "<div class='fad'>" + String.join(" | ", extraInfoEntries) + "</div>";
			String subArgumentSyntax = "";
			if (subCommand.argumentParser != null)
				subArgumentSyntax = "<div class='fad'>Argument" + (subCommand.argumentParser.argumentCount == 1 ? "" : "s") + ": " + StringEscapeUtils.escapeHtml4(subCommand.argumentParser.getArgumentSyntax(true)) + "</div>";
			item += "<tr><td style='white-space: nowrap;'> " + character + " " + subCommand.getCommand() + "</td><td>" + subHelp + subArgumentSyntax + extraInfo + "</td><td style='white-space: nowrap;'>" + String.join("<br/>", subCommand.getAliasesDisplay()) + "</td></tr>";
			i++;
		}
		return item;
	}

	public static String getHelpRows(String permFilter) {
		String items = "";
		try {
			items = "";
			includedCommands.clear();
			for (Map.Entry<String, Command> entry : IRCBot.commands.entrySet()) {
				Command command = entry.getValue();
				String item = getHelpRow(command, permFilter);
				if (!item.equals(""))
					includedCommands.add(command.getCommand());
				items += item;
			}
			if (IRCBot.dynamicCommands.size() > 0)
				items += "<tr><th colspan='3'>Dynamic commands</th></tr>";
			for (Map.Entry<String, DynamicCommand> entry : IRCBot.dynamicCommands.entrySet()) {
				DynamicCommand command = entry.getValue();
				String item = getHelpRow(command, permFilter);
				if (!item.equals(""))
					includedCommands.add(command.getCommand());
				items += item;
			}
			items = StringUtils.strip(items, "\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return items;
	}

	public static String getHelpTable(String permFilter) {
		return "<table><tr><th>Command</th><th>Help</th><th>Aliases</th></tr>" + getHelpRows(permFilter) + "</table>";
	}

	static class HelpHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			TimeAgo time = new TimeAgo();
			String target = t.getRequestURI().toString();
			String permFilter = "";
			if (t.getRequestURI().getQuery() != null) {
				String[] queries = t.getRequestURI().getQuery().split("&");
				for (String q : queries) {
					String[] query = q.split("=");
					if (query[0].equals("permFilter"))
						permFilter = query[1];
				}
			}
			if (permFilter.isEmpty())
				permFilter = Permissions.EVERYONE;
			String response = "";

			String items = "<p>Command syntax works as follows:</p>" +
				"<ul><li>Arguments encased within [] are optional and may be omitted. Specifying an optional argument requires specifying the preceding ones. (All arguments are ordered)</li>" +
				"<li>Certain commands accept certain keywords for some arguments, such as the word \"random\", which can change how the command behaves.</li>" +
				"<li>Certain commands will substitute missing arguments. For example missing targets or items may use random ones.</li>" +
				"<li>The following argument types can appear:<ul>";
			HashMap<String, String> args = ArgumentTypes.getList();
			for (Map.Entry<String, String> entry : args.entrySet()) {
				items += "<li>" + entry.getKey() + " - " + entry.getValue() + "</li>";
			}
			items += "</ul></li>";
			items += "<li>If an argument name looks like <span class='arg-tooltip' title='This is the argument description.'>This</span> it has a description you can see by hovering the cursor over it.</li>";
			items += "</ul>";
			items += "<p></p>";
			items += "<p>Permission filter: <a href='?permFilter=" + Permissions.EVERYONE + "'>Anyone</a> | <a href='?permFilter=" + Permissions.TRUSTED + "'>Trusted</a> | <a href='?permFilter=" + Permissions.MOD + "'>Moderator</a> | <a href='?permFilter=" + Permissions.ADMIN + "'>Admin</a></p>";
			items += getHelpTable(permFilter);

			String navData = "";
			Iterator it = httpd.pages.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				navData += "<div class=\"innertube\"><h1><a href=\"" + pair.getValue() + "\">" + pair.getKey() + "</a></h1></div>";
			}

			// convert String into InputStream
			InputStream is = new ByteArrayInputStream(html.getBytes());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String line = null;

				while ((line = br.readLine()) != null) {
					response = response + line.replace("#BODY#", target).replace("#BOTNICK#", IRCBot.getOurNick()).replace("#HELPDATA#", items).replace("#NAVIGATION#", navData) + "\n";
				}
			}
			//System.out.println(response);
			//t.sendResponseHeaders(200, response.getBytes().length);
			//OutputStream os = t.getResponseBody();
			//os.write(response.getBytes());
			//os.close();

			t.getResponseHeaders().set("Content-type", "text/html; charset=utf-8");
			ByteBuffer buffer = Charset.forName("UTF-8").encode(response);
			byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			t.sendResponseHeaders(200, bytes.length);
			t.getResponseBody().write(bytes);
			t.close();
		}
	}
}
