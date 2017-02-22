package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.utils.Account;

@SuppressWarnings("rawtypes")
public class Quotes extends AbstractListener {

	@Override
	protected void initCommands() {
		IRCBot.httpServer.registerContext("/quotes", new QuoteHandler());
		IRCBot.registerCommand("addquote", "Adds a quote to the database (Requires BotAdmin, or Channel Op");
		IRCBot.registerCommand("quote", "Returns quotes from the quote database");
		IRCBot.registerCommand("delquote", "Removes a quote from the database (Requires BotAdmin, or Channel Op");
		IRCBot.registerCommand("listquotes", "Returns list of ids for quotes belonging to user as well as their total quote count");
	}

	static class QuoteHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {

			String target = t.getRequestURI().toString();
			String response = "";

			String quoteList = "";
			List<NameValuePair> paramsList = URLEncodedUtils.parse(t.getRequestURI(),"utf-8");
			String qid = "0";
			if (paramsList.size() >= 1) {
				for (NameValuePair parameter : paramsList)
					if (parameter.getName().equals("id"))
						qid = parameter.getValue();
				try {
					PreparedStatement getQuote = IRCBot.getInstance().getPreparedStatement("getIdQuote");
					getQuote.setString(1, qid);
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
					PreparedStatement getAllQuotes = IRCBot.getInstance().getPreparedStatement("getAllQuotes");
					ResultSet results = getAllQuotes.executeQuery();
					while (results.next()) {
						quoteList = quoteList + "<a href=\"?id=" + results.getString(1) +"\">Quote #"+results.getString(1)+"</a><br>\n";
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			try (BufferedReader br = new BufferedReader(new FileReader("webroot/quotes.html"))) {
				String line = null;
				while ((line = br.readLine()) != null) {
					response = response + line.replace("#BODY#", target).replace("#BOTNICK#", IRCBot.getOurNick()).replace("#QUOTEDATA#", quoteList)+"\n";
				}
			}
			t.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	@Override
	public void handleCommand(String sender, final MessageEvent event, String command, String[] args) {
		String prefix = Config.commandprefix;
		if (command.equals(prefix + "quote") || command.equals(prefix + "q")) {
			if (args.length == 0) {
				try {
					PreparedStatement getAnyQuote = IRCBot.getInstance().getPreparedStatement("getAnyQuote");
					ResultSet results = getAnyQuote.executeQuery();
					if (results.next()) {
						IRCBot.bot.sendIRC().message(event.getChannel().getName(), "Quote #" + results.getString(1) + ": <" + pcl.lc.utils.Helper.antiPing(results.getString(2)) + "> " + results.getString(3));
					}
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (args.length == 1) {
				String idIdentificationCharacter = "#";
				String key = args[0];
				if (key.substring(0, 1).equals(idIdentificationCharacter)) {
					String id = key.replace(idIdentificationCharacter, "");
					try {
						PreparedStatement getQuote = IRCBot.getInstance().getPreparedStatement("getIdQuote");
						getQuote.setString(1, id);
						ResultSet results = getQuote.executeQuery();
						if (results.next()) {
							IRCBot.bot.sendIRC().message(event.getChannel().getName(), "Quote #" + id + ": <" + pcl.lc.utils.Helper.antiPing(results.getString(1)) + "> " + results.getString(2));
						}
						else {
							IRCBot.bot.sendIRC().message(event.getChannel().getName(), sender + ": " + "No quotes found for id " + id);
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					try	{
						PreparedStatement getQuote = IRCBot.getInstance().getPreparedStatement("getUserQuote");
						getQuote.setString(1, key.toLowerCase());
						ResultSet results = getQuote.executeQuery();
						if (results.next()) {
							IRCBot.bot.sendIRC().message(event.getChannel().getName(), "Quote #" + results.getString(1) + ": <" + pcl.lc.utils.Helper.antiPing(key) + "> " + results.getString(2));
						}
						else {
							IRCBot.bot.sendIRC().message(event.getChannel().getName(), sender + ": " + "No quotes found for " + pcl.lc.utils.Helper.antiPing(key));
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} else if (command.equals(prefix + "addquote")) {
			if (args.length > 1) {
				String key = args[0];
				String data = StringUtils.join(args, " ", 1, args.length);
				try {
					PreparedStatement addQuote = IRCBot.getInstance().getPreparedStatement("addQuote");
					addQuote.setString(1, key);
					addQuote.setString(2, data);
					if (addQuote.executeUpdate() > 0) {
						IRCBot.bot.sendIRC().message(event.getChannel().getName(), sender + ": " + "Quote added at id: " + addQuote.getGeneratedKeys().getInt(1) );
					} else {
						IRCBot.bot.sendIRC().message(event.getChannel().getName(), sender + ": " + "An error occurred while trying to set the value.");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (command.equals(prefix + "delquote")) {
			boolean isOp = Permissions.isOp(event.getBot(), event.getUser());
			if (isOp || event.getChannel().isOp(event.getUser())) {
				if (args.length == 1) {
					String key = args[0];
					//String data = StringUtils.join(args, " ", 1, args.length);
					try {
						PreparedStatement removeQuote = IRCBot.getInstance().getPreparedStatement("removeQuote");
						removeQuote.setString(1, key);
						//removeQuote.setString(2, data);
						if (removeQuote.executeUpdate() > 0) {
							event.respond("Quote removed.");
						} else {
							event.respond("An error occurred while trying to set the value.");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} else if (command.equals(prefix + "listquotes")) {
			if (args.length == 1) {
				String key = args[0];
				try	{
					PreparedStatement getUserQuoteAll = IRCBot.getInstance().getPreparedStatement("getUserQuoteAll");
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
						IRCBot.bot.sendIRC().message(event.getChannel().getName(), "User <" + pcl.lc.utils.Helper.antiPing(key) + "> has " + returnValues.size() + " quotes: " + ids);
					}
					else {
						IRCBot.bot.sendIRC().message(event.getChannel().getName(), sender + ": " + "No quotes found for " + key);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}


	@Override
	public void handleCommand(String nick, GenericMessageEvent event,
			String command, String[] copyOfRange) {
		// TODO Auto-generated method stub

	}
	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		// TODO Auto-generated method stub

	}
}