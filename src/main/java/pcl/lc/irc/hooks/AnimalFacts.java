/**
 *
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.CommandChainStateObject;
import pcl.lc.utils.Helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Michiyo / Stolen from Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class AnimalFacts extends AbstractListener {
    private Command local_command;
    private Command local_command2;

    @Override
    protected void initHook() {
        initCommands();
        IRCBot.registerCommand(local_command);
        IRCBot.registerCommand(local_command2);
    }


    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    private void initCommands() {
        local_command = new Command("catfact") {
            @Override
            public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws IOException, JSONException {
                    JSONObject json = readJsonFromUrl("https://some-random-api.com/animal/cat");
                    Helper.sendMessage(target, json.get("fact").toString());
                return new CommandChainStateObject();
            }
        };

        local_command2 = new Command("fact", new CommandArgumentParser(0, new CommandArgument(ArgumentTypes.STRING, "Animal"))) {
            @Override
            public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws IOException, JSONException {
                List<String> animalNames = Arrays.asList("dog", "cat", "panda", "fox", "red_panda", "koala", "birb", "bird", "racoon", "raccoon", "kangaroo");
                
                String aminal = this.argumentParser.getArgument("Animal");
                
                if (aminal == null || aminal.equals("random") || aminal.equals("")) {
                    Random rand = new Random();
                    aminal = animalNames.get(rand.nextInt(animalNames.size()));
                }
                
                if (aminal.equals("birb")) {
                    aminal = "bird"; //Cause the OLD API was dumb. *sigh*
                } else if (aminal.equals("red panda")) {
                    aminal = "red_panda";
                } else if (aminal.equals("racoon")) {
                    aminal = "raccoon";
                }
                if (animalNames.contains(aminal)) {
                    JSONObject json = readJsonFromUrl("https://some-random-api.com/animal/" + aminal);
                    Helper.sendMessage(target, aminal.substring(0, 1).toUpperCase() + aminal.substring(1) + " fact: " + json.get("fact").toString());
                } else {
                    Helper.sendMessage(target, "Not a valid option. " + String.join(", ", animalNames));
                }
                return new CommandChainStateObject();
            }
        };
        local_command.registerAlias("catfacts");
        local_command.setHelpText("Cat Facts!");
        local_command2.setHelpText("Animal facts!");
    }
}
