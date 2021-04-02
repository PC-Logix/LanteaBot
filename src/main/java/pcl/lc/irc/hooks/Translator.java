package pcl.lc.irc.hooks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.types.GenericMessageEvent;

import io.github.firemaples.language.Language;
import io.github.firemaples.translate.Translate;
import pcl.lc.irc.*;
import pcl.lc.irc.entryClasses.*;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.CommandChainStateObject;
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
		local_command.setHelpText("Translate! supported languages are " + supportedLangs.replaceAll(", $", ""));
		IRCBot.registerCommand(local_command);
		if (Config.botConfig.containsKey("AzureTextAPI")) {
			Translate.setSubscriptionKey(Config.AzureTextAPI);
		}
	}

	private void initCommands() {
		local_command = new Command("translate", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Text"), new CommandArgument(ArgumentTypes.STRING, "FromLanguage", "If this argument is omitted the API will try to determine it automatically."), new CommandArgument(ArgumentTypes.STRING, "ToLanguage", "If this argument is omitted the API will try to determine it automatically.")), new CommandRateLimit(5)) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String str = this.argumentParser.getArgument("Text");
				String from = this.argumentParser.getArgument("FromLanguage");
				String to = this.argumentParser.getArgument("ToLanguage");
				if (to == null || to.equals(""))
					to = "auto";
				if (from == null || from.equals(""))
					from = "auto";
				Helper.sendMessage(target, doTranslate(from, to, str));
				return new CommandChainStateObject(CommandChainState.FINISHED);
			}
		};
		local_command.registerAlias("t");
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
