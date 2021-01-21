package pcl.lc.irc.hooks;

import com.google.common.collect.Lists;
import org.pircbotx.Colors;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * @author Caitlyn
 */
@SuppressWarnings("rawtypes")
public class Flip extends AbstractListener {
  private Command local_command;
  private static final String
    flipOriginal = "!().12345679<>?ABCDEFGJKLMPQRTUVWY[]_abcdefghijklmnpqrtuvwy{},'\"┳",
    flipReplace = "¡)(˙⇂ⵒƐㄣϛ9Ɫ6><¿∀ℇƆᗡƎℲפſ丬˥WԀΌᴚ⊥∩ΛMλ][‾ɐqɔpǝɟɓɥıɾʞlɯudbɹʇnʌʍʎ}{',„┻";

  private static String mutate(String original, String replacement, CharSequence str) {
    char[] chars = new char[str.length()];
    for (int i = 0; i < chars.length; ++i) {
      char source = str.charAt(i);
      int iof1 = original.indexOf(source);
      int iof2 = replacement.indexOf(source);
      if (iof1 == -1 && iof2 == -1) {
        chars[i] = source;
        continue;
      }
      if (iof1 != -1)
        chars[i] = replacement.charAt(iof1);
      else if (iof2 != -1)
        chars[i] = original.charAt(iof2);
    }
    return new String(chars);
  }

  public static String flip(CharSequence str) {
    return mutate(flipOriginal, flipReplace, str);
  }

  @Override
  protected void initHook() {
    local_command = new Command("flip", new CommandArgumentParser(0, new CommandArgument(ArgumentTypes.STRING))) {
      @Override
      public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
        String flip = this.argumentParser.getArgument(0);
        if (flip == null || flip.equals("")) {
          Helper.sendMessage(target, "(╯°□°）╯┻━┻", nick);
        } else {
          Helper.sendMessage(target, "(╯°□°）╯" + new StringBuffer(Colors.removeFormattingAndColors(flip(flip))).reverse().toString(), nick);
        }
      }
    }; local_command.setHelpText("Flips the text sent.");
    IRCBot.registerCommand(local_command);
  }
}
