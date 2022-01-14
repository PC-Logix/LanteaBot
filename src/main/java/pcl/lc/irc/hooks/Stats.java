package pcl.lc.irc.hooks;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pcl.lc.httpd.httpd;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.EffectEntry;
import pcl.lc.utils.db_items.DbStatCounter;
import pcl.lc.utils.db_items.DbStatCounterCollection;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class Stats extends AbstractListener {

	static String html;

	@Override
	protected void initHook() {
		try {
			httpd.registerContext("/stats", new StatsHandler(), "Stats");
			InputStream htmlIn = getClass().getResourceAsStream("/html/stats.html");
			html = CharStreams.toString(new InputStreamReader(htmlIn, Charsets.UTF_8));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	static class StatsHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			String target = t.getRequestURI().toString();
			StringBuilder response = new StringBuilder();

			StringBuilder stats_list = new StringBuilder();

			ArrayList<String> stat_groups = DbStatCounter.GetUniqueByField(DbStatCounter.table, "group");
			stats_list.append("<table>");
			for (String group_name : stat_groups) {
				String group_title = group_name.substring(0, 1).toUpperCase() + group_name.substring(1);
				group_title = group_title.replaceAll("_", " ");
				stats_list.append("<tr><th>").append(group_title).append("</th><th>Count</th></tr>");

				DbStatCounterCollection group = DbStatCounter.GetByGroup(group_name);
				if (group.items.size() == 0) {
					stats_list.append("<table><tr><td>No entries</td></tr></table>");
				} else {
					for (DbStatCounter item : group.items) {
						String entry_name = item.key;
						if (item.group.equalsIgnoreCase("potion_effects")) {
							EffectEntry effect = DrinkPotion.GetEffectByKey(item.key);
							if (effect != null)
								entry_name = effect.effectDrink;
							else
								entry_name = null;
						}
						if (item.group.startsWith("command"))
							entry_name = Config.commandprefix + entry_name;
						if (entry_name != null)
							stats_list.append("<tr><td>").append(entry_name).append("</td><td>").append((int) item.count).append("</tr>");
					}
				}
			}
			stats_list.append("</table>");

			StringBuilder navData = new StringBuilder();
			for (Map.Entry<String, String> stringStringEntry : httpd.pages.entrySet()) {
				navData.append("<div class=\"innertube\"><h1><a href=\"").append(((Map.Entry) stringStringEntry).getValue()).append("\">").append(((Map.Entry) stringStringEntry).getKey()).append("</a></h1></div>");
			}

			// convert String into InputStream
			InputStream is = new ByteArrayInputStream(html.getBytes());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String line = null;

				while ((line = br.readLine()) != null) {
					response.append(line.replace("#BODY#", target).replace("#BOTNICK#", IRCBot.getOurNick()).replace("#STATDATA#", stats_list).replace("#NAVIGATION#", navData.toString())).append("\n");
				}
			}

			t.getResponseHeaders().set("Content-type", "text/html; charset=utf-8");
			ByteBuffer buffer = StandardCharsets.UTF_8.encode(response.toString());
			byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			t.sendResponseHeaders(200, bytes.length);
			t.getResponseBody().write(bytes);
			t.close();
		}
	}
}
