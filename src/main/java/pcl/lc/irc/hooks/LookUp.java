package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.CommandChainStateObject;
import pcl.lc.utils.Helper;

import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * @author Caitlyn
 */
@SuppressWarnings("rawtypes")
public class LookUp extends AbstractListener {
	private Command local_command_lookup;
	private Command local_command_rdns;

	@Override
	protected void initHook() {
		local_command_lookup = new Command("lookup", new CommandArgumentParser(1, new CommandArgument("Address", ArgumentTypes.STRING), new CommandArgument("RecordType", ArgumentTypes.STRING))) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				String address = this.argumentParser.getArgument("Address");
				try {
					InetAddress inetAddress;
					String recordType = this.argumentParser.getArgument("RecordType");
					if (recordType == null)
						recordType = "A";
					// if first character is a digit then assume is an address
					if (Character.isDigit(address.charAt(0))) {   // convert address from string representation to byte array
						byte[] b = new byte[4];
						String[] bytes = address.split("[.]");
						for (int i = 0; i < bytes.length; i++) {
							b[i] = new Integer(bytes[i]).byteValue();
						}
						// get Internet Address of this host address
						inetAddress = InetAddress.getByAddress(b);
					} else {   // get Internet Address of this host name
						inetAddress = InetAddress.getByName(address);
					}
					// get the default initial Directory Context
					InitialDirContext iDirC = new InitialDirContext();
					// get the DNS records for inetAddress
					Attributes attributes = iDirC.getAttributes("dns:/" + inetAddress.getHostName());
					// get an enumeration of the attributes and print them out
					NamingEnumeration attributeEnumeration = attributes.getAll();
					while (attributeEnumeration.hasMore()) {
						String record = "" + attributeEnumeration.next();
						String[] thisType = record.split(":", 2);
						if (thisType[0].equalsIgnoreCase(recordType) || recordType.equalsIgnoreCase("any")) {
							Helper.sendMessage(target, "" + record);
						}
					}
					attributeEnumeration.close();
				} catch (UnknownHostException exception) {
					Helper.sendMessage(target, "ERROR: No Internet Address for '" + address + "'");
				} catch (NamingException exception) {
					Helper.sendMessage(target, "ERROR: No DNS record for '" + address + "'");
					Helper.sendMessage(target, exception.getExplanation() + " Resolved: " + exception.getResolvedName() + " Unresolved: " + exception.getRemainingName());
					exception.printStackTrace();
				}

				//Helper.sendMessage(target, output.replace(params.get(0) + "/", " ").replaceAll("((?::0\\b){2,}):?(?!\\S*\\b\\1:0\\b)(\\S*)", "::$2"), nick);
				return new CommandChainStateObject();
			}
		};
		local_command_lookup.setHelpText("Returns DNS information");
		local_command_rdns = new Command("rdns") {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws UnknownHostException {
				InetAddress addr = InetAddress.getByName(params.get(0));
				String host = addr.getCanonicalHostName();
				String output = "Reverse DNS Info for " + params.get(0) + " " + host;
				Helper.sendMessage(target, output, nick);
				return new CommandChainStateObject();
			}
		};
		local_command_rdns.setHelpText("Returns Reverse DNS information");
		IRCBot.registerCommand(local_command_lookup);
		IRCBot.registerCommand(local_command_rdns);
	}
}
