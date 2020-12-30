package pcl.lc.irc.hooks;
import org.json.JSONException;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.GoogleSearch;
import pcl.lc.utils.Helper;
import pcl.lc.utils.SearchResult;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class Search extends AbstractListener {
	private Command search;
	private Command google;
	private Command curseForge;
	private Command wiki;
	private Command urban;
	private Command ann;
	private Command youtube;
	private Command lmgtfy;
	private Command g;
	private Command yt;
	private Command wik;
	private Command cf;
	private Command urb;

	@Override
	protected void initHook() {
		search = new Command("search") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				Helper.sendMessage(target, this.trySubCommandsMessage(((params.size() > 0) ? params.get(0) : "")), nick);
			}
		}; search.setHelpText("Search various sites for term (eg search <site> <term>)");
		google = new Command("google", new CommandArgumentParser(1, new CommandArgument("Query", ArgumentTypes.STRING))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				List<SearchResult> result = performSearch(null, this.argumentParser.getArgument("Query"));
				Helper.sendMessage(target, ((result != null ) ? result.get(0).getSuggestedReturn() : "Search failed"), nick, true);
			}
		}; google.setHelpText("Searches google and returns the first result");
		curseForge = new Command("curseForge", new CommandArgumentParser(1, new CommandArgument("Query", ArgumentTypes.STRING))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				List<SearchResult> result = performSearch("site:minecraft.curseforge.com", this.argumentParser.getArgument("Query"));
				Helper.sendMessage(target, ((result != null ) ? result.get(0).getSuggestedReturn() : "Search failed"), nick, true);
			}
		}; curseForge.setHelpText("Searches CurseForge and returns the first result");
		wiki = new Command("wiki", new CommandArgumentParser(1, new CommandArgument("Query", ArgumentTypes.STRING))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				List<SearchResult> result = performSearch("wiki", this.argumentParser.getArgument("Query"));
				Helper.sendMessage(target, ((result != null ) ? result.get(0).getSuggestedReturn() : "Search failed"), nick, true);
			}
		}; wiki.setHelpText("Searches Wikipedia and returns the first result");
		urban = new Command("urban", new CommandArgumentParser(1, new CommandArgument("Query", ArgumentTypes.STRING))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				List<SearchResult> result = performSearch("site:urbandictionary.com", this.argumentParser.getArgument("Query"));
				Helper.sendMessage(target, ((result != null ) ? result.get(0).getSuggestedReturn() : "Search failed"), nick, true);
			}
		}; urban.setHelpText("Searches UrbanDictonary and returns the first result");
		ann = new Command("ann", new CommandArgumentParser(1, new CommandArgument("Query", ArgumentTypes.STRING))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				List<SearchResult> result = performSearch("site:animenewsnetwork.com", this.argumentParser.getArgument("Query"));
				Helper.sendMessage(target, ((result != null ) ? result.get(0).getSuggestedReturn() : "Search failed"), nick, true);
			}
		}; ann.setHelpText("Searches Anime News Network and returns the first result");
		youtube = new Command("youtube", new CommandArgumentParser(1, new CommandArgument("Query", ArgumentTypes.STRING))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				List<SearchResult> result = performSearch("site:youtube.com", this.argumentParser.getArgument("Query"));
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
		g = new Command("g") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				google.onExecuteSuccess(command, nick, target, event, params);
			}
		};
		g.registerAlias("google");
		yt = new Command("yt") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				youtube.onExecuteSuccess(command, nick, target, event, params);
			}
		};
		yt.registerAlias("youtube");
		wik = new Command("wiki") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				wiki.onExecuteSuccess(command, nick, target, event, params);
			}
		};
		wik.registerAlias("wp");
		wik.registerAlias("wikipedia");
		cf = new Command("cf") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				curseForge.onExecuteSuccess(command, nick, target, event, params);
			}
		};
		cf.registerAlias("curse");
		cf.registerAlias("curseforge");
		urb = new Command("urban") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				urban.onExecuteSuccess(command, nick, target, event, params);
			}
		};
		urb.registerAlias("u");
		urb.registerAlias("urbandictionary");

		lmgtfy = new Command("lmgtfy") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					Helper.sendMessage(target, "https://lmgtfy.com/?q=" + URLEncoder.encode(params, "UTF-8"), nick, true);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
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
}