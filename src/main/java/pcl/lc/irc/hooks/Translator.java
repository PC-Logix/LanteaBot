package pcl.lc.irc.hooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.Colors;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import com.google.common.collect.Lists;

import io.github.firemaples.language.Language;
import io.github.firemaples.translate.Translate;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

public class Translator extends AbstractListener {

	private Command local_command;
	public HashMap<String, Language> langMap;
	@Override
	protected void initHook() {
		langMap = new HashMap<String, Language>();
		langMap.put("english", Language.ENGLISH);
		langMap.put("en", Language.ENGLISH);
		
		langMap.put("russian", Language.RUSSIAN);
		langMap.put("ru", Language.RUSSIAN);
		
		langMap.put("french", Language.FRENCH);
		langMap.put("fr", Language.FRENCH);
		
		langMap.put("japanese", Language.JAPANESE);
		langMap.put("ja", Language.JAPANESE);
		
		langMap.put("spanish", Language.SPANISH);
		langMap.put("es", Language.SPANISH);
		
		langMap.put("hungarian", Language.HUNGARIAN);
		langMap.put("hu", Language.HUNGARIAN);
		
		langMap.put("dutch", Language.DUTCH);
		langMap.put("nl", Language.DUTCH);
		
		langMap.put("greek", Language.GREEK);
		langMap.put("el", Language.GREEK);
		
		langMap.put("norwegian", Language.NORWEGIAN);
		langMap.put("no", Language.NORWEGIAN);
		
		langMap.put("german", Language.GERMAN);
		langMap.put("de", Language.GERMAN);

		initCommands();
		String supportedLangs = "";
	    Iterator<?> it = langMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        supportedLangs += pair.getKey() + ", ";
	    }
		IRCBot.registerCommand(local_command, "Translate! supported languages are " + supportedLangs.replaceAll(", $", ""));
		if (Config.botConfig.containsKey("AzureTextAPI")) {
			Translate.setSubscriptionKey(Config.AzureTextAPI);
		}
	}

	private void initCommands() {
		local_command = new Command("translate", 5) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (params.equals("^")) {
					List<Entry<UUID, List<String>>> list = new ArrayList<>(IRCBot.messages.entrySet());
					for (Entry<UUID, List<String>> entry : Lists.reverse(list)) {
						if (entry.getValue().get(0).equals(target)) {
							Helper.sendMessage(target, doTranslate("auto", "auto", entry.getValue().get(2).toString()));
							return;
						}
					}
				} else if (params.split(" ").length > 2) {
					Helper.sendMessage(target, doTranslate(params.split(" ")[0], params.split(" ")[1], params));
				} else {
					Helper.sendMessage(target, doTranslate("auto", "auto", params));
				}
			}
		};
		local_command.registerAlias("t");
	}

	public String chan;
	public String target = null;

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args, String callingRelay) {
		if (local_command.shouldExecute(command, event) >= 0) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange, String callingRelay) {
		target = Helper.getTarget(event);
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}

	public String doTranslate(String from, String to, String text) {
		String output = "";
		if (Config.botConfig.containsKey("AzureTextAPI")) {
			try {
				Language fromLang;
				Language toLang;
				if (langMap.containsKey(from.toLowerCase())) {
					fromLang = langMap.get(from.toLowerCase());
					text = StringUtils.substringAfter(text, " ");
				} else {
					fromLang = Language.AUTO_DETECT;
				}
				if (langMap.containsKey(to.toLowerCase())) {
					toLang = langMap.get(to.toLowerCase());
					text = StringUtils.substringAfter(text, " ");
				} else {
					toLang = Language.ENGLISH;
				}
				output = Translate.execute(text, fromLang, toLang);
			} catch (Exception e) {
				output = e.getLocalizedMessage();
				e.printStackTrace();
			}
			return output;
		} else {
			return "No Azure text translation API key specified in config";
		}
	}
}
