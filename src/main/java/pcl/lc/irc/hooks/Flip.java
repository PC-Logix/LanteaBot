package pcl.lc.irc.hooks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import com.google.common.collect.Lists;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Flip extends ListenerAdapter {
	public Flip() {
		IRCBot.registerCommand("flip");
	}

	private static final String
	flipOriginal =	"!().12345679<>?ABCDEFGJKLMPQRTUVWY[]_abcdefghijklmnpqrtuvwy{},'\"┳",
	flipReplace =	"¡)(˙⇂ⵒƐㄣϛ9Ɫ6><¿∀ℇƆᗡƎℲפſ丬˥WԀΌᴚ⊥∩ΛMλ][‾ɐqɔpǝɟɓɥıɾʞlɯudbɹʇnʌʍʎ}{',„┻";
	
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

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);

		String prefix = Config.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		if (trigger.length() > 1) {
			String[] firstWord = StringUtils.split(trigger);
			String triggerWord = firstWord[0];
			if (triggerWord.equals(prefix + "flip")) {
				if (!IRCBot.isIgnored(event.getUser().getNick())) {
					String s = event.getMessage().substring(event.getMessage().indexOf("flip") + 4).trim();
					if (s.equals("^")) {
						List<Entry<UUID, List<String>>> list = new ArrayList<>(IRCBot.messages.entrySet());
						for(Entry<UUID, List<String>> entry : Lists.reverse(list)){	
							if (entry.getValue().get(0).equals(event.getChannel().getName().toString())) {
								event.respond("(╯°□°）╯︵" + new StringBuffer(Colors.removeFormattingAndColors(flip(entry.getValue().get(2)))).reverse().toString());
								return;
							}
						}
					} else {
						event.respond("(╯°□°）╯︵" + new StringBuffer(Colors.removeFormattingAndColors(flip(s))).reverse().toString());								
					}
				}
			}			
		}
	}
}