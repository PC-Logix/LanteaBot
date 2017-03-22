package pcl.lc.httpd;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import pcl.lc.irc.Config;
import pcl.lc.irc.Database;
import pcl.lc.irc.IRCBot;

@SuppressWarnings("restriction")
public class httpd {
	static HttpServer server;
	static String baseDomain;
	public static void setup() throws Exception {
        server = HttpServer.create(new InetSocketAddress(Integer.parseInt(Config.httpdport)), 0);
    }
    /**
     * Creates a route from a URL to a HttpHandler
     * @param route
     * @param handlerIn
     */
	public static void registerContext(String route,  HttpHandler handlerIn) {
		if(server != null)
			server.createContext(route, handlerIn);
    }
    
	public static void start() throws Exception {
		if(server != null) {
			registerContext("/", new IndexHandler());
			IRCBot.log.info("Starting HTTPD On port " + Config.httpdport + " Base domain: " + Config.httpdBaseDomain);
			server.setExecutor(null); // creates a default executor
	        server.start();
		} else {
			IRCBot.log.error("httpd server was null!");
		}
    }
	public static void setBaseDomain(String httpdBaseDomain) {
		baseDomain = httpdBaseDomain;
	}
	
	public static String getBaseDomain() {
		return baseDomain;
	}
	
	static class IndexHandler implements HttpHandler {
		
		static String html;
		@Override
		public void handle(HttpExchange t) throws IOException {
			InputStream htmlIn = getClass().getResourceAsStream("/html/index.html");
			html = CharStreams.toString(new InputStreamReader(htmlIn, Charsets.UTF_8));

			String target = t.getRequestURI().toString();
			String response = "";

			// convert String into InputStream
			InputStream is = new ByteArrayInputStream(html.getBytes());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String line = null;
				while ((line = br.readLine()) != null) {
					response = response + line.replace("#BODY#", target).replace("#BOTNICK#", IRCBot.getOurNick())+"\n";
				}
			}
			t.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
}
