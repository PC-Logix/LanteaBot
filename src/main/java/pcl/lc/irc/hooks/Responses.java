package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import java.util.ArrayList;
import java.util.Date;

/**
 * Solicit responses from bot
 * Created by Forecaster on 2017-03-28.
 */
public class Responses extends AbstractListener {
  private static long lastTonk = 0;
  @Override
  protected void initHook() {}

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
    if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
      target = nick;
    } else {
      target = chan;
    }

    if (event.getMessage().toLowerCase().contains(IRCBot.getOurNick().toLowerCase())) {
      ArrayList<String[]> respondTo = new ArrayList<>();
      respondTo.add(new String[]{"thanks", "1"});
      respondTo.add(new String[]{"thank you", "1"});

      respondTo.add(new String[]{"seriously", "2"});
      respondTo.add(new String[]{"srsly", "2"});
      respondTo.add(new String[]{"how dare you", "2"});
      respondTo.add(new String[]{"howdareyou", "2"});
      respondTo.add(new String[]{"no u", "2"});
      respondTo.add(new String[]{"no you", "2"});
      respondTo.add(new String[]{"really", "2"});

      respondTo.add(new String[]{"you're welcome", "3"});
      respondTo.add(new String[]{"youre welcome", "3"});

      respondTo.add(new String[]{"good", "4"});
      respondTo.add(new String[]{"excellent", "4"});
      respondTo.add(new String[]{"nice", "4"});

      respondTo.add(new String[]{"poor", "5"});

      respondTo.add(new String[]{"you're cute", "6"});

      respondTo.add(new String[]{"there there", "7"});
      respondTo.add(new String[]{"mean", "7"});

      respondTo.add(new String[]{"tonk", "8"});

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
      case "1":
        Helper.sendMessage(target,"You're welcome!", nick);
        break;
      case "2":
        Helper.sendMessage(target, Helper.get_surprise_response(), nick);
        break;
      case "3":
        Helper.sendAction(target, "smiles ^.^");
        break;
      case "4":
        Helper.sendMessage(target, Helper.get_thanks_response(), nick);
        break;
      case "5":
        Helper.sendMessage(target,"Don't you poor me! I'll poor you in the face! D:<", nick);
        break;
      case "6":
        Helper.sendMessage(target,"I know! :D", nick);
        break;
      case "7":
        Helper.sendMessage(target,";_;");
        break;
      case "8":
        long time = new Date().getTime();
        if (lastTonk != 0)
          Helper.sendMessage(target, "The last Tonk was " + Helper.timeString(Helper.parseMilliseconds(time - lastTonk)) + " ago!", nick);
        else
          Helper.sendMessage(target, "There haven't been any Tonks yet.", nick);
        lastTonk = time;
        break;
    }
  }
}
