package pcl.lc.irc.hooks;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static gcardone.junidecode.Junidecode.*;

import io.github.firemaples.language.Language;
import io.github.firemaples.translate.Translate;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Weather extends AbstractListener {
	Command local_command;

	String prefix = Config.commandprefix;
	private String chan;

	@Override
	protected void initHook() {
		local_command = new Command("weather") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					Helper.sendMessage(target, getWeather(params));
				} catch (Exception e) {
					e.printStackTrace();
					Helper.sendMessage(target, e.getCause().getMessage());
				}
			}
		};
		local_command.registerAlias("w");
		local_command.setHelpText("Returns weather data for the supplied postal code, or Place name");
		IRCBot.registerCommand(local_command);
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args, String callingRelay) {
		if (command.equals(Config.commandprefix + "weather") || command.equals(Config.commandprefix + "w")) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange, String callingRelay) {
		if ((command.equalsIgnoreCase(prefix + "weather") || command.equalsIgnoreCase(prefix + "w"))) {
		}
	}

	public boolean isNumeric(String s) {  
		return s != null && s.matches("[-+]?\\d*\\.?\\d+");  
	} 

	public String getWeather(String location) throws XPathExpressionException, ParserConfigurationException, MalformedURLException, SAXException, IOException {
		if (Config.botConfig.containsKey("WeatherAPI")) {
			if (Config.botConfig.containsKey("AzureTextAPI")) {
				Translate.setSubscriptionKey(Config.AzureTextAPI);
			}
			if (!isNumeric(location)){
				if (Config.botConfig.containsKey("AzureTextAPI")) {
					try {
						location = Translate.execute(location, Language.AUTO_DETECT, Language.ENGLISH);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (location.contains(",")) {
					String[] tmp = location.split(",");
					location = unidecode(tmp[1].trim()) + "/" + unidecode(tmp[0].trim());
				} else {
					location = unidecode(location.trim());
				}
			}

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			location = location.trim().replace("+", "%20").replace(" ",  "%20").replace("-", "%20");
			System.out.println("http://api.wunderground.com/api/" +Config.weatherAPI + "/conditions/q/" + location + ".xml");
			Document doc = db.parse(new URL("http://api.wunderground.com/api/" +Config.weatherAPI + "/conditions/q/" + location + ".xml").openStream());
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression expr = xpath.compile("//response/results/result/l");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);

			NodeList nodes = (NodeList) result;
			if (nodes.getLength() > 0) {
				String newpath = (String) xpath.evaluate("/response/results/result/l", doc, XPathConstants.STRING);
				System.out.println("http://api.wunderground.com/api/" +Config.weatherAPI + "/conditions" + newpath + ".xml");
				doc = db.parse(new URL("http://api.wunderground.com/api/" +Config.weatherAPI + "/conditions" + newpath + ".xml").openStream());
			} 

			String location_name = (String) xpath.evaluate("/response/current_observation/display_location/full", doc, XPathConstants.STRING);
			String temp_C = (String) xpath.evaluate("/response/current_observation/temp_c", doc, XPathConstants.STRING);
			String temp_F = (String) xpath.evaluate("/response/current_observation/temp_f", doc, XPathConstants.STRING);
			String FeelsLikeC = (String) xpath.evaluate("/response/current_observation/feelslike_c", doc, XPathConstants.STRING);
			String FeelsLikeF = (String) xpath.evaluate("/response/current_observation/feelslike_f", doc, XPathConstants.STRING);
			String humidity = (String) xpath.evaluate("/response/current_observation/relative_humidity", doc, XPathConstants.STRING);
			String windspeedMiles = (String) xpath.evaluate("/response/current_observation/wind_mph", doc, XPathConstants.STRING);
			String windspeedKmph = (String) xpath.evaluate("/response/current_observation/wind_kph", doc, XPathConstants.STRING);
			String weather = (String) xpath.evaluate("/response/current_observation/weather", doc, XPathConstants.STRING);
			String winddir16Point = (String) xpath.evaluate("/response/current_observation/wind_dir", doc, XPathConstants.STRING);

			if (location_name.length() > 0) {
				return ("Current weather for " + location_name + " Current Temp: " + temp_F + "°F/" + temp_C + "°C Feels Like: " + FeelsLikeF + "°F/" + FeelsLikeC + "°C Current Humidity: " + humidity + " Wind: From the " + winddir16Point + " " + windspeedMiles + " Mph/" + windspeedKmph + " Km/h Conditions: " + weather);					
			} else {
				return ("No data returned");
			}
		} else {
			return ("API Key missing, alert an admin.");
		}
	}
}
