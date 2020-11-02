/**
 *
 */
package pcl.lc.irc.hooks;

import com.google.common.collect.Lists;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Michiyo / Stolen from Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class CatFacts extends AbstractListener {
    private Command local_command;

    @Override
    protected void initHook() {
        initCommands();
        IRCBot.registerCommand(local_command);
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

    //public static void main(String[] args) throws IOException, JSONException {
    //    JSONObject json = readJsonFromUrl("https://catfact.ninja/fact");
    //    System.out.println(json.toString());
        //System.out.println(json.get("fact"));
    //}

    private void initCommands() {
        local_command = new Command("catfact") {
            @Override
            public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
                try {
                    JSONObject json = readJsonFromUrl("https://catfact.ninja/fact");
                    Helper.sendMessage(target, json.get("fact").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        local_command.registerAlias("catfacts");
        local_command.setHelpText("Cat Facts!");
    }
}
