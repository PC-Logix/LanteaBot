package pcl.lc.irc.hooks;

import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.w3c.dom.Document;

import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Weather extends ListenerAdapter {
	public Weather() {
		IRCBot.registerCommand("weather");
	}
	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		String prefix = IRCBot.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		if (trigger.length() > 1) {
			String[] firstWord = StringUtils.split(trigger);
			String triggerWord = firstWord[0];
			if (triggerWord.equals(prefix + "weather")) {
				if (IRCBot.botConfig.containsKey("WUndergroundAPI")) {
					String loc = event.getMessage().substring(event.getMessage().indexOf("weather") + 7).trim();
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					String location = loc;
					location = URLEncoder.encode(location, "UTF-8").replace("+", "%20");
					Document doc = db.parse(new URL("http://api.worldweatheronline.com/free/v2/weather.ashx?q=" + location + "&format=XML&key=15a0c07dbaa418e896c1be9d542b8").openStream());
					//System.out.println("http://api.wunderground.com/api/" + IRCBot.botConfig.get("WUndergroundAPI") + "/conditions/q/" + location + ".xml");
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
						event.respond("Current weather for " + location_name + " Current Temp: " + temp_F + "f/" + temp_C + "c Feels Like: " + FeelsLikeF + "f/" + FeelsLikeC + "c Current Humidity: " + humidity + " Wind: From the " + winddir16Point + " " + windspeedMiles + " Mph/" + windspeedKmph + " KPH Conditions: " + weather);					
					} else {
						event.getUser().send().notice("No data returned");
					}
				} else {
					event.getUser().send().notice("API Key missing, alert an admin.");
				}
			}
		}
	}
}
