package pcl.lc.httpd;

import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

@SuppressWarnings("restriction")
public class httpd {
	static HttpServer server;
	public static void setup() throws Exception {
        server = HttpServer.create(new InetSocketAddress(Integer.parseInt(Config.httpdport)), 0);
    }
    /**
     * Creates a route from a URL to a HttpHandler
     * @param route
     * @param handlerIn
     */
	public void registerContext(String route,  HttpHandler handlerIn) {
		if(server != null)
			server.createContext(route, handlerIn);
    }
    
	public static void start() throws Exception {
		if(server != null) {
			IRCBot.log.info("Starting HTTPD On port " + Config.httpdport);
			server.setExecutor(null); // creates a default executor
	        server.start();
		} else {
			IRCBot.log.error("httpd server was null!");
		}
    }
}
