package pcl.lc.httpd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

public class httpd {

    @SuppressWarnings("restriction")
	public static void start() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(Integer.parseInt(Config.httpdport)), 0);
        server.createContext("/quotes", new QuoteHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class QuoteHandler implements HttpHandler {
        @SuppressWarnings("restriction")
		@Override
        public void handle(HttpExchange t) throws IOException {
        	
			String target = t.getRequestURI().toString();
        	String response = "";
        	String quoteList = "";
    			try {
    				PreparedStatement getAllQuotes = IRCBot.getInstance().getPreparedStatement("getAllQuotes");
    				ResultSet results = getAllQuotes.executeQuery();
    				while (results.next()) {
    					try {
    						PreparedStatement getQuote = IRCBot.getInstance().getPreparedStatement("getIdQuote");
    						getQuote.setString(1, results.getString(1));
    						ResultSet results2 = getQuote.executeQuery();
    						if (results2.next()) {
    							quoteList = quoteList + "#" + results.getString(1) + ": &lt;" + results2.getString(1) + "&gt; " + results2.getString(2) + "<br>\n";
    						}
    					}
    					catch (Exception e) {
    						e.printStackTrace();
    					}
    					//quoteList = quoteList + "<a href=\"?id=" + results.getString(1) +"\">Quote #"+results.getString(1)+"</a><br>\n";
    				}
    			}
    			catch (Exception e) {
    				e.printStackTrace();
    			}
        	try (BufferedReader br = new BufferedReader(new FileReader("webroot/quotes.html"))) {
     		   String line = null;
     		   while ((line = br.readLine()) != null) {
     		       response = response + line.replace("#BODY#", target).replace("#BOTNICK#", IRCBot.getInstance().ournick)+"\n";
     		   }
     		} catch (Exception e) {
				e.printStackTrace();
			}
            //String response = "This is the response";
        	System.out.println(response.length());
            t.sendResponseHeaders(200, response.length());


			IRCBot.bot.sendIRC().message("#MichiBot", target + " from ip: " + t.getRemoteAddress());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

}
