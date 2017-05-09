/**
 * 
 */
package pcl.lc.irc.hooks;

import java.io.BufferedReader;
import java.io.StringReader;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import com.wolfram.alpha.WAEngine;
import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;
import com.wolfram.alpha.WASubpod;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class WolframAlpha extends ListenerAdapter {
	WAEngine engine = new WAEngine();
	public WolframAlpha() {
		IRCBot.registerCommand("wa", "Sends the query to Wolfram Alpha, this is a early dev command, and replies via Query");
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		String prefix = Config.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		if (trigger.length() > 1) {
			String[] firstWord = StringUtils.split(trigger);
			String triggerWord = firstWord[0];
			if (triggerWord.equals(prefix + "wa")) {
				if (!IRCBot.isIgnored(event.getUser().getNick())) {
					if (Config.botConfig.containsKey("WolframAPI")) {
						String apiKey = Config.botConfig.get("WolframAPI").toString();

						engine.setAppID(apiKey);
				        engine.addFormat("plaintext");
				        WAQuery query = engine.createQuery();
				        String expression = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
				        query.setInput(expression);
				        System.out.println(engine.getAppID());
				        
				        try {
				            WAQueryResult queryResult = engine.performQuery(query);
				            
				            if (queryResult.isError()) {
				                //System.out.println("Query error");
				                //System.out.println("  error code: " + queryResult.getErrorCode());
				            	event.respond("  error message: " + queryResult.getErrorMessage());
				            } else if (!queryResult.isSuccess()) {
				            	event.respond("Query was not understood; no results available.");
				            } else {
				                if (queryResult.getPods().length < 0) {
				                	event.respond("No data returned");
				                }
				                for (WAPod pod : queryResult.getPods()) {
				                    if (!pod.isError()) {
				                    	event.getUser().send().message(pod.getTitle());
				                        for (WASubpod subpod : pod.getSubpods()) {
				                            for (Object element : subpod.getContents()) {
				                                if (element instanceof WAPlainText) {
				                                	BufferedReader bufReader = new BufferedReader(new StringReader(((WAPlainText) element).getText()));
				                                	String line=null;
				                                	while( (line=bufReader.readLine()) != null )
				                                	{
				                                		event.getUser().send().message(line);
				                                	}
				                                	
				                                }
				                            }
				                        }
				                    }
				                }
				            }
				        } catch (WAException e) {
				            e.printStackTrace();
				        }
				        
					} else {
						event.respond("WolframAlpha AppID missing");
					}
				}
			}			
		}
	}
}
