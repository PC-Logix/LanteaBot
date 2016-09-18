package pcl.lc.httpd;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

public class httpd {

    public static void start() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(Integer.parseInt(Config.httpdport)), 0);
        server.createContext("/test", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
			String target = t.getRequestURI().toString();

			IRCBot.bot.sendIRC().message("#MichiBot", target);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

}
