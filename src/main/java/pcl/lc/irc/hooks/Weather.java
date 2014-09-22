package pcl.lc.irc.hooks;

import java.net.URL;

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
				if (IRCBot.botConfig.get("WUndergroundAPI") != null) {
					String[] loc = event.getMessage().split(" ");
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					Document doc = db.parse(new URL("http://api.wunderground.com/api/" + IRCBot.botConfig.get("WUndergroundAPI") + "/conditions/q/" + loc[1] + ".xml").openStream());
					XPathFactory xPathfactory = XPathFactory.newInstance();
					XPath xpath = xPathfactory.newXPath();
					String location_name = (String) xpath.evaluate("/response/current_observation/display_location/full", doc, XPathConstants.STRING);
					String temperature= (String) xpath.evaluate("/response/current_observation/temperature_string", doc, XPathConstants.STRING);
					String relative_humidity = (String) xpath.evaluate("/response/current_observation/relative_humidity", doc, XPathConstants.STRING);
					String wind = (String) xpath.evaluate("/response/current_observation/wind_string", doc, XPathConstants.STRING);
					String weather = (String) xpath.evaluate("/response/current_observation/weather", doc, XPathConstants.STRING);
					event.respond("Current weather for " + location_name + " Current Temp: " + temperature + " Current Humidity: " + relative_humidity + " Wind: " + wind + " Conditions: " + weather);
				}
			}
		}
	}
}
