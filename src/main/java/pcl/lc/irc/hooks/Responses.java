package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

import java.util.ArrayList;

/**
 * Solicit responses from bot
 * Created by Forecaster on 2017-03-28.
 */
public class Responses extends AbstractListener {
  private static long lastTonk;
  @Override
  protected void initHook() {
     String tonk = Database.getJsonData("lasttonk");
     if (tonk != "")
       lastTonk = Long.parseLong(tonk);
     else
       lastTonk = 0;
  }

  @Override
  public void handleCommand(String sender, MessageEvent event, String command, String[] args) {}

  @Override
  public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {}

  public String chan;
  public String target = null;
  @Override
  public void handleMessage(String sender, MessageEvent event, String[] args) {
    chan = event.getChannel().getName();
  }

  @Override
  public void handleMessage(String nick, GenericMessageEvent event, String[] copyOfRange) {
	if (IRCBot.isIgnored(nick))
		return;
	target = Helper.getTarget(event);

    if (event.getMessage().toLowerCase().contains(IRCBot.getOurNick().toLowerCase())) {
      ArrayList<String[]> respondTo = new ArrayList<>();
      respondTo.add(new String[]{"tonk", "tonk"});

      respondTo.add(new String[]{"thanks", "welcome"});
      respondTo.add(new String[]{"thank you", "welcome"});

      respondTo.add(new String[]{"hi", "hello"});
      respondTo.add(new String[]{"hello", "hello"});
      respondTo.add(new String[]{"hai", "hello"});
      respondTo.add(new String[]{"good morning", "hello"});
      respondTo.add(new String[]{"good afternoon", "hello"});

      respondTo.add(new String[]{"seriously", "surprise"});
      respondTo.add(new String[]{"srsly", "surprise"});
      respondTo.add(new String[]{"how dare you", "surprise"});
      respondTo.add(new String[]{"howdareyou", "surprise"});
      respondTo.add(new String[]{"no u", "surprise"});
      respondTo.add(new String[]{"no you", "surprise"});
      respondTo.add(new String[]{"really", "surprise"});

      respondTo.add(new String[]{"you're welcome", "smile"});
      respondTo.add(new String[]{"youre welcome", "smile"});
      respondTo.add(new String[]{"no problem", "smile"});

      respondTo.add(new String[]{"good", "thanks"});
      respondTo.add(new String[]{"excellent", "thanks"});
      respondTo.add(new String[]{"nice", "thanks"});

      respondTo.add(new String[]{"poor", "angry"});

      respondTo.add(new String[]{"you're cute", "iknow"});

      respondTo.add(new String[]{"there there", "cry"});
      respondTo.add(new String[]{"mean", "cry"});

      respondTo.add(new String[]{"naughty bits", "blush"});
      respondTo.add(new String[]{"lewd bits", "blush"});

      respondTo.add(new String[]{"boops", "squeak"});
      respondTo.add(new String[]{"pokes", "squeak"});
      
      respondTo.add(new String[]{"defragments", "tickles"});

      respondTo.add(new String[]{"care", "whocares"});

      respondTo.add(new String[]{"that won't work", "face"});

      respondTo.add(new String[]{"right", "right"});

      respondTo.add(new String[]{"pets", "pet"});

      respondTo.add(new String[]{"do the flop", "flop"});

      respondTo.add(new String[]{"baps", "hurt"});
      respondTo.add(new String[]{"slaps", "hurt"});
      respondTo.add(new String[]{"hits", "hurt"});

      String msg = event.getMessage().toLowerCase().replace(IRCBot.getOurNick().toLowerCase(), "");
      for (String[] str : respondTo) {
        if (msg.contains(str[0])) {
          respond(str[1], nick);
          break;
        }
      }
    }
  }

  private void respond(String type, String nick) {
    switch (type) {
      case "welcome":
        Helper.sendMessage(target,"You're welcome!", nick);
        break;
      case "surprise":
        Helper.sendMessage(target, Helper.get_surprise_response(), nick);
        break;
      case "smile":
        Helper.sendAction(target, "smiles ^.^");
        break;
      case "thanks":
        Helper.sendMessage(target, Helper.get_thanks_response(), nick);
        break;
      case "angry":
        Helper.sendMessage(target,"Don't you poor me! I'll poor you in the face! D:<", nick);
        break;
      case "iknow":
        Helper.sendMessage(target,"I know! :D", nick);
        break;
      case "cry":
        Helper.sendMessage(target," ;_;");
        break;
      case "squeak":
        Helper.sendAction(target, "squeaks!");
        break;
      case "blush":
        Helper.sendMessage(target, "o///o");
        break;
      case "tickles":
    	  Helper.sendMessage(target, "That tickles!");
    	  break;
      case "whocares":
        Helper.sendMessage(target, Helper.get_care_response(), nick);
        break;
      case "face":
        Helper.sendMessage(target, "Your face wont work!");
        break;
      case "flop":
        Helper.sendAction(target, "does the flop");
        break;
      case "hurt":
        Helper.sendMessage(target, Helper.get_hurt_response(), nick);
      case "hello":
        Helper.sendMessage(target, "Hello " + nick);
      case "right":
        Helper.sendMessage(target, Helper.get_right_response());
      case "pet":
        Helper.sendAction(target, "purrs");
    }
  }
}
