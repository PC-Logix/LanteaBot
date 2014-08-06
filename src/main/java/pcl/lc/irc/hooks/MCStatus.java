package pcl.lc.irc.hooks;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.IRCBot;

@SuppressWarnings("rawtypes")
public class MCStatus extends ListenerAdapter {
	public MCStatus() {
		IRCBot.registerCommand("mcstatus");
	}

	public String isUp(String input) {
		if (input.contains("green")) 
			return Colors.NORMAL + Colors.DARK_GREEN + Colors.BOLD + "Up" + Colors.NORMAL;
		else if (input.contains("yellow"))
			return Colors.NORMAL + Colors.YELLOW + Colors.BOLD + "Slow" + Colors.NORMAL;
		else
			return Colors.NORMAL + Colors.RED + Colors.BOLD + "Down" + Colors.NORMAL;
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
			if (triggerWord.equals(prefix + "mcstatus")) {
				URL url = new URL("http://status.mojang.com/check");
				JSONTokener tokener = null;
				try {
					tokener = new JSONTokener(url.openStream());
					JSONArray root = new JSONArray(tokener);
					String message = "";
					message = message + "Website: "			+ isUp(root.getJSONObject(0).getString("minecraft.net")) + " ";
					message = message + "Session: "			+ isUp(root.getJSONObject(1).getString("session.minecraft.net")) + " ";
					message = message + "Account: "			+ isUp(root.getJSONObject(2).getString("account.mojang.com")) + " ";
					message = message + "Auth: "			+ isUp(root.getJSONObject(3).getString("auth.mojang.com")) + " ";
					message = message + "Skins: "			+ isUp(root.getJSONObject(4).getString("skins.minecraft.net")) + " ";
					message = message + "Authserver: " 		+ isUp(root.getJSONObject(5).getString("authserver.mojang.com")) + " ";
					message = message + "Session: " 		+ isUp(root.getJSONObject(6).getString("sessionserver.mojang.com")) + " ";
					message = message + "API: " 			+ isUp(root.getJSONObject(7).getString("api.mojang.com")) + " ";
					message = message + "Textures: " 		+ isUp(root.getJSONObject(8).getString("textures.minecraft.net")) + " ";
					event.respond(message);
				} catch (IOException ex) {
					event.respond("Server returned an error " + ex);
				} catch (JSONException ex) {
						event.respond("Error " + ex);
					}
			}
		}
	}
}
