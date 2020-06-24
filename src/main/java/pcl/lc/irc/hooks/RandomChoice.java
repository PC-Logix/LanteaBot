package pcl.lc.irc.hooks;

import com.ibm.icu.text.RuleBasedNumberFormat;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
		System.out.println("Looking for template matching " + arguments + " arguments");
		int counter = 0;
		int maxIterations = 100;
		int rand = 0;
		Template template = null;
		while (template == null && counter < maxIterations) {
			counter++;
			rand = Helper.getRandomInt(0, this.templates.size() - 1);
			Template thisTemplate = this.templates.get(rand);
			if (thisTemplate.min_arguments <= arguments && (thisTemplate.max_arguments == 0 || arguments <= thisTemplate.max_arguments))
				template = thisTemplate;
		}
		return template;
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
		this.max_arguments = 0;
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

		local_command = new Command("choose") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String[] parts = params.split(" or ");
//				String msg = output.get(Helper.getRandomInt(0, output.size() - 1));
				String msg = templates.getRandomTemplate(parts.length).template;

				String choice = parts[Helper.getRandomInt(0, parts.length - 1)].trim();
				msg = msg.replaceAll("\\{choice}", choice);
				if (parts.length > 1) {
					String other_choice = "";
					while (other_choice == "" || other_choice == choice) {
						other_choice = parts[Helper.getRandomInt(0, parts.length - 1)].trim();
					}
					msg = msg.replaceAll("\\{other_choice}", other_choice);
				}
				else
					msg = msg.replaceAll("\\{other_choice}", "something else");
				String count = "";
				String raw_count = "";
				
				RuleBasedNumberFormat ruleBasedNumberFormat = new RuleBasedNumberFormat( new Locale("EN", "US"), RuleBasedNumberFormat.SPELLOUT ); 
				
				switch (parts.length)
				{
					case 1:
						count = "the";
						raw_count = "one";
					case 2:
						count = "both";
						raw_count = "two";
						break;
					default:
						count = "all " + ruleBasedNumberFormat.format(parts.length);
						raw_count = "" + ruleBasedNumberFormat.format(parts.length);
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

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args, String callingRelay) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange, String callingRelay) {
		target = Helper.getTarget(event);
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}
