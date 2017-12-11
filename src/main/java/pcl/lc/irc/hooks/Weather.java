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

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Weather extends AbstractListener {

	String prefix = Config.commandprefix;
	private String chan;

	public Map<String, String> states = new HashMap<String, String>();
	
	public Weather() {
		states.put("Alabama","AL");
		states.put("Alaska","AK");
		states.put("Alberta","AB");
		states.put("American Samoa","AS");
		states.put("Arizona","AZ");
		states.put("Arkansas","AR");
		states.put("Armed Forces (AE)","AE");
		states.put("Armed Forces Americas","AA");
		states.put("Armed Forces Pacific","AP");
		states.put("British Columbia","BC");
		states.put("California","CA");
		states.put("Colorado","CO");
		states.put("Connecticut","CT");
		states.put("Delaware","DE");
		states.put("District Of Columbia","DC");
		states.put("Florida","FL");
		states.put("Georgia","GA");
		states.put("Guam","GU");
		states.put("Hawaii","HI");
		states.put("Idaho","ID");
		states.put("Illinois","IL");
		states.put("Indiana","IN");
		states.put("Iowa","IA");
		states.put("Kansas","KS");
		states.put("Kentucky","KY");
		states.put("Louisiana","LA");
		states.put("Maine","ME");
		states.put("Manitoba","MB");
		states.put("Maryland","MD");
		states.put("Massachusetts","MA");
		states.put("Michigan","MI");
		states.put("Minnesota","MN");
		states.put("Mississippi","MS");
		states.put("Missouri","MO");
		states.put("Montana","MT");
		states.put("Nebraska","NE");
		states.put("Nevada","NV");
		states.put("New Brunswick","NB");
		states.put("New Hampshire","NH");
		states.put("New Jersey","NJ");
		states.put("New Mexico","NM");
		states.put("New York","NY");
		states.put("Newfoundland","NF");
		states.put("North Carolina","NC");
		states.put("North Dakota","ND");
		states.put("Northwest Territories","NT");
		states.put("Nova Scotia","NS");
		states.put("Nunavut","NU");
		states.put("Ohio","OH");
		states.put("Oklahoma","OK");
		states.put("Ontario","ON");
		states.put("Oregon","OR");
		states.put("Pennsylvania","PA");
		states.put("Prince Edward Island","PE");
		states.put("Puerto Rico","PR");
		states.put("Quebec","QC");
		states.put("Rhode Island","RI");
		states.put("Saskatchewan","SK");
		states.put("South Carolina","SC");
		states.put("South Dakota","SD");
		states.put("Tennessee","TN");
		states.put("Texas","TX");
		states.put("Utah","UT");
		states.put("Vermont","VT");
		states.put("Virgin Islands","VI");
		states.put("Virginia","VA");
		states.put("Washington","WA");
		states.put("West Virginia","WV");
		states.put("Wisconsin","WI");
		states.put("Wyoming","WY");
		states.put("Yukon Territory","YT");
	}
	
	@Override
	protected void initHook() {
		IRCBot.registerCommand("weather", "Returns weather data for the supplied postal code, or Place name");
	}
	
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (command.equals(Config.commandprefix + "weather") || command.equals(Config.commandprefix + "w")) {
			chan = event.getChannel().getName();
		}
	}
	
	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (!IRCBot.isIgnored(nick) && (command.equalsIgnoreCase(prefix + "weather") || command.equalsIgnoreCase(prefix + "w"))) {
			String location = "";
			for( int i = 0; i < copyOfRange.length; i++)
			{
				location = location + " " + copyOfRange[i];
			}
			String target = Helper.getTarget(event);
			try {
				Helper.sendMessage(target, getWeather(location));
			} catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getWeather(String location) throws XPathExpressionException, ParserConfigurationException, MalformedURLException, SAXException, IOException {
		if (Config.botConfig.containsKey("WeatherAPI")) {
			if (location.contains(",")){
				String[] tmp = location.split(",");
				location = unidecode(tmp[1].trim()) + "/" + unidecode(tmp[0].trim());
				System.out.println(location);
			}
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			location = location.trim().replace("+", "%20").replace(" ",  "%20");
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
