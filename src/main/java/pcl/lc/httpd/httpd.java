package pcl.lc.httpd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

public class httpd {
	static HttpServer server;
    @SuppressWarnings("restriction")
	public static void setup() throws Exception {
        server = HttpServer.create(new InetSocketAddress(Integer.parseInt(Config.httpdport)), 0);
    }
    /**
     * Creates a route from a URL to a HttpHandler
     * @param route
     * @param handlerIn
     */
    public void registerContext(String route,  HttpHandler handlerIn) {
    	server.createContext(route, handlerIn);
    }
    
    public static void start() throws Exception {
        server.setExecutor(null); // creates a default executor
        server.start();
    }
}
