/**
 *
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.*;
import pcl.lc.utils.Helper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;

/**
 * @author Caitlyn
 */
@SuppressWarnings("rawtypes")
public class DynamicCommands extends AbstractListener {
  private Command local_command_add;
  private Command local_command_del;

  @Override
  protected void initHook() {
    local_command_add = new Command("addcommand", 0);
    IRCBot.registerCommand(local_command_add, "Adds a dynamic command to the bot, requires BotAdmin, or Channel Op.");
    local_command_del = new Command("delcommand", 0);
    IRCBot.registerCommand(local_command_del, "Removes a dynamic command to the bot, requires BotAdmin, or Channel Op.");
    Database.addStatement("CREATE TABLE IF NOT EXISTS Commands(command STRING UNIQUE PRIMARY KEY, return)");
    Database.addPreparedStatement("addCommand", "INSERT INTO Commands(command, return) VALUES (?, ?);");
    Database.addPreparedStatement("searchCommands", "SELECT command FROM Commands");
    Database.addPreparedStatement("getCommand", "SELECT return FROM Commands WHERE command = ?");
    Database.addPreparedStatement("delCommand", "DELETE FROM Commands WHERE command = ?;");
    try {
      PreparedStatement searchCommands = Database.getPreparedStatement("searchCommands");
      ResultSet commands = searchCommands.executeQuery();
      while (commands.next()) {
        IRCBot.registerCommand(commands.getString(1), "Dynamic commands module, who knows what it does?!");
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      IRCBot.log.info("An error occurred while processing this command");
    }
  }

  @Override
  public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
    String prefix = Config.commandprefix;
    String ourinput = event.getMessage().toLowerCase().trim();
    if (ourinput.length() > 1) {
      if (!IRCBot.isIgnored(event.getUser().getNick())) {
        String[] message = event.getMessage().split(" ", 3);

        long shouldExecute = local_command_add.shouldExecute(command);
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
          shouldExecute = local_command_del.shouldExecute(command);
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
          else {
            try {
              PreparedStatement getCommand = Database.getPreparedStatement("getCommand");
              getCommand.setString(1, command.replace(prefix, "").toLowerCase());
              ResultSet command1 = getCommand.executeQuery();
              if (command1.next()) {
                String msg = MessageFormat.format(command1.getString(1), args);
                event.getBot().sendIRC().message(event.getChannel().getName(), msg);
              }
            }
            catch (Exception e) {
              e.printStackTrace();
              System.out.println("An error occurred while processing this command");
            }
          }
        }
      }
    }
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