package pcl.lc.irc.hooks;
import com.google.api.client.json.Json;
import org.json.JSONException;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.GoogleSearch;
import pcl.lc.utils.Helper;
import pcl.lc.utils.PasteUtils;
import pcl.lc.utils.SearchResult;
@SuppressWarnings("rawtypes")
public class Search extends AbstractListener {
	private Command search;
	private Command google;
	private Command curseForge;
	private Command wiki;
	private Command urban;
	private Command ann;
	private Command youtube;
	private Command g;
	private Command yt;
	private Command wik;
	private Command cf;

	@Override
	protected void initHook() {
		search = new Command("search", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				Helper.sendMessage(target, this.trySubCommandsMessage(((params.size() > 0) ? params.get(0) : "")), nick);
			}
		}; search.setHelpText("Search various sites for term (eg search <site> <term>)");
		google = new Command("google", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				List<SearchResult> result = performSearch(null, params);
				Helper.sendMessage(target, ((result != null ) ? result.get(0).getSuggestedReturn() : "Search failed"), nick, true);
			}
		}; google.setHelpText("Searches google and returns the first result");
		curseForge = new Command("curseForge", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				List<SearchResult> result = performSearch("site:minecraft.curseforge.com", params);
				Helper.sendMessage(target, ((result != null ) ? result.get(0).getSuggestedReturn() : "Search failed"), nick, true);
			}
		}; curseForge.setHelpText("Searches CurseForge and returns the first result");
		wiki = new Command("wiki", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				List<SearchResult> result = performSearch("wiki", params);
				Helper.sendMessage(target, ((result != null ) ? result.get(0).getSuggestedReturn() : "Search failed"), nick, true);
			}
		}; wiki.setHelpText("Searches Wikipedia and returns the first result");
		urban = new Command("urban", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				List<SearchResult> result = performSearch("site:urbandictionary.com", params);
				Helper.sendMessage(target, ((result != null ) ? result.get(0).getSuggestedReturn() : "Search failed"), nick, true);
			}
		}; urban.setHelpText("Searches UrbanDictonary and returns the first result");
		ann = new Command("ann", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				List<SearchResult> result = performSearch("site:animenewsnetwork.com", params);
				Helper.sendMessage(target, ((result != null ) ? result.get(0).getSuggestedReturn() : "Search failed"), nick, true);
			}
		}; ann.setHelpText("Searches Anime News Network and returns the first result");
		youtube = new Command("youtube", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				List<SearchResult> result = performSearch("site:youtube.com", params);
				Helper.sendMessage(target, ((result != null ) ? result.get(0).getSuggestedReturn() : "Search failed"), nick, true);
			}
		}; youtube.setHelpText("Searches YouTube and returns the first result");
		IRCBot.registerCommand(search);
		search.registerSubCommand(google);
		search.registerSubCommand(curseForge);
		search.registerSubCommand(wiki);
		search.registerSubCommand(urban);
		search.registerSubCommand(ann);
		search.registerSubCommand(youtube);
		g = new Command("g", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				google.onExecuteSuccess(command, nick, target, event, params);
			}
		};
		yt = new Command("yt", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				youtube.onExecuteSuccess(command, nick, target, event, params);
			}
		};
		wik = new Command("wiki", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				wiki.onExecuteSuccess(command, nick, target, event, params);
			}
		};
		cf = new Command("cf", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				curseForge.onExecuteSuccess(command, nick, target, event, params);
			}
		};
	}

	private List<SearchResult> performSearch(String filter, String terms) {
		try {
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
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
			target = nick;
		} else {
			target = chan;
		}
		search.tryExecute(command, nick, target, event, copyOfRange);
		g.tryExecute(command, nick, target, event, copyOfRange);
		yt.tryExecute(command, nick, target, event, copyOfRange);
		wik.tryExecute(command, nick, target, event, copyOfRange);
	}

	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {}
}