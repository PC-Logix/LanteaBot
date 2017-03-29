package pcl.lc.httpd;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

@SuppressWarnings("restriction")
public class httpd {
	static HttpServer server;
	static String baseDomain;
	public static Map<String, String> pages = new LinkedHashMap<String, String>();
	public static void setup() throws Exception {
        server = HttpServer.create(new InetSocketAddress(Integer.parseInt(Config.httpdport)), 0);
		registerContext("/", new IndexHandler(), "Home");
	}
    /**
     * Creates a route from a URL to a HttpHandler
     * @param route
     * @param handlerIn
     * @param pageName
     */
	public static void registerContext(String route,  HttpHandler handlerIn, String pageName) {
		if(server != null) {
			IRCBot.log.info("Adding " + pageName + " to page list");
			pages.put(pageName, route);
			server.createContext(route, handlerIn);
		}
    }
    
	public static void start() throws Exception {
		if(server != null) {
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

			String navData = "";
		    Iterator it = pages.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pair = (Map.Entry)it.next();
		        navData += "<div class=\"innertube\"><h1><a href=\""+ pair.getValue() +"\">"+ pair.getKey() +"</a></h1></div>";
		    }
		    
			// convert String into InputStream
			InputStream is = new ByteArrayInputStream(html.getBytes());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String line = null;
				while ((line = br.readLine()) != null) {
					response = response + line.replace("#BODY#", target).replace("#BOTNICK#", IRCBot.getOurNick()).replace("#NAVIGATION#", navData)+"\n";
				}
			}
			t.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
}
