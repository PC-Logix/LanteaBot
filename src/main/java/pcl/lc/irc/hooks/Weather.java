package pcl.lc.irc.hooks;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Weather extends AbstractListener {

	String prefix = Config.commandprefix;

	@Override
	protected void initCommands() {
		IRCBot.registerCommand("weather", "Returns weather data for the supplied postal code, or Place name");
	}
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		//if (!IRCBot.isIgnored(sender) && (command.equalsIgnoreCase(prefix + "weather") || command.equalsIgnoreCase(prefix + "w"))) {

		//}
	}
	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (!IRCBot.isIgnored(nick) && (command.equalsIgnoreCase(prefix + "weather") || command.equalsIgnoreCase(prefix + "w"))) {
			String location = "";
			for( int i = 0; i < copyOfRange.length; i++)
			{
				location = location + " " + copyOfRange[i];
			}
			try {
				event.respond(getWeather(location));
			} catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getWeather(String location) throws XPathExpressionException, ParserConfigurationException, MalformedURLException, SAXException, IOException {
		if (Config.botConfig.containsKey("WeatherAPI")) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			location = URLEncoder.encode(location, "UTF-8").replace("+", "%20");
			Document doc = db.parse(new URL("http://api.worldweatheronline.com/free/v2/weather.ashx?q=" + location + "&format=XML&key="+Config.weatherAPI).openStream());
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			String location_name = (String) xpath.evaluate("/data/request/query", doc, XPathConstants.STRING);
			String temp_C = (String) xpath.evaluate("/data/current_condition/temp_C", doc, XPathConstants.STRING);
			String temp_F = (String) xpath.evaluate("/data/current_condition/temp_F", doc, XPathConstants.STRING);
			String FeelsLikeC = (String) xpath.evaluate("/data/current_condition/FeelsLikeC", doc, XPathConstants.STRING);
			String FeelsLikeF = (String) xpath.evaluate("/data/current_condition/FeelsLikeF", doc, XPathConstants.STRING);
			String humidity = (String) xpath.evaluate("/data/current_condition/humidity", doc, XPathConstants.STRING);
			String windspeedMiles = (String) xpath.evaluate("/data/current_condition/windspeedMiles", doc, XPathConstants.STRING);
			String windspeedKmph = (String) xpath.evaluate("/data/current_condition/windspeedKmph", doc, XPathConstants.STRING);
			String weather = (String) xpath.evaluate("/data/current_condition/weatherDesc", doc, XPathConstants.STRING);
			String winddir16Point = (String) xpath.evaluate("/data/current_condition/winddir16Point", doc, XPathConstants.STRING);

			if (weather.length() > 0) {
				return ("Current weather for " + location_name + " Current Temp: " + temp_F + "°F/" + temp_C + "°C Feels Like: " + FeelsLikeF + "°F/" + FeelsLikeC + "°C Current Humidity: " + humidity + " Wind: From the " + winddir16Point + " " + windspeedMiles + " Mph/" + windspeedKmph + " Km/h Conditions: " + weather);					
			} else {
				return ("No data returned");
			}
		} else {
			return ("API Key missing, alert an admin.");
		}
	}

}
