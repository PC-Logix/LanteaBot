package pcl.lc.irc.hooks;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.io.FilenameFilter;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;
import org.pircbotx.hooks.types.GenericMessageEvent;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Subdivision;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.CommandChainStateObject;
import pcl.lc.utils.Helper;
import pcl.lc.utils.UnTarGZ;

public class IP2Geo extends AbstractListener {

	private static Calendar cacheCalendar;
	private static DatabaseReader reader;
	private Command local_command;
	private File ip2geodb = null;
	@Override
	protected void initHook() {
		cacheCalendar = Calendar.getInstance();
		int year = cacheCalendar.get(Calendar.YEAR);
		int month = cacheCalendar.get(Calendar.MONTH) + 1;
		int day = getFirstTuesday(year, month);
		DecimalFormat mFormat= new DecimalFormat("00");
		mFormat.setRoundingMode(RoundingMode.DOWN);
		String Dates =  mFormat.format(Double.valueOf(year)) +  mFormat.format(Double.valueOf(month)) + mFormat.format(Double.valueOf(day));
		File f = new File("./ip2geo/GeoLite2-City_" + Dates + "/");
		if (f.exists() && f.isDirectory()) {
			ip2geodb = new File("./ip2geo/GeoLite2-City_" + Dates + "/GeoLite2-City.mmdb");
			cleanup();
		} else {
			try {
				FileUtils.deleteDirectory(new File("./ip2geo/"));
				new File("./ip2geo/").mkdir();
				FileUtils.copyURLToFile(new URL("http://geolite.maxmind.com/download/geoip/database/GeoLite2-City.tar.gz"), new File("./ip2geo/GeoLite2-City.tar.gz"),3000,3000);
				UnTarGZ.unGzip(new File("./ip2geo/GeoLite2-City.tar.gz"), new File("./ip2geo/"));
				List<File> outFiles = UnTarGZ.unTar(new File("./ip2geo/GeoLite2-City.tar"), new File("./ip2geo/"));
				for(File file : outFiles) {
					if (file.isDirectory() && file.getName().contains("GeoLite2-City")) {
						ip2geodb = new File("./ip2geo/"+ file.getName() + "/GeoLite2-City.mmdb");
					}
				}			
				cleanup();
			} catch (IOException | ArchiveException e) {
				e.printStackTrace();
			}
		}


		if (ip2geodb != null) {
			try {
				reader = new DatabaseReader.Builder(ip2geodb).withCache(new CHMCache()).build();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		local_command = new Command("geoip", new CommandArgumentParser(0, new CommandArgument("Location", ArgumentTypes.STRING))) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String location = this.argumentParser.getArgument("Location");
				Helper.AntiPings = Helper.getNamesFromTarget(location);
				if (location != null) {
					Helper.sendMessage(target, getGeoIP(location), nick);
				} else {
					Helper.sendMessage(target, getGeoIP(event.getUserHostmask().getHostname()), nick);					
				}
				return new CommandChainStateObject();
			}
		};
		local_command.setHelpText("gets GeoIP information");
		IRCBot.registerCommand(local_command);

	}

	public void cleanup() {
		final File folder = new File("./ip2geo/");
		final File[] files = folder.listFiles( new FilenameFilter() {
			@Override
			public boolean accept( final File dir, final String name ) {
				return name.matches( "GeoLite2-City.*\\.gz" ) || name.matches( "GeoLite2-City.*\\.tar" );
			}
		} );
		for ( final File file : files ) {
			if ( !file.delete() ) {
				System.err.println( "Can't remove " + file.getAbsolutePath() );
			}
		}
	}
	public static int getFirstTuesday(int year, int month) {
		cacheCalendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
		cacheCalendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
		cacheCalendar.set(Calendar.MONTH, month);
		cacheCalendar.set(Calendar.YEAR, year);
		return cacheCalendar.get(Calendar.DATE);
	}

	private String getGeoIP(String host) {
		if (ip2geodb != null) {
			return "No ip2geo DB found check for errors on the console";
		}
		InetAddress ipAddress;
		try {
			ipAddress = InetAddress.getByName(host);
			CityResponse response = reader.city(ipAddress);
			Country country = response.getCountry();
			Subdivision subdivision = response.getMostSpecificSubdivision();
			City city = response.getCity();
			return country.getName() + " " + subdivision.getName() + " " + city.getName();
		} catch (IOException | GeoIp2Exception e) {
			e.printStackTrace();
			return "N/A";
		}
	}
}
