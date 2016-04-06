package pcl.lc.irc.hooks;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
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
			event.respond(performSearch(filter, StringUtils.join(splitMessage, " ", 1, splitMessage.length)));
	}

	private String performSearch(String filter, String terms) {
		try {
			StringBuilder searchURLString = new StringBuilder();
			searchURLString.append("https://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=");
			if (filter != null) {
				searchURLString.append(filter).append("+");
			}
			searchURLString.append(terms.replace(" ", "+"));
			URL searchURL = new URL(searchURLString.toString());
			URLConnection conn = searchURL.openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:39.0) Gecko/20100101 RavenBot/2.0");
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder json = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				json.append(line).append("\n");
			}
			in.close();
			JsonElement element = new JsonParser().parse(json.toString());
			JsonObject output = element.getAsJsonObject().getAsJsonObject("responseData").getAsJsonArray("results").get(0).getAsJsonObject();
			String title = StringEscapeUtils.unescapeJava(StringEscapeUtils.unescapeHtml4(output.get("titleNoFormatting").toString().replaceAll("\"", "")));
			String content = StringEscapeUtils.unescapeJava(StringEscapeUtils.unescapeHtml4(output.get("content").toString().replaceAll("\\s+", " ").replaceAll("\\<.*?>", "").replaceAll("\"", "")));
			String url = StringEscapeUtils.unescapeJava(output.get("unescapedUrl").toString().replaceAll("\"", ""));
			return url + " - " + Colors.BOLD + title + Colors.NORMAL + ": \"" + content + "\"";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}