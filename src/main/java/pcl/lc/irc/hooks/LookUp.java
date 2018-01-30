package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import com.google.common.net.InetAddresses;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
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
 *
 */
@SuppressWarnings("rawtypes")
public class LookUp extends AbstractListener {
	private Command local_command_lookup;
	private Command local_command_rdns;

	@Override
	protected void initHook() {
		local_command_lookup = new Command("lookup", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {	
		        try
		        {
		            InetAddress inetAddress;
		            String recordType;
		            if (params.size() > 1) {
		            	recordType = params.get(1);
		            } else {
		            	recordType = "A";
		            }
		            // if first character is a digit then assume is an address
		            if (Character.isDigit(params.get(0).charAt(0)))
		            {   // convert address from string representation to byte array
		                byte[] b = new byte[4];
		                String[] bytes = params.get(0).split("[.]");
		                for (int i = 0; i < bytes.length; i++)
		                {
		                    b[i] = new Integer(bytes[i]).byteValue();
		                }
		                // get Internet Address of this host address
		                inetAddress = InetAddress.getByAddress(b);
		            }
		            else
		            {   // get Internet Address of this host name
		                inetAddress = InetAddress.getByName(params.get(0));
		            }
		            // get the default initial Directory Context
		            InitialDirContext iDirC = new InitialDirContext();
		            // get the DNS records for inetAddress
		            Attributes attributes = iDirC.getAttributes("dns:/" + inetAddress.getHostName());
		            // get an enumeration of the attributes and print them out
		            NamingEnumeration attributeEnumeration = attributes.getAll();
		            while (attributeEnumeration.hasMore())
		            {
		            	String record = ""+ attributeEnumeration.next();
		            	String[] thisType = record.split(":", 2);
		            	if (thisType[0].equalsIgnoreCase(recordType) || recordType.equalsIgnoreCase("any")) {
			            	Helper.sendMessage(target, "" + record);
		            	}
		            }
		            attributeEnumeration.close();
		        }
		        catch (UnknownHostException exception)
		        {
		        	Helper.sendMessage(target, "ERROR: No Internet Address for '" + params.get(0) + "'");
		        }
		        catch (NamingException exception)
		        {
		        	Helper.sendMessage(target, "ERROR: No DNS record for '" + params.get(0) + "'");
		        	exception.printStackTrace();
		        }
				
				//Helper.sendMessage(target, output.replace(params.get(0) + "/", " ").replaceAll("((?::0\\b){2,}):?(?!\\S*\\b\\1:0\\b)(\\S*)", "::$2"), nick);
			}
		}; local_command_lookup.setHelpText("Returns DNS information");
		local_command_rdns = new Command("rdns", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				InetAddress addr = null;
				try {
					addr = InetAddress.getByName(params.get(0));
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String host = addr.getCanonicalHostName();
				String output = "Reverse DNS Info for " + params.get(0) + " " + host;
				Helper.sendMessage(target, output, nick);
			}
		}; local_command_rdns.setHelpText("Returns Reverse DNS information");
		IRCBot.registerCommand(local_command_lookup);
		IRCBot.registerCommand(local_command_rdns);
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		target = Helper.getTarget(event);
		local_command_lookup.tryExecute(command, nick, target, event, copyOfRange);
		local_command_rdns.tryExecute(command, nick, target, event, copyOfRange);
	}}
