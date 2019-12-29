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

	public Template getRandomTemplate(int min_arguments) {
		System.out.println("Looking for template with min arguments " + min_arguments);
		int rand = 0;

		Template template = null;
		while (template == null || template.min_arguments > min_arguments) {
			rand = Helper.getRandomInt(0, this.templates.size() - 1);
			template = this.templates.get(rand);
			System.out.println("Found template with " + template.min_arguments + " min arguments");
		}
		return template;
	}
}

class Template {
	public int min_arguments;
	public String template;

	Template(String template) {
		this.min_arguments = 1;
		this.template = template;
	}

	Template(int min_arguments, String template) {
		this.min_arguments = min_arguments;
		this.template = template;
	}
}

public class RandomChoice extends AbstractListener {
	private Command local_command;
	private TemplateCollection templates;

	@Override
	protected void initHook() {
		templates = new TemplateCollection();
		templates.add(new Template("Some \"{choice}\" sounds nice"));
		templates.add(new Template("I'm 40% \"{choice}\"!"));
		templates.add(new Template("You *could* do \"{choice}\", I guess."));
		templates.add(new Template("Why not {count}? Okay fine. \"{choice}\"."));
		templates.add(new Template("I sense some \"{choice}\" in your future!"));
		templates.add(new Template("\"{choice}\" is for cool kids!"));
		templates.add(new Template(3, "Definitely \"{choice}\"... Or maybe \"{other_choice}\"..."));
		templates.add(new Template("If I had a gold nugget for every time someone asked me about \"{choice}\""));
		templates.add(new Template("The proof is in the pudding. Definitely \"{choice}\"."));
		templates.add(new Template("I received a message from future you, said to go with \"{choice}\"."));
		templates.add(new Template("I saw that \"{choice}\" is the best choice in a vision"));
		templates.add(new Template("You'll want to go with \"{choice}\"."));
		templates.add(new Template("Elementary dear Watson, \"{choice}\" is the obvious choice!"));
		templates.add(new Template("My grandfather always told me that \"{choice}\" is the way to go!"));
		templates.add(new Template("If I've learned anything in life it's that you always pick \"{choice}\""));
		templates.add(new Template("Once you get a taste of \"{choice}\" you can't stop."));
		templates.add(new Template(3, "One the one hand, there's \"{choice}\" but then there's also \"{other_choice}\""));
		templates.add(new Template("Somebody once told me to roll with \"{choice}\""));
		templates.add(new Template("Out of these {raw_count} choices? I'd say \"{choice}\"."));
		templates.add(new Template("I've heard \"{choice}\" is in these days"));
		templates.add(new Template("I spy with my robotic eye something beginning with \"{choice}\"!"));

		local_command = new Command("choose", 0) {
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
						msg = "The choice seems obvious..."; break;
					case 2:
						count = "both";
						raw_count = "two";
						break;
					default:
						count = "all " + ruleBasedNumberFormat.format(parts.length);
						raw_count = "" + ruleBasedNumberFormat.format(parts.length);
				}
				if (count != "")
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
