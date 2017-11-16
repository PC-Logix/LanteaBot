/**
 *
 */
package pcl.lc.irc.hooks;

import org.pircbotx.Colors;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.*;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;
import pcl.lc.utils.Item;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;

/**
 * @author Caitlyn
 */
@SuppressWarnings("rawtypes")
public class DynamicCommands extends AbstractListener {
  private Command local_command_add;
  private Command local_command_del;
  private Command local_command_addhelp;

  @Override
  protected void initHook() {
    local_command_add = new Command("addcommand", 0);
    IRCBot.registerCommand(local_command_add, "Adds a dynamic command to the bot, requires BotAdmin, or Channel Op.");
    local_command_del = new Command("delcommand", 0);
    local_command_addhelp = new Command ("addcommandhelp", 0) {
		@Override
		public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
			PreparedStatement addCommandHelp;
			try {
				addCommandHelp = Database.getPreparedStatement("addCommandHelp");
				String arr[] = params.split(" ", 2);
				String theCommand = arr[0];
				String theHelp = arr[1]; 
				if (IRCBot.commands.containsKey(theCommand)) {
					try {
						addCommandHelp.setString(1, theHelp);
						addCommandHelp.setString(2, theCommand.toLowerCase());
						addCommandHelp.executeUpdate();
						IRCBot.setHelp(theCommand, theHelp);
						event.respond("Help Set");
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						event.respond("fail 1");
					}
				} else {
					event.respond("fail 2 ");
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				event.respond("fail 3");
			}
		}
    };
    local_command_addhelp.setHelpText("Sets help on dynamic commands");
    IRCBot.registerCommand(local_command_del, "Removes a dynamic command to the bot, requires BotAdmin, or Channel Op.");
    IRCBot.registerCommand(local_command_addhelp);
    Database.addStatement("CREATE TABLE IF NOT EXISTS Commands(command STRING UNIQUE PRIMARY KEY, return, help)");
    Database.addUpdateQuery(5, "ALTER TABLE Commands ADD help STRING DEFAULT NULL NULL;");
    Database.addPreparedStatement("addCommand", "INSERT INTO Commands(command, return) VALUES (?, ?);");
    Database.addPreparedStatement("addCommandHelp", "UPDATE Commands SET help = ? WHERE command = ?");
    Database.addPreparedStatement("searchCommands", "SELECT command, help FROM Commands");
    Database.addPreparedStatement("getCommand", "SELECT return FROM Commands WHERE command = ?");
    Database.addPreparedStatement("delCommand", "DELETE FROM Commands WHERE command = ?;");
    try {
      PreparedStatement searchCommands = Database.getPreparedStatement("searchCommands");
      ResultSet commands = searchCommands.executeQuery();
      while (commands.next()) {
    	  if (commands.getString(2) != null) {
    	       IRCBot.registerCommand(commands.getString(1), commands.getString(2));
    	  } else {
    	       IRCBot.registerCommand(commands.getString(1), "Dynamic commands module, who knows what it does?!");
    	  }
       }
    }
    catch (Exception e) {
      e.printStackTrace();
      IRCBot.log.info("An error occurred while processing this command");
    }
  }
  
	public String chan;
	public String target = null;
  @Override
  public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
	  String prefix = Config.commandprefix;
	  target = Helper.getTarget(event);
	  try {
          PreparedStatement getCommand = Database.getPreparedStatement("getCommand");
          getCommand.setString(1, command.replace(prefix, "").toLowerCase());
          ResultSet command1 = getCommand.executeQuery();
          if (command1.next()) {
            String msg = MessageFormat.format(command1.getString(1), copyOfRange);
            if (msg.contains("[randomitem]")) {
            	msg = msg.replace("[randomitem]", Inventory.getRandomItem().getName());
            }
            if (msg.contains("[drama]")) {
            	msg = msg.replace("[drama]", Drama.dramaParse());
            }
            event.getBot().sendIRC().message(target, msg);
          }
        }
        catch (Exception e) {
          e.printStackTrace();
          System.out.println("An error occurred while processing this command");
        }
  }

  @Override
  public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
    
    String ourinput = event.getMessage().toLowerCase().trim();
    chan = event.getChannel().getName();
	target = Helper.getTarget(event);
    local_command_addhelp.tryExecute(command, sender, event.getChannel().getName(), event, args);
    if (ourinput.length() > 1) {
      if (!IRCBot.isIgnored(event.getUser().getNick())) {
        String[] message = event.getMessage().split(" ", 3);

        long shouldExecute = local_command_add.shouldExecute(command, event);
        if (shouldExecute == 0) {
          boolean isOp = Permissions.isOp(event.getBot(), event.getUser());
          if (isOp || Helper.isChannelOp(event)) {
            try {
              PreparedStatement addCommand = Database.getPreparedStatement("addCommand");
              if (!IRCBot.commands.containsKey(message[1])) {
                addCommand.setString(1, message[1].toLowerCase());
                addCommand.setString(2, message[2]);
                addCommand.executeUpdate();
                event.respond("Command Added");
                IRCBot.registerCommand(message[1].toLowerCase(), "Dynamic commands module, who knows what it does?!");
              }
              else {
                event.respond("Can't override existing commands.");
              }
            }
            catch (Exception e) {
              e.printStackTrace();
              event.respond("An error occurred while processing this command");
            }
          }
        }
        else {
          shouldExecute = local_command_del.shouldExecute(command, event);
          if (shouldExecute == 0) {
            boolean isOp = Permissions.isOp(event.getBot(), event.getUser());
            if (isOp || Helper.isChannelOp(event)) {
              try {
                PreparedStatement delCommand = Database.getPreparedStatement("delCommand");
                delCommand.setString(1, message[1].toLowerCase());
                delCommand.execute();
                event.respond("Command deleted");
                IRCBot.unregisterCommand(message[1].toLowerCase());
              }
              catch (Exception e) {
                e.printStackTrace();
                event.respond("An error occurred while processing this command");
              }
            }
          }
        }
      }
    }
  }
}