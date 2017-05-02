package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

import java.util.ArrayList;
import java.util.Date;

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
  public void handleMessage(String sender, MessageEvent event, String command, String[] args) {
    chan = event.getChannel().getName();
  }

  @Override
  public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
	if (IRCBot.isIgnored(nick))
		return;
	target = Helper.getTarget(event);

    if (event.getMessage().toLowerCase().contains(IRCBot.getOurNick().toLowerCase())) {
      ArrayList<String[]> respondTo = new ArrayList<>();
      respondTo.add(new String[]{"tonk", "tonk"});

      respondTo.add(new String[]{"thanks", "welcome"});
      respondTo.add(new String[]{"thank you", "welcome"});

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


      for (String[] str : respondTo) {
        if (event.getMessage().toLowerCase().contains(str[0])) {
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
      case "tonk":
        long time = new Date().getTime();
        if (lastTonk != 0)
          Helper.sendMessage(target, "The last Tonk was " + Helper.timeString(Helper.parseMilliseconds(time - lastTonk), true) + " ago! Your Tonk has been noted.", nick);
        else
          Helper.sendMessage(target, "There hadn't been any Tonks yet. I've noted your Tonk.", nick);
        lastTonk = time;
        Database.storeJsonData("lasttonk", Long.toString(time));
        break;
      case "squeak":
        Helper.sendAction(target, "squeaks!");
        break;
      case "blush":
        Helper.sendMessage(target, "o///o");
        break;
      case "tickles":
    	  Helper.sendMessage(target, "That tickles!");
    }
  }
}
