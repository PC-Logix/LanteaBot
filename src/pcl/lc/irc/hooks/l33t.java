/**
 * 
 */
package pcl.lc.irc.hooks;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class l33t extends ListenerAdapter {
	public l33t() {
		IRCBot.registerCommand("1337");
	}

	public static String toLeet(String str){
		boolean ck = false;
		boolean s = false;
		if(str.endsWith("ck")){
			ck = true;
			str = str.substring(0, str.length() - 2);
		} else if(str.endsWith("s")){
			s = true;
			str = str.substring(0, str.length() - 1);
		}
		char[] arr = str.toCharArray();

		for(int i=0; i < str.length(); ++i){
			switch(arr[i]){
			case 'a':	arr[i]='@'; break;
			case 'e':	arr[i]='3'; break;
			case 'i':	arr[i]='1'; break;
			case 'o':	arr[i]='0'; break;
			case 'u':	arr[i]='v'; break;
			case 'f':	arr[i]='p'; break;
			case 's':	arr[i]='$'; break;
			case 'g':	arr[i]='9'; break;
			case 'y':	arr[i]='j'; break;
			case 't':	arr[i]='+'; break;
			case '!':	arr[i]='1'; break;
			}
			++i;
			if(Character.isLowerCase(arr[i-1])){
				arr[i-1] = Character.toUpperCase(arr[i-1]);
			} else /*if(Character.isUpperCase(arr[i]))*/ {
				arr[i-1] = Character.toLowerCase(arr[i-1]);
			}
		}

		String result = new String(arr);
		if(ck){
			result = result.concat("x");
		} else if(s) {
			result = result.concat("z");
		}

		return result;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		String prefix = IRCBot.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		if (trigger.length() > 1) {
			String[] firstWord = StringUtils.split(trigger);
			String triggerWord = firstWord[0];
			if (triggerWord.equals(IRCBot.commandprefix + "1337")) {
				String s = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
				event.respond(toLeet(s));
			}
		}
	}
}
