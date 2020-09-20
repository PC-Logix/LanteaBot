package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import pcl.lc.httpd.httpd;
import pcl.lc.irc.*;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

@SuppressWarnings("rawtypes")
public class Quotes extends AbstractListener {
	private Command quote;
	private Command add;
	private Command delete;
	private Command list;
	private Command quotes;

	private String idIdentificationCharacter = "#";
	
	static String html;
	
	public Quotes() throws IOException {
		InputStream htmlIn = getClass().getResourceAsStream("/html/quotes.html");
		html = CharStreams.toString(new InputStreamReader(htmlIn, Charsets.UTF_8));
	}
	
	@Override
	protected void initHook() {
		httpd.registerContext("/quotes", new QuoteHandler(), "Quotes");
		initCommands();
		Database.addStatement("CREATE TABLE IF NOT EXISTS Quotes(id INTEGER PRIMARY KEY, user, data)");
		Database.addUpdateQuery(3, "ALTER TABLE Quotes ADD added_by DEFAULT NULL");
		Database.addPreparedStatement("addQuote","INSERT INTO Quotes(id, user, data, added_by) VALUES (NULL, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
		Database.addPreparedStatement("getUserQuote","SELECT id, data FROM Quotes WHERE LOWER(user) = ? ORDER BY RANDOM () LIMIT 1;");
		Database.addPreparedStatement("getIdQuote","SELECT user, data FROM Quotes WHERE id = ? LIMIT 1;");
		Database.addPreparedStatement("getUserQuoteAll","SELECT id, data FROM Quotes WHERE LOWER(user) = ?;");
		Database.addPreparedStatement("getAnyQuote","SELECT id, user, data FROM Quotes ORDER BY RANDOM () LIMIT 1;");
		Database.addPreparedStatement("getAllQuotes","SELECT id, user, data, added_by FROM Quotes;");
		Database.addPreparedStatement("getSpecificQuote","SELECT id, data FROM Quotes WHERE user = ? AND data = ?;");
		Database.addPreparedStatement("removeQuote","DELETE FROM Quotes WHERE id = ?;");

	}

	private void initCommands() {
		quote = new Command("quote") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				if (params.length() == 0) {
						PreparedStatement getAnyQuote = Database.getPreparedStatement("getAnyQuote");
						ResultSet results = getAnyQuote.executeQuery();
						if (results.next()) {
							Helper.sendMessage(target, "Quote #" + results.getString(1) + ": <" + pcl.lc.utils.Helper.antiPing(results.getString(2)) + "> " + results.getString(3));
						}
				} else {
					if (params.substring(0, 1).equals(idIdentificationCharacter)) {
						String id = params.replace(idIdentificationCharacter, "");

							PreparedStatement getQuote = Database.getPreparedStatement("getIdQuote");
							getQuote.setString(1, id);
							ResultSet results = getQuote.executeQuery();
							if (results.next()) {
								Helper.sendMessage(target, "Quote #" + id + ": <" + pcl.lc.utils.Helper.antiPing(results.getString(1)) + "> " + results.getString(2));
							}
							else {
								Helper.sendMessage(target, "No quote found for id #" + id, nick);
							}
					}
					else {
							PreparedStatement getQuote = Database.getPreparedStatement("getUserQuote");
							getQuote.setString(1, params.toLowerCase());
							ResultSet results = getQuote.executeQuery();
							if (results.next()) {
								Helper.sendMessage(target, "Quote #" + results.getString(1) + ": <" + pcl.lc.utils.Helper.antiPing(params) + "> " + results.getString(2));
							}
							else {
								Helper.sendMessage(target, "No quotes found for name '" + pcl.lc.utils.Helper.antiPing(params) + "'", nick);
							}
					}
				}
			}
		};
		quote.setHelpText("Returns quotes from the quote database. Also Has sub-commands: add, del");

		add = new Command("add") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws Exception {
				if (params.size() > 1) {
					String key = params.get(0);
					String data = "";
					for (int i = 1; i < params.size(); i++) {
						data += " " + params.get(i);
					}
					data = data.trim();
						PreparedStatement addQuote = Database.getPreparedStatement("addQuote");
						addQuote.setString(1, key);
						addQuote.setString(2, data);
						addQuote.setString(3, nick);
						if (addQuote.executeUpdate() > 0) {
							Helper.sendMessage(target, "Quote added at id: " + addQuote.getGeneratedKeys().getInt(1), nick);
							return;
						}
					Helper.sendMessage(target, "An error occurred while trying to save the quote.", nick);
				}
			}
		};
		add.setHelpText("Adds a quote to the database");

		delete = new Command("delete", Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws Exception {
				if (params.size() > 0) {
					String key = params.get(0).replace(idIdentificationCharacter, "");
					//String data = StringUtils.join(args, " ", 1, args.length);
						PreparedStatement removeQuote = Database.getPreparedStatement("removeQuote");
						removeQuote.setString(1, key);
						//removeQuote.setString(2, data);
						if (removeQuote.executeUpdate() > 0) {
							Helper.sendMessage(target, "Quote removed.", nick);
							return;
						}
					Helper.sendMessage(target, "An error occurred while trying to set the value.", nick);
				}
			}
		};
		delete.setHelpText("Removes a quote from the database");
		delete.registerAlias("del");

		list = new Command("list") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws Exception {
				if (params.size() > 0) {
					String key = params.get(0);
						PreparedStatement getUserQuoteAll = Database.getPreparedStatement("getUserQuoteAll");
						getUserQuoteAll.setString(1, key.toLowerCase());
						ResultSet results = getUserQuoteAll.executeQuery();

						ArrayList<String> returnValues = new ArrayList<String>();

						while (results.next())
							returnValues.add(results.getString(1));

						if (!returnValues.isEmpty()) {
							String ids = "";
							for (String value :returnValues) {
								ids += value + ", ";
							}
							ids = ids.replaceAll(", $", "");
							Helper.sendMessage(target, "User <" + pcl.lc.utils.Helper.antiPing(key) + "> has " + returnValues.size() + " quotes: " + ids);
						}
						else {
							Helper.sendMessage(target, "No quotes found for user '" + key + "'", nick);
						}
				}
			}
		};
		list.setHelpText("Returns list of ids for quotes belonging to user as well as their total quote count");

		quotes = new Command("quotes") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, httpd.getBaseDomain() + "/quotes", nick);
			}
		};

		IRCBot.registerCommand(quote);
		quote.registerAlias("q");
		quote.registerSubCommand(add);
		quote.registerSubCommand(delete);
		quote.registerSubCommand(list);
	}

	static class QuoteHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {

			String target = t.getRequestURI().toString();
			String response = "";

			String quoteList = "";
			List<NameValuePair> paramsList = URLEncodedUtils.parse(t.getRequestURI(),"utf-8");
			int qid = 0;
			if (paramsList.size() >= 1) {
				for (NameValuePair parameter : paramsList)
					if (parameter.getName().equals("id"))
						qid = Integer.valueOf(parameter.getValue());
				try {
					PreparedStatement getQuote = Database.getPreparedStatement("getIdQuote");
					getQuote.setInt(1, qid);
					ResultSet results = getQuote.executeQuery();
					if (results.next()) {
						quoteList = "Quote #" + qid + ": &lt;" + escapeHtml4(results.getString(1)) + "&gt; " + escapeHtml4(results.getString(2));
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					PreparedStatement getAllQuotes = Database.getPreparedStatement("getAllQuotes");
					ResultSet results = getAllQuotes.executeQuery();
					quoteList = "<table><tr><th>ID</th><th>Quote</th><th>Added By</th></tr>";
					PreparedStatement getQuote = Database.getPreparedStatement("getIdQuote");
					while (results.next()) {
						getQuote.setInt(1, results.getInt(1));
						ResultSet quoteData = getQuote.executeQuery();
						quoteList = quoteList + "<tr><td>" + results.getString(1) + "</td><td>&lt;" + escapeHtml4(quoteData.getString(1)) + "&gt; " + escapeHtml4(quoteData.getString(2))+"</td><td>"+results.getString(4)+"</td></tr>\n";
						//quoteList = quoteList + "<tr><td>" + "<a href=\"?id=" + results.getString(1) +"\">Quote #"+results.getString(1) + "</a>" + "</td><td>" +results.getString(4)+"</td><td></td></tr>\n";
					}
					quoteList += "</table>";
				}
				catch (Exception e) {
					e.printStackTrace();
				}
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
					response = response + line.replace("#BODY#", target).replace("#BOTNICK#", IRCBot.getOurNick()).replace("#QUOTEDATA#", quoteList)
							.replace("#NAVIGATION#", navData)+"\n";
				}
			}
			t.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
}
