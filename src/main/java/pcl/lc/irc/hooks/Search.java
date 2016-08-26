package pcl.lc.irc.hooks;
import org.json.JSONException;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.GoogleSearch;
import pcl.lc.utils.SearchResult;
@SuppressWarnings("rawtypes")
public class Search extends ListenerAdapter {

	public Search() {
		IRCBot.registerCommand("g", "Searches google and returns the first result");
		IRCBot.registerCommand("cf", "Searches CurseForge and returns the first result");
		IRCBot.registerCommand("wiki", "Searches Wikipedia and returns the first result");
		IRCBot.registerCommand("urban", "Searches UrbanDictonary and returns the first result");
		IRCBot.registerCommand("ann", "Searches Anime News Network and returns the first result");
	}
	
	@Override
	public void onMessage(final MessageEvent event) {
		String filter = null;
		String[] splitMessage = event.getMessage().split(" ");
		String prefix = Config.commandprefix;
		boolean doSearch = false;
		if(splitMessage[0].equals(prefix + "g") || splitMessage[0].equals(prefix + "google")) {
			doSearch = true;
		} else if(splitMessage[0].equals(prefix + "curseforge") || splitMessage[0].equals(prefix + "cf")) {
			filter = "site:minecraft.curseforge.com";
			doSearch = true;
		} else if(splitMessage[0].equals(prefix + "wiki")) {
			filter = "wiki";
			doSearch = true;
		} else if(splitMessage[0].equals(prefix + "urban")) {
			filter = "site:urbandictionary.com";
			doSearch = true;
		} else if(splitMessage[0].equals(prefix + "ann")) {
			filter = "site:animenewsnetwork.com";
			doSearch = true;
		} else if(splitMessage[0].equals(prefix + "yt") || splitMessage[0].equals(prefix + "youtube")) {
			filter = "site:youtube.com";
			doSearch = true;
		}
		if (doSearch)
			try {
				event.respond(performSearch(filter, StringUtils.join(splitMessage, " ", 1, splitMessage.length)).get(0).getSuggestedReturn());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

		//return url + " - " + Colors.BOLD + title + Colors.NORMAL + ": \"" + content + "\"";
		return results;
	}
}