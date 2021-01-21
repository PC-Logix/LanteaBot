package pcl.lc.irc.hooks;

import com.ibm.icu.text.RuleBasedNumberFormat;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.Helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TemplateCollection {
	List<Template> templates;

	TemplateCollection() {
		this.templates = new ArrayList<>();
	}

	public void add(Template template) {
		this.templates.add(template);
	}

	public Template getRandomTemplate() {
		return getRandomTemplate(1);
	}

	public Template getRandomTemplate(int arguments) {
		System.out.println("Looking for template matching " + arguments + " arguments.");
		List<Template> validTemplates = new ArrayList<>();
		for (Template t : this.templates) {
			if (t.min_arguments <= arguments && arguments <= t.max_arguments)
				validTemplates.add(t);
		}
		System.out.println("Found " + validTemplates.size() + " valid candidate templates.");
		if (validTemplates.size() == 0)
			return null;
		int rand = Helper.getRandomInt(0, validTemplates.size() -1);
		return validTemplates.get(rand);
	}
}

class Template {
	public int min_arguments;
	public int max_arguments;
	public String template;

	Template(String template) {
		this.min_arguments = 1;
		this.max_arguments = 0;
		this.template = template;
	}

	Template(int min_arguments, String template) {
		this.min_arguments = min_arguments;
		this.max_arguments = Integer.MAX_VALUE;
		this.template = template;
	}

	Template(int min_arguments, String template, int max_arguments) {
		this.min_arguments = min_arguments;
		this.max_arguments = max_arguments;
		this.template = template;
	}
}

public class RandomChoice extends AbstractListener {
	private Command local_command;
	private TemplateCollection templates;

	@Override
	protected void initHook() {
		templates = new TemplateCollection();
		templates.add(new Template(0, "Ah, I was just about to do that!", 0));
		templates.add(new Template(0, "Whatever you're thinking, don't.", 0));
		templates.add(new Template(0, "Yes, but only if you do it right now.", 0));
		templates.add(new Template(0, "Maybe. In a few minutes.", 0));
		templates.add(new Template(0, "Does cats like knocking things off of other things?", 0));
		templates.add(new Template(0, "If you pet me, yes, otherwise no.", 0));
		templates.add(new Template(0, "Only if you stab Inari first.", 0));
		templates.add(new Template(0, "Ohwouldyoulookatthetime! I suddenly need to be on the other side of the planet!", 0));
		templates.add(new Template(0, "You haven't pet me recently, so no.", 0));
		templates.add(new Template(0, "I wouldn't do that if I were you...", 0));
		templates.add(new Template(1, "Why would you do that when you could do something else instead?", 1));
		templates.add(new Template(1, "\"{choice}\" doesn't really seem like a good idea right now.", 1));
		templates.add(new Template(1, "No, maybe tomorrow.", 1));
		templates.add(new Template(1, "Are you sure? Well alright.", 1));
		templates.add(new Template(1, "Oh yes, definitely!", 1));
		templates.add(new Template(1, "Hm. I can't choose. Ask me again in a couple of minutes.", 1));
		templates.add(new Template(1, "I don't think I've heard of \"{choice}\", so probably not.", 1));
		templates.add(new Template(1, "Yes! Do it now!", 1));
		templates.add(new Template(1, "Hm, yeah okay.", 1));
		templates.add(new Template(1, "Ah... well, I'd say wait an hour.", 1));
		templates.add(new Template(1, "Oh, I've heard about that. You'll want to wait until tomorrow.", 1));
		templates.add(new Template(1, "Boo! No!", 1));
		templates.add(new Template(1, "I'd advice against \"{choice}\" right now.", 1));
		templates.add(new Template(2, "Some \"{choice}\" sounds nice"));
		templates.add(new Template(2, "I'm 40% \"{choice}\"!"));
		templates.add(new Template(2, "You *could* do \"{choice}\", I guess."));
		templates.add(new Template(2, "Why not {count}? Okay fine. \"{choice}\"."));
		templates.add(new Template(2, "I sense some \"{choice}\" in your future!"));
		templates.add(new Template(2, "\"{choice}\" is for cool kids!"));
		templates.add(new Template(3, "Definitely \"{choice}\"... Or maybe \"{other_choice}\"..."));
		templates.add(new Template(2, "If I had a gold nugget for every time someone asked me about \"{choice}\""));
		templates.add(new Template(2, "The proof is in the pudding. Definitely \"{choice}\". Now please get it out of my pudding."));
		templates.add(new Template(2, "I received a message from future you, said to go with \"{choice}\"."));
		templates.add(new Template(2, "I saw that \"{choice}\" is the best choice in a vision"));
		templates.add(new Template(2, "You'll want to go with \"{choice}\"."));
		templates.add(new Template(2, "Elementary dear Watson, \"{choice}\" is the obvious choice!"));
		templates.add(new Template(2, "My grandfather always told me that \"{choice}\" is the way to go!"));
		templates.add(new Template(2, "If I've learned anything in life it's that you always pick \"{choice}\""));
		templates.add(new Template(2, "Once you get a taste of \"{choice}\" you can't stop."));
		templates.add(new Template(3, "One the one hand, there's \"{choice}\" but then there's also \"{other_choice}\""));
		templates.add(new Template(2, "Somebody once told me to roll with \"{choice}\""));
		templates.add(new Template(2, "Out of these {raw_count} choices? I'd say \"{choice}\"."));
		templates.add(new Template(2, "I've heard \"{choice}\" is in these days"));
		templates.add(new Template(2, "I spy with my robotic eye something beginning with \"{choice}\"!"));
		templates.add(new Template(2, "Haven't you always gone with \"{choice}\"? Hm, maybe not."));
		templates.add(new Template(2, "I have a pamphlet that says never to engage in \"{choice}\", so you should definitely do it!"));
		templates.add(new Template(2, "Pretty sure I'd want you to go with \"{choice}\"!"));
		templates.add(new Template(2, "The sands of time whisper to me... they're saying \"{choice}\"."));
		templates.add(new Template(2, "I tried reading my tea leaves this morning. There was something about death and doom. Anyway, go with \"{choice}\""));
		templates.add(new Template(2, "Eeny, meeny, miny, {choice}."));
		templates.add(new Template(2, "{choice}'os, for a complete breakfast!"));
		templates.add(new Template(2, "\"{choice}\", now with 30% fewer deaths caused by negligence!"));
		templates.add(new Template(2, "Hold on tightly! \"{choice}\" is a wild ride!"));
		templates.add(new Template(2, "A wizard is never late, and sometimes engages in some \"{choice}\"."));
		templates.add(new Template(2, "I received a telegram from a long lost relative that only read \"{choice}\". Weird."));
		templates.add(new Template(2, "Wait, what was the question again? Uhh... \"{choice}\"?"));
		templates.add(new Template(2, "I want a divorce. I'm taking half the \"{choice}\"."));
		templates.add(new Template(2, "Is it a bird?! Is it a plane?! No! It's \"{choice}\"!"));

		local_command = new Command("choose", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String splitOn = ", or | or |,(?! )";
				String stripPunctuationFromEnd = "[?.!:]*$";
				ArrayList<String> parts = new ArrayList<>();
				Collections.addAll(parts, this.argumentParser.getArgument(0).split("; ?"));
				if (parts.size() == 2) {
					ArrayList<String> subParts = new ArrayList<>();
					Collections.addAll(subParts, parts.get(1).split(splitOn));
					for (int i = 0; i < subParts.size(); i++) {
						subParts.set(i, subParts.get(i).replaceAll(stripPunctuationFromEnd, ""));
					}
					String choice = subParts.get(Helper.getRandomInt(0, subParts.size() - 1));
//					if (parts[0].matches())
					Pattern pattern = Pattern.compile("\\$\\d\\d?");
					Matcher matcher = pattern.matcher(parts.get(0));
					ArrayList<String> groups = new ArrayList<>();
					while (matcher.find())
						if (!groups.contains(matcher.group()))
							groups.add(matcher.group());
					if (groups.size() > 0) {
						Collections.shuffle(subParts);
						Helper.sendMessage(target, Helper.replacePlaceholders(parts.get(0), subParts));
						return;
					} else if (parts.get(0).contains("$")) {
						Helper.sendMessage(target, parts.get(0).replace("$", choice), nick);
						return;
					}
					Helper.sendMessage(target, choice + " " + parts.get(0), nick);
					return;
				} else if (parts.size() > 2) {
					Helper.sendMessage(target, "What?!", nick);
					return;
				}
				parts = new ArrayList<>();
				for (String part : params.split(splitOn)) {
					if (!part.replace(" ", "").equals(""))
						parts.add(part.replaceAll(stripPunctuationFromEnd, ""));
				}
				System.out.println("Parts has " + parts.size() + " elements: " + parts);
//				String msg = output.get(Helper.getRandomInt(0, output.size() - 1));
				String msg = templates.getRandomTemplate(parts.size()).template;

				if (parts.size() == 0) {
					Helper.sendMessage(target, msg, nick);
					return;
				}

				String choice = parts.get(Helper.getRandomInt(0, parts.size() - 1)).trim();
				msg = msg.replaceAll("\\{choice}", choice);
				if (parts.size() > 1) {
					String other_choice = "";
					while (other_choice == "" || other_choice == choice) {
						other_choice = parts.get(Helper.getRandomInt(0, parts.size() - 1)).trim();
					}
					msg = msg.replaceAll("\\{other_choice}", other_choice);
				}
				else
					msg = msg.replaceAll("\\{other_choice}", "something else");
				String count = "";
				String raw_count = "";
				
				RuleBasedNumberFormat ruleBasedNumberFormat = new RuleBasedNumberFormat( new Locale("EN", "US"), RuleBasedNumberFormat.SPELLOUT ); 
				
				switch (parts.size())
				{
					case 1:
						count = "the";
						raw_count = "one";
					case 2:
						count = "both";
						raw_count = "two";
						break;
					default:
						count = "all " + ruleBasedNumberFormat.format(parts.size());
						raw_count = "" + ruleBasedNumberFormat.format(parts.size());
				}
				msg = msg.replaceAll("\\{count}", count);
				msg = msg.replaceAll("\\{raw_count}", raw_count);

				Helper.sendMessage(target, msg, nick);
			}
		}; local_command.setHelpText("Randomly picks a choice for you.");
		local_command.registerAlias("choice");
		local_command.registerAlias("pick");
		IRCBot.registerCommand(local_command);
	}
}
