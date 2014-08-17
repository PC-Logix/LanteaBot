package pcl.lc.irc.hooks;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

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

	private static final String[] REPLACEMENT = new String[Character.MAX_VALUE+1];
	static {
	    for(int i=Character.MIN_VALUE;i<=Character.MAX_VALUE;i++)
	        REPLACEMENT[i] = Character.toString(Character.toLowerCase((char) i));
	    // substitute
	    REPLACEMENT['a'] =  "ɐ";
	    REPLACEMENT['b'] =  "q";
	    REPLACEMENT['c'] =  "ɔ";
	    REPLACEMENT['d'] =  "p";
	    REPLACEMENT['e'] =  "ǝ";
	    REPLACEMENT['f'] =  "ɟ";
	    REPLACEMENT['g'] =  "ƃ";
	    REPLACEMENT['h'] =  "ɥ";
	    REPLACEMENT['i'] =  "!";
	    REPLACEMENT['j'] =  "ṛ";
	    REPLACEMENT['k'] =  "ʞ";
	    REPLACEMENT['l'] =  "ʃ";
	    REPLACEMENT['m'] =  "ɯ";
	    REPLACEMENT['n'] =  "u";
	    REPLACEMENT['o'] =  "o";
	    REPLACEMENT['p'] =  "d";
	    REPLACEMENT['q'] =  "b";
	    REPLACEMENT['r'] =  "ɹ";
	    REPLACEMENT['s'] =  "s";
	    REPLACEMENT['t'] =  "ʇ";
	    REPLACEMENT['u'] =  "n";
	    REPLACEMENT['v'] =  "ʌ";
	    REPLACEMENT['w'] =  "ʍ";
	    REPLACEMENT['x'] =  "x";
	    REPLACEMENT['y'] =  "ʎ";
	    REPLACEMENT['z'] =  "z";
	    

	    REPLACEMENT['A'] =  "∀";
	    REPLACEMENT['B'] =  "ჵ";
	    REPLACEMENT['C'] =  "Ↄ";
	    REPLACEMENT['D'] =  "◖";
	    REPLACEMENT['E'] =  "Ǝ";
	    REPLACEMENT['F'] =  "Ⅎ";
	    REPLACEMENT['G'] =  "⅁";
	    REPLACEMENT['H'] =  "H";
	    REPLACEMENT['I'] =  "I";
	    REPLACEMENT['J'] =  "J";
	    REPLACEMENT['K'] =  "ʞ";
	    REPLACEMENT['L'] =  "L";
	    REPLACEMENT['M'] =  "W";
	    REPLACEMENT['N'] =  "И";
	    REPLACEMENT['O'] =  "O";
	    REPLACEMENT['P'] =  "Ԁ";
	    REPLACEMENT['Q'] =  "Ό";
	    REPLACEMENT['R'] =  "ᴚ";
	    REPLACEMENT['S'] =  "S";
	    REPLACEMENT['T'] =  "⊥";
	    REPLACEMENT['U'] =  "n";
	    REPLACEMENT['V'] =  "ᴧ";
	    REPLACEMENT['W'] =  "M";
	    REPLACEMENT['X'] =  "X";
	    REPLACEMENT['Y'] =  "Y";
	    REPLACEMENT['Z'] =  "Z";
	    
	    
	    REPLACEMENT['.'] =  "˙";
	    REPLACEMENT['_'] =  "‾";
	    REPLACEMENT['!'] =  "¡";
	    REPLACEMENT[';'] =  "؛";
	    REPLACEMENT['?'] =  "¿";
	    REPLACEMENT[','] =  "'";
	    
	    
	    REPLACEMENT['('] =  ")";
	    REPLACEMENT[')'] =  "(";
	    REPLACEMENT['['] =  "]";
	    REPLACEMENT[']'] =  "[";
	    REPLACEMENT['{'] =  "}";
	    REPLACEMENT['}'] =  "{";
	    REPLACEMENT['<'] =  "<";
	    REPLACEMENT['>'] =  ">";    
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
			if (triggerWord.equals(prefix + "flip")) {
				String s = event.getMessage().substring(event.getMessage().indexOf("flip") + 4).trim();
			    StringBuilder sb = new StringBuilder(s.length());
			    for(int i=0;i<s.length();i++)
			        sb.append(REPLACEMENT[s.charAt(i)]);
			    
				event.respond("(╯°□°）╯︵" + new StringBuffer(Colors.removeFormattingAndColors(sb.toString())).reverse().toString());
			}
		}
	}
}