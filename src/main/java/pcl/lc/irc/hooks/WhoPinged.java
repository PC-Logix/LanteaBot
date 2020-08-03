package pcl.lc.irc.hooks;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import com.github.kevinsawicki.timeago.TimeAgo;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import pcl.lc.httpd.httpd;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

public class WhoPinged extends AbstractListener {
	Command command_WhoPinged;
	Command command_ClearPings;
	private ScheduledFuture<?> executor;
	static String html;

	public WhoPinged() throws IOException {
		InputStream htmlIn = getClass().getResourceAsStream("/html/whopinged.html");
		html = CharStreams.toString(new InputStreamReader(htmlIn, Charsets.UTF_8));
	}

	@Override
	protected void initHook() {
		Database.addStatement("CREATE TABLE IF NOT EXISTS Pings(id INTEGER PRIMARY KEY, whowaspinged, whopinged, message, time, channel)");
		Database.addPreparedStatement("addPing", "INSERT INTO Pings(whowaspinged, whopinged, message, time, channel) VALUES (?,?,?,?,?);");
		Database.addPreparedStatement("getPings", "SELECT id, whopinged, message, time, channel FROM Pings WHERE LOWER(whowaspinged) = ?;");
		Database.addPreparedStatement("getAllPings", "SELECT id, time FROM Pings;");
		Database.addPreparedStatement("delPings", "DELETE FROM Pings WHERE id = ?;");

		command_WhoPinged = new Command("whopinged") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {

				try {
					PreparedStatement statement = Database.getPreparedStatement("getPings");
					statement.setString(1, nick.toLowerCase());
					ResultSet results = statement.executeQuery();
					if (Config.httpdEnable.equals("true") && results.next()){
						Helper.sendMessage(target, "Who Pinged you: " + httpd.getBaseDomain() + "/whopinged?nick="+nick, nick);
					} else if (!results.next()) {
						Helper.sendMessage(target, "No pings! :(");
					}
				} catch (Exception e) {
					Helper.sendMessage(target, "Error: " + e.getClass() + " " + e.getMessage());
					e.printStackTrace();
				}
			}
		};
		command_WhoPinged.setHelpText("Shows you the last ? pings you had.");
		command_ClearPings = new Command("clearpings") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					PreparedStatement statement = Database.getPreparedStatement("delPings");
					statement.setString(1, nick);
					statement.execute();
					Helper.sendMessage(target, "Ok");
				} catch (Exception e) {
					Helper.sendMessage(target, "Error: " + e.getClass() + " " + e.getMessage());
					e.printStackTrace();
				}
			}
		};
		command_ClearPings.setHelpText("Clears your pings from the DB");
		IRCBot.registerCommand(command_WhoPinged);
		IRCBot.registerCommand(command_ClearPings);
		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		executor = ses.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					long epoch = System.currentTimeMillis();
					PreparedStatement getPings = Database.getPreparedStatement("getAllPings");
					ResultSet results = getPings.executeQuery();
					while (results.next()) {
						if ((results.getLong(2) + 259200000) <= epoch) {
							PreparedStatement delPings = Database.getPreparedStatement("delPings");
							delPings.setLong(1, results.getLong(1));
							delPings.execute();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 0, 1, TimeUnit.SECONDS);
		httpd.registerContext("/whopinged", new WhoPingedHandler(), "WhoPinged");
	}

	static class WhoPingedHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			TimeAgo time = new TimeAgo();
			String target = t.getRequestURI().toString();
			String response = "";
			List<NameValuePair> paramsList = URLEncodedUtils.parse(t.getRequestURI(),"utf-8");
			String items = "";
			if (paramsList.size() == 1) {
				try {
					PreparedStatement statement = Database.getPreparedStatement("getPings");
					statement.setString(1, paramsList.get(0).getValue().toLowerCase());
					ResultSet resultSet = statement.executeQuery();
					items = "<form action=\"\" method=\"get\">Nickname: <input type=\"text\" name=\"nick\" value=\""+paramsList.get(0).getValue()+"\"><input type=\"submit\" value=\"Submit\"></form>"
							+ "<table><tr><th>Channel</th><th>User</th><th>Message</th><th>Time/Date</th></tr>";
					while (resultSet.next()) {
						items += "<tr><td>" + resultSet.getString(5) + "</td><td>" + resultSet.getString(2) + "</td><td>" + resultSet.getString(3) + "</td><td>" + time.timeAgo(resultSet.getLong(4)) + "</td></tr>";
					}
					items += "</table>";
					items = StringUtils.strip(items, "\n");
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				items = "<form action=\"\" method=\"get\">Nickname: <input type=\"text\" name=\"nick\"><input type=\"submit\" value=\"Submit\"></form>";
			}

			String navData = "";
			Iterator it = httpd.pages.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry)it.next();
				navData += "<div class=\"innertube\"><h1><a href=\""+ pair.getValue() +"\">"+ pair.getKey() +"</a></h1></div>";
			}

			// convert String into InputStream
			InputStream is = new ByteArrayInputStream(html.getBytes());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String line = null;

				while ((line = br.readLine()) != null) {
					response = response + line.replace("#BODY#", target).replace("#BOTNICK#", IRCBot.getOurNick()).replace("#whopinged#", items).replace("#NAVIGATION#", navData)+"\n";
				}
			}
			t.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	public String chan;
	public String target = null;

	@Override
	public void handleMessage(String sender, MessageEvent event, String[] args) {
		String message = String.join(" ", args).trim();
		try {
			String[] parts = message.split(" ");
			for (String part : parts) {
				if (Helper.stringContainsItemFromList(part, event.getChannel().getUsersNicks())) {
					if (event.getUser().getNick().equalsIgnoreCase("corded")) {
						sender = "@"+sender;
					}
					PreparedStatement addPing = Database.getPreparedStatement("addPing");
					addPing.setString(1, part);
					addPing.setString(2, sender);
					addPing.setString(3, message);
					addPing.setLong(4, System.currentTimeMillis());
					addPing.setString(5, event.getChannel().getName());
					addPing.executeUpdate();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
