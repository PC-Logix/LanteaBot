package pcl.lc.irc.hooks;

import com.google.common.collect.Lists;

import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.*;
import pcl.lc.irc.entryClasses.*;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

public class DNSBL extends AbstractListener {
	private Command toggle_command;
	private Command check_command;
	private Command adddnsbl_command;
	private Command remdnsbl_command;
	private Command listdnsbl_command;

	private static DirContext ictx;
	private static List<String> dnsbls = Lists.newArrayList();

	static {
		Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
		env.put("com.sun.jndi.dns.timeout.initial", "150");
		env.put("com.sun.jndi.dns.timeout.retries", "1");
		env.put(Context.PROVIDER_URL, "dns://4.2.2.4");
		try {
			ictx = new InitialDirContext(env);
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onJoin(final JoinEvent event) {
		if (event.getUser().getNick().equals(IRCBot.getOurNick()))
			return;
		if (Helper.isEnabledHere(event.getChannel().getName(), "DNSBL")) {
			InetAddress address = null;
			try {
				address = InetAddress.getByName(event.getUserHostmask().getHostname());
			} catch (Exception e) {
			}
			System.out.println(address.toString().split("/")[1]);
			String[] parts = address.toString().split("/")[1].split("\\.");
			String reversedAddress = parts[3] + "." + parts[2] + "." + parts[1] + "." + parts[0];
			Attribute attribute;
			Attributes attributes;
			String foundOn = "";
			for (String service : dnsbls) {
				System.out.println("Trying " + service);
				try {
					attributes = ictx.getAttributes(reversedAddress + "." + service, new String[]{"A"});
					attribute = attributes.get("A");
					if (attribute != null) {
						foundOn += service + ", ";
					}
				} catch (Exception e) {
				}
			}
			if (foundOn.length() > 1) {
				//Helper.sendMessage(event.getChannel().getName(), event.getUserHostmask().getHostname() + " found on " + foundOn.replaceAll(", $", ""));
				TimedBans.setTimedBan(event.getChannel(), event.getUser().getNick(), event.getUserHostmask().getHostname(), "6h", "Listed on " + foundOn.replaceAll(", $", ""), "DNSBL Check");
			}
		}
	}

	@Override
	protected void initHook() {
		Database.addStatement("CREATE TABLE IF NOT EXISTS DNSBLs(url)");
		Database.addPreparedStatement("addDNSBL", "INSERT INTO DNSBLs(url) VALUES (?);");
		Database.addPreparedStatement("getDNSBLs", "SELECT url FROM DNSBLs;");
		Database.addPreparedStatement("removeDNSBL", "DELETE FROM DNSBLs WHERE url = ?;");

		PreparedStatement getDNSBLs;
		try {
			getDNSBLs = Database.getPreparedStatement("getDNSBLs");
			ResultSet results = getDNSBLs.executeQuery();
			while (results.next()) {
				dnsbls.add(results.getString(1));
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		toggle_command = new Command("dnsbl", new CommandArgumentParser(0, new CommandArgument("State", ArgumentTypes.STRING)), new CommandRateLimit(10), Permissions.MOD) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String state = this.argumentParser.getArgument("State");
				if (state != null && (state.equals("disable") || state.equals("enable"))) {
					Helper.toggleCommand("DNSBL", target, state);
				} else {
					String isEnabled = Helper.isEnabledHere(target, "DNSBL") ? "enabled" : "disabled";
					Helper.sendMessage(target, "DNSBL is " + isEnabled + " in this channel", nick);
				}
			}
		};
		toggle_command.setHelpText("DNSBL check on join");
		IRCBot.registerCommand(toggle_command);


		check_command = new Command("checkdnsbl", new CommandArgumentParser(1, new CommandArgument("Address", ArgumentTypes.STRING)), new CommandRateLimit(10), Permissions.EVERYONE) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String addr = this.argumentParser.getArgument("Address");
				InetAddress address = null;
				try {
					address = InetAddress.getByName(addr);
				} catch (Exception e) {
				}
				System.out.println(address.toString().split("/")[1]);
				String[] parts = address.toString().split("/")[1].split("\\.");
				String reversedAddress = parts[3] + "." + parts[2] + "." + parts[1] + "." + parts[0];
				Attribute attribute;
				Attributes attributes;
				String foundOn = "";
				for (String service : dnsbls) {
					System.out.println("Trying " + service);
					try {
						attributes = ictx.getAttributes(reversedAddress + "." + service, new String[]{"A"});
						attribute = attributes.get("A");
						if (attribute != null) {
							foundOn += service + ", ";
						}
					} catch (Exception e) {
					}
				}
				if (foundOn.length() > 1) {
					Helper.sendMessage(target, addr + " found on " + foundOn.replaceAll(", $", ""));
				} else {
					Helper.sendMessage(target, "Host wasn't found on any tracked DNSBLs");
				}
			}
		};
		check_command.setHelpText("DNSBL check on demand");
		IRCBot.registerCommand(check_command);


		adddnsbl_command = new Command("adddnsbl", new CommandArgumentParser(1, new CommandArgument("Address", ArgumentTypes.STRING)), new CommandRateLimit(10), Permissions.MOD) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				String address = this.argumentParser.getArgument("Address");
				PreparedStatement addDNSBL = Database.getPreparedStatement("addDNSBL");
				addDNSBL.setString(1, address.trim());
				if (addDNSBL.executeUpdate() > 0) {
					dnsbls.add(address.trim());
					Helper.sendMessage(target, "Added " + address.trim(), nick);
					return;
				}
				Helper.sendMessage(target, "An error occurred while trying to add the service.", nick);
			}
		};
		toggle_command.setHelpText("Add DNSBL Service");
		IRCBot.registerCommand(adddnsbl_command);

		remdnsbl_command = new Command("remdnsbl", new CommandArgumentParser(1, new CommandArgument("Address", ArgumentTypes.STRING)), new CommandRateLimit(10), Permissions.MOD) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				String address = this.argumentParser.getArgument("Address");
				PreparedStatement removeDNSBL = Database.getPreparedStatement("removeDNSBL");
				removeDNSBL.setString(1, address.trim());
				if (removeDNSBL.executeUpdate() > 0) {
					dnsbls.remove(address.trim());
					Helper.sendMessage(target, "Removed " + address.trim(), nick);
					return;
				}
				Helper.sendMessage(target, "An error occurred while trying to remove the service.", nick);
			}
		};
		toggle_command.setHelpText("Remove DNSBL Service");
		IRCBot.registerCommand(remdnsbl_command);

		listdnsbl_command = new Command("listdnsbl", new CommandRateLimit(10), Permissions.EVERYONE) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String dnsblServices = "";
				Iterator<String> iter = dnsbls.iterator();
				while (iter.hasNext()) {
					dnsblServices += iter.next() + ", ";
				}
				if (dnsblServices.length() > 1) {
					Helper.sendMessage(target, dnsblServices.replaceAll(", $", ""), nick, true);
				} else {
					Helper.sendMessage(target, "No DNSBLs tracked");
				}
			}
		};
		toggle_command.setHelpText("List DNSBL services");
		IRCBot.registerCommand(toggle_command);
	}
}