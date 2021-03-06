/**
 * 
 */
package pcl.lc.irc.hooks;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.pircbotx.Colors;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import pcl.lc.irc.*;
import pcl.lc.irc.entryClasses.*;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.CommandChainStateObject;
import pcl.lc.utils.Helper;

/**
 * @author Michiyo
 *
 */
public class GithubInfo extends AbstractListener {

	private Command local_command;

	@Override
	protected void initHook() {
		local_command = new Command("github", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "State")), new CommandRateLimit(10), Permissions.MOD) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String state = this.argumentParser.getArgument("State").toLowerCase();
				if (state.equals("disable") || state.equals("enable")) {
					Helper.toggleCommand("github", target, state);
				} else {
					String isEnabled = Helper.isEnabledHere(target, "github") ? "enabled" : "disabled";
					Helper.sendMessage(target, "GitHub Info is " + isEnabled + " in this channel", nick);
				}
				return new CommandChainStateObject();
			}
		}; local_command.setHelpText("Github Ticket info");
		IRCBot.registerCommand(local_command);
	}
	
	@Override
	public void handleMessage(String sender, MessageEvent event, String[] args) {
		if (Helper.isEnabledHere(event.getChannel().getName(), "github")) {
			String message = String.join(" ", args);
			Pattern pattern = Pattern.compile("(?:github.com)/(.+)/(.+)/(issues)/([0-9]+)");
			Matcher matcher = pattern.matcher(message);
			StringBuilder path = new StringBuilder();
			boolean matchFound = matcher.find();
			if (matchFound) {
				for (int i = 1; i <= matcher.groupCount(); i++) {
					path.append(matcher.group(i) + "/");
				}
				String URL = "https://api.github.com/repos/" + path.toString().replaceAll("/$", "");
			    URL url;
				try {
					url = new URL(URL);
				    HttpURLConnection request = (HttpURLConnection) url.openConnection();
				    request.connect();
				    String result = IOUtils.toString(new InputStreamReader((InputStream) request.getContent()));
				    JsonParser jsonParser = new JsonParser();
				    JsonElement title = jsonParser.parse(result).getAsJsonObject().get("title");
				    JsonElement user = jsonParser.parse(result).getAsJsonObject().get("user").getAsJsonObject().get("login");
				    JsonElement time = jsonParser.parse(result).getAsJsonObject().get("created_at");
				    JsonElement status = jsonParser.parse(result).getAsJsonObject().get("state");
				    java.util.Date date = new DateTime(time.getAsString()).toDate();
				    Helper.AntiPings = Helper.getNamesFromTarget(event.getChannel().getName());
				    Helper.sendMessage(event.getChannel().getName(), Colors.BOLD + "Title: " + Colors.NORMAL + title.getAsString() + Colors.BOLD + " | Posted by: " + Colors.NORMAL + user.getAsString() + Colors.BOLD + " | Posted: " + Colors.NORMAL + date + Colors.BOLD + " | Status: " + Colors.NORMAL + status.getAsString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
