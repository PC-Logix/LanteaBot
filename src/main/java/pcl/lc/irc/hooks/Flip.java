package pcl.lc.irc.hooks;

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
	    REPLACEMENT['a'] =  "\u0250";
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
	    
	    //reverse the flips
	    REPLACEMENT['\u0250'] =  "a";
	    REPLACEMENT['q'] =  "b";
	    REPLACEMENT['ɔ'] =  "c";
	    REPLACEMENT['p'] =  "d";
	    REPLACEMENT['ǝ'] =  "ǝ";
	    REPLACEMENT['ɟ'] =  "f";
	    REPLACEMENT['ƃ'] =  "g";
	    REPLACEMENT['ɥ'] =  "h";
	    REPLACEMENT['!'] =  "!";
	    REPLACEMENT['ṛ'] =  "j";
	    REPLACEMENT['ʞ'] =  "k";
	    REPLACEMENT['ʃ'] =  "l";
	    REPLACEMENT['ɯ'] =  "m";
	    REPLACEMENT['u'] =  "n";
	    REPLACEMENT['o'] =  "o";
	    REPLACEMENT['d'] =  "p";
	    REPLACEMENT['b'] =  "q";
	    REPLACEMENT['ɹ'] =  "r";
	    REPLACEMENT['s'] =  "s";
	    REPLACEMENT['ʇ'] =  "t";
	    REPLACEMENT['n'] =  "u";
	    REPLACEMENT['ʌ'] =  "v";
	    REPLACEMENT['ʍ'] =  "w";
	    REPLACEMENT['x'] =  "x";
	    REPLACEMENT['ʎ'] =  "y";
	    REPLACEMENT['z'] =  "z";
	    

	    REPLACEMENT['∀'] =  "A";
	    REPLACEMENT['ჵ'] =  "B";
	    REPLACEMENT['Ↄ'] =  "C";
	    REPLACEMENT['◖'] =  "D";
	    REPLACEMENT['Ǝ'] =  "E";
	    REPLACEMENT['Ⅎ'] =  "F";
	    REPLACEMENT['⅁'] =  "G";
	    REPLACEMENT['H'] =  "H";
	    REPLACEMENT['I'] =  "I";
	    REPLACEMENT['J'] =  "J";
	    REPLACEMENT['ʞ'] =  "K";
	    REPLACEMENT['L'] =  "L";
	    REPLACEMENT['W'] =  "M";
	    REPLACEMENT['И'] =  "N";
	    REPLACEMENT['O'] =  "O";
	    REPLACEMENT['Ԁ'] =  "P";
	    REPLACEMENT['Ό'] =  "Q";
	    REPLACEMENT['ᴚ'] =  "R";
	    REPLACEMENT['S'] =  "S";
	    REPLACEMENT['⊥'] =  "T";
	    REPLACEMENT['n'] =  "U";
	    REPLACEMENT['ᴧ'] =  "V";
	    REPLACEMENT['M'] =  "W";
	    REPLACEMENT['X'] =  "X";
	    REPLACEMENT['Y'] =  "Y";
	    REPLACEMENT['Z'] =  "Z";
	    
	    
	    REPLACEMENT['˙'] =  ".";
	    REPLACEMENT['‾'] =  "_";
	    REPLACEMENT['¡'] =  "!";
	    REPLACEMENT['؛'] =  ";";
	    REPLACEMENT['¿'] =  "?";
	    REPLACEMENT['\''] =  ",";
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