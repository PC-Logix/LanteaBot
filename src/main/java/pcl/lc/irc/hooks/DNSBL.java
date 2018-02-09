package pcl.lc.irc.hooks;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.utils.Helper;

public class DNSBL  extends AbstractListener {
	private Command toggle_command;
	private Command check_command;
	
    private static final LoadingCache<String, Boolean> cachedIPs = CacheBuilder.newBuilder()
            .maximumSize(4096)
            .expireAfterAccess(24, TimeUnit.HOURS).build(new CacheLoader<String, Boolean>() {
                @Override
                public Boolean load(String ip) throws Exception {
                    String[] parts = ip.split("\\.");
                    String reversedAddress = parts[3] + "." + parts[2] + "." + parts[1] + "." + parts[0];
                    Attribute attribute;
                    Attributes attributes;
                    for (String service : dnsbls){
                    	System.out.println("Trying " + service);
                        try{
                            attributes = ictx.getAttributes(reversedAddress + "." + service, new String[]{"A"});
                            attribute = attributes.get("A");
                            if (attribute != null){
                                return true;
                            }
                        }catch (Exception e){ }
                    }

                    return false;
                }
            });
        private static DirContext ictx;
        private static final List<String> dnsbls = Lists.newArrayList(
            "dnsbl.proxybl.org", // Confirmed Proxies
            "zombie.dnsbl.sorbs.net", // Zombie Networks
            "http.dnsbl.sorbs.net", // HTTP Proxies
            "socks.dnsbl.sorbs.net", // Socks Proxies
            "misc.dnsbl.sorbs.net", // Misc Proxies
            "xbl.spamhaus.org", // XBL
            "spam.dnsbl.sorbs.net",
            "b.barracudacentral.org",
            "dnsbl.sorbs.net"
        );
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

        public static boolean checkIP(String ip){
            try {
                return cachedIPs.get(ip);
            } catch (ExecutionException e) {
                e.printStackTrace();
                return false;
            }
    }
	
	
	@Override
	public void onJoin(final JoinEvent event) {
		if (event.getUser().getNick().equals(IRCBot.getOurNick()))
			return;
		if (Helper.isEnabledHere(event.getChannel().getName(), "DNSBL")) {
			InetAddress address;
			try {
				address = InetAddress.getByName(event.getUserHostmask().getHostname());
		    	Boolean badIP = checkIP(address.getHostAddress().split("/")[1]);
		    	if (badIP) {
		    		TimedBans.setDNSBLBan(event.getChannel(), event.getUser().getNick(), event.getUserHostmask().getHostname(), "6h", "Listed on DNS Black Lists");
		    	}
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}

	@Override
	protected void initHook() {
		toggle_command = new Command("dnsbl", 10, Permissions.MOD) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (params.equals("disable") || params.equals("enable")) {
					Helper.toggleCommand("DNSBL", target, params);
				} else {
					String isEnabled = Helper.isEnabledHere(target, "DNSBL") ? "enabled" : "disabled";
					Helper.sendMessage(target, "DNSBL is " + isEnabled + " in this channel", nick);
				}
			}
		}; toggle_command.setHelpText("DNSBL check on join");
		IRCBot.registerCommand(toggle_command);
		
		check_command = new Command("checkdnsbl", 10, Permissions.EVERYONE) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				InetAddress address = null;
				try {
					address = InetAddress.getByName(params);
				}catch (Exception e){ }
				System.out.println(address.toString().split("/")[1]);
                String[] parts = address.toString().split("/")[1].split("\\.");
                String reversedAddress = parts[3] + "." + parts[2] + "." + parts[1] + "." + parts[0];
                Attribute attribute;
                Attributes attributes;
                String foundOn = "";
                for (String service : dnsbls){
                	System.out.println("Trying " + service);
                    try{
                        attributes = ictx.getAttributes(reversedAddress + "." + service, new String[]{"A"});
                        attribute = attributes.get("A");
                        if (attribute != null){
                        	foundOn += service + ", ";
                        }
                    }catch (Exception e){ }
                }
                if (foundOn.length() > 1) {
                    Helper.sendMessage(target, params + " found on " + foundOn.replaceAll(", $", ""));
                }
			}
		}; check_command.setHelpText("DNSBL check on demand");
		IRCBot.registerCommand(check_command);
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
		toggle_command.tryExecute(command, nick, target, event, copyOfRange);
		check_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}


