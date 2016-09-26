package pcl.lc.irc.hooks;
import org.json.JSONException;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.GoogleSearch;
import pcl.lc.utils.Helper;
import pcl.lc.utils.SearchResult;
@SuppressWarnings("rawtypes")
public class Search extends AbstractListener {

	private String chan;

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

	@Override
	protected void initCommands() {
		IRCBot.registerCommand("g", "Searches google and returns the first result");
		IRCBot.registerCommand("cf", "Searches CurseForge and returns the first result");
		IRCBot.registerCommand("wiki", "Searches Wikipedia and returns the first result");
		IRCBot.registerCommand("urban", "Searches UrbanDictonary and returns the first result");
		IRCBot.registerCommand("ann", "Searches Anime News Network and returns the first result");
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		String filter = null;
		String prefix = Config.commandprefix;
		boolean doSearch = false;
		String target;
		if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
			target = nick;
		} else {
			target = chan;
		}
		if(command.equals(prefix + "g") || command.equals(prefix + "google")) {
			doSearch = true;
		} else if(command.equals(prefix + "curseforge") || command.equals(prefix + "cf")) {
			filter = "site:minecraft.curseforge.com";
			doSearch = true;
		} else if(command.equals(prefix + "wiki")) {
			filter = "wiki";
			doSearch = true;
		} else if(command.equals(prefix + "urban")) {
			filter = "site:urbandictionary.com";
			doSearch = true;
		} else if(command.equals(prefix + "ann")) {
			filter = "site:animenewsnetwork.com";
			doSearch = true;
		} else if(command.equals(prefix + "yt") || command.equals(prefix + "youtube")) {
			filter = "site:youtube.com";
			doSearch = true;
		}
		if (doSearch)
			try {
				IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " + performSearch(filter, StringUtils.join(copyOfRange, " ", 0, copyOfRange.length)).get(0).getSuggestedReturn());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}