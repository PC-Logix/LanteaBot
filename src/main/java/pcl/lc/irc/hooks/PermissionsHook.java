package pcl.lc.irc.hooks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.utils.Account;
import pcl.lc.utils.Helper;

public class PermissionsHook extends AbstractListener {

	@Override
	protected void initCommands() {
		IRCBot.registerCommand("addperm", "Adds a permission level to the given user for the current channel");
		IRCBot.registerCommand("delperm", "Removes the permission level from a user for the current channel");
		IRCBot.registerCommand("listperms", "");
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		String prefix = Config.commandprefix;
		boolean isOp = Account.isOp(event.getBot(), event.getUser());
		boolean chanOp = Helper.isChannelOp(event);
		if (command.equals(prefix + "addperm")) {
			if (isOp || chanOp) {
				if (event.getChannel().getUsers().contains(Account.getUserFromString(args[0], event))) {
					Boolean addPerm = Permissions.setPermLevel(args[0], event, Integer.parseInt(args[1]));
					event.respond(addPerm.toString());
				} else {
					event.respond("User is not in this channel");
				}
			}
		} else if (command.equals(prefix + "listperms")) {
			if (isOp || chanOp) {
				try {
					PreparedStatement getAnyQuote = IRCBot.getInstance().getPreparedStatement("getAllUserPerms");
					ResultSet results = getAnyQuote.executeQuery();
					if (results.next()) {
						IRCBot.bot.sendIRC().message(event.getChannel().getName(), results.getString(1) +" "+ results.getInt(3));
					}
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		// TODO Auto-generated method stub

	}

}
