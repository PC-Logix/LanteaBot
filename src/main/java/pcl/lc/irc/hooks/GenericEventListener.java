/**
 * 
 */
package pcl.lc.irc.hooks;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.pircbotx.hooks.events.*;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.InviteEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.PingEvent;
import org.pircbotx.hooks.events.ServerResponseEvent;
import org.pircbotx.hooks.types.GenericCTCPEvent;

import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings({ "rawtypes" })
public class GenericEventListener extends ListenerAdapter {

	public GenericEventListener() {
		System.out.println("onConnect listener loaded");
		System.out.println("onNickChange listener loaded");
		System.out.println("onInvite listener loaded");
		System.out.println("onGenericCTCP listener loaded");
		System.out.println("onPing listener loaded");
	}

	
	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		List<String> list = new ArrayList<String>();
		list.add(event.getChannel().getName().toString());
		list.add(event.getUser().getNick().toString());
		list.add(event.getMessage());
		IRCBot.messages.put(UUID.randomUUID(), list);
	}
	
	
	@Override
	public void onConnect(final ConnectEvent event) {
		if (!IRCBot.nspass.isEmpty())
			event.respond("ns identify "+ IRCBot.nsaccount + " " + IRCBot.nspass);
		IRCBot.ournick = event.getBot().getNick();
	}

	@Override
	public void onNickChange(final NickChangeEvent event) {
		if (event.getOldNick().equals(IRCBot.ournick)) {
			IRCBot.ournick = event.getNewNick();
		} else {
			String server = IRCBot.users.get(event.getOldNick());
			IRCBot.users.remove(event.getOldNick());
			IRCBot.users.put(event.getNewNick(), server);
			IRCBot.authed.remove(event.getOldNick());
			IRCBot.bot.sendRaw().rawLineNow("who " + event.getNewNick() + " %an");
		}
	}

	@Override
	public void onJoin(final JoinEvent event) {
		if (!event.getUser().getNick().equals(IRCBot.ournick)) {
			IRCBot.bot.sendRaw().rawLineNow("who " + event.getUser().getNick() + " %an");
			if (!event.getUser().getNick().equals(IRCBot.ournick) && !event.getUser().getServer().isEmpty()) {
				IRCBot.users.put(event.getUser().getNick(), event.getUser().getServer());
			}
			if(IRCBot.authed.containsKey(event.getUser().getNick())) {
				IRCBot.authed.remove(event.getUser().getNick());
			}
		} else {
			IRCBot.bot.sendRaw().rawLineNow("who " + event.getChannel().getName() + " %an");
		}
	}

	@Override
	public void onPart(final PartEvent event) {

	}

	@Override
	public void onQuit(final QuitEvent event) {
		if(event.getReason().equals("*.net *.split")) {
			IRCBot.authed.remove(event.getUser().getNick());
			IRCBot.users.remove(event.getUser().getNick());
		}
		if(IRCBot.authed.containsKey(event.getUser().getNick())) {
			IRCBot.authed.remove(event.getUser().getNick());
			IRCBot.users.remove(event.getUser().getNick());
		}
	}

	@Override
	public void onKick(final KickEvent event) {

	}

	@Override
	public void onInvite(InviteEvent event) {
		if (IRCBot.invites.containsKey(event.getChannel())) {
			event.getBot().sendIRC().joinChannel(event.getChannel());
			IRCBot.invites.remove(event.getChannel());
		}
	}

	@Override
	public void onGenericCTCP(final GenericCTCPEvent event) {

	}

	@Override
	public void onPing(final PingEvent event) {
		//event.respond(event.getPingValue());
	}

	@Override
	public void onServerResponse(final ServerResponseEvent event) {
		System.out.println(event.getCode());
		if(event.getCode() == 352) {
			System.out.println(event.getParsedResponse());
			Object nick = event.getParsedResponse().toArray()[5];
			Object server = event.getParsedResponse().toArray()[4];
			if (IRCBot.users.containsKey(nick)) {
				IRCBot.users.remove(nick);
			}
			IRCBot.users.put(nick.toString(), server.toString());
		}
		if(event.getCode() == 354) {
			Object nick = event.getParsedResponse().toArray()[1];
			Object nsaccount = event.getParsedResponse().toArray()[2];
			if (IRCBot.authed.containsKey(nick)) {
				IRCBot.authed.remove(nick);
			}if (!nsaccount.toString().equals("0")) {
				IRCBot.authed.put(nick.toString(),nsaccount.toString());
			}
		}
		if(event.getCode() == 330) {
			Object nick = event.getParsedResponse().toArray()[1];
			Object nsaccount = event.getParsedResponse().toArray()[2];
			if (IRCBot.authed.containsKey(nick)) {
				IRCBot.authed.remove(nick);
			}
			IRCBot.authed.put(nick.toString(),nsaccount.toString());
		}
		if(event.getCode() == 372) {
			
		}
	}
	
	@Override
	public void onUnknown(final UnknownEvent event) {
		System.out.println("UnknownEvent: "+ event.getLine());
		if(event.getLine().contains("ACCOUNT")) {
			String nick = event.getLine().substring(event.getLine().indexOf(":") + 1, event.getLine().indexOf("!"));
			if(event.getLine().split("\\s")[2].equals("*")) {
				IRCBot.authed.remove(nick);
				System.out.println(nick + " Logged out");
			} else {
				IRCBot.authed.put(nick, event.getLine().split("\\s")[2].toString());
				System.out.println(nick + " Logged in");
			}
		}
	}
}
