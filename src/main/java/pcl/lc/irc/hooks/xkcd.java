package pcl.lc.irc.hooks;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.*;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.GoogleSearch;
import pcl.lc.utils.Helper;
import pcl.lc.utils.SearchResult;

/**
 * @author Caitlyn
 */
@SuppressWarnings("rawtypes")
public class xkcd extends AbstractListener {
	Command local_command;
	public List<String> enabledChannels = new ArrayList<String>();
	private String chan;

	private static String readUrl(String urlString) throws Exception {
		BufferedReader reader = null;
		try {
			URL url = new URL(urlString);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer buffer = new StringBuffer();
			int read;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1)
				buffer.append(chars, 0, read);

			return buffer.toString();
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	public static boolean isNumeric(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isDigit(c)) return false;
		}
		return true;
	}

	@Override
	protected void initHook() {
		local_command = new Command("xkcd", new CommandArgumentParser(0, new CommandArgument("Comic", ArgumentTypes.STRING))) {
			@Override
			public CommandChainState onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws Exception {
				String id_or_terms = this.argumentParser.getArgument("Comic");
				if (id_or_terms != null) {
					if (isNumeric(id_or_terms)) {
						String json = null;
						json = readUrl("https://xkcd.com/" + id_or_terms + "/info.0.json");
						JSONObject obj = null;
						obj = new JSONObject(json);
						String name = null;
						name = obj.get("safe_title").toString();
						Helper.sendMessage(target, "XKCD Comic Name: " + name + " URL: https://xkcd.com/" + id_or_terms);
					} else {
						if (Config.googleAPI.equals("")) {
							Helper.sendMessage(target, "No Google API key has been set. This is required for this search feature.", nick);
							return CommandChainState.ERROR;
						}
						String filter = "site:xkcd.com";
						String suggestedReturn = performSearch(filter, id_or_terms).get(0).getSuggestedReturn();
						Helper.sendMessage(target, suggestedReturn, nick);
					}
				} else {
					URLConnection con = null;
					con = new URL("https://dynamic.xkcd.com/random/comic/").openConnection();
					con.connect();
					InputStream is = null;
					is = con.getInputStream();
					String newurl = con.getURL().toString();
					is.close();
					Helper.sendMessage(target, "Random XKCD Comic: " + newurl);
				}
				return CommandChainState.FINISHED;
			}
		};
		local_command.setHelpText("XKCD stuff");
		IRCBot.registerCommand(local_command);
	}

	@Override
	public void onMessage(final MessageEvent event) {
		chan = event.getChannel().getName();
		if (event.getMessage().contains("xkcd.com") && enabledChannels.contains(event.getChannel().getName())) {
			Pattern pattern = Pattern.compile(
					"(?:https?:\\/\\/)?(?:www\\.)?(?<!what-if\\.)xkcd\\.com\\/([0-9]+)",
					Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(event.getMessage());
			if (matcher.find()) {
				String json = null;
				try {
					json = readUrl("https://xkcd.com/" + matcher.group(1) + "/info.0.json");
				} catch (Exception e) {
					e.printStackTrace();
				}
				JSONObject obj = null;
				try {
					obj = new JSONObject(json);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				String name = null;
				try {
					name = obj.get("safe_title").toString();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				try {
					event.getChannel().send().message("XKCD Comic Name: " + name + " Posted on: " + obj.get("month").toString() + "/" + obj.get("day").toString() + "/" + obj.get("year").toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private List<SearchResult> performSearch(String filter, String terms) throws JSONException {
		StringBuilder searchURLString = new StringBuilder();
		//searchURLString.append("https://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=");
		if (filter != null) {
			searchURLString.append(filter).append("+");
		}
		searchURLString.append(terms.replace(" ", "+"));
		List<SearchResult> results = GoogleSearch.performSearch(
				"018291224751151548851%3Ajzifriqvl1o",
				searchURLString.toString());
		System.out.println("Results: " + results.size());
		//return url + " - " + Colors.BOLD + title + Colors.NORMAL + ": \"" + content + "\"";
		return results;
	}
}
