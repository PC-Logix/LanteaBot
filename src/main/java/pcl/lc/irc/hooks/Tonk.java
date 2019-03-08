package pcl.lc.irc.hooks;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.httpd.httpd;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A module for Tonking
 * Created by Forecaster on 30/03/2017 for the LanteaBot project.
 */
public class Tonk extends AbstractListener {
    static String numberFormat = "#.########";
	static boolean applyBonusPoints = true;
	private Command local_command;
	private Command reset_command;
	private Command tonkout_command;
	private Command tonkpoints_command;

	@Override
	protected void initHook() {
		initCommands();
		httpd.registerContext("/tonk", new TonkHandler(), "Tonk");
		IRCBot.registerCommand(local_command);
		IRCBot.registerCommand(reset_command);
		IRCBot.registerCommand(tonkout_command);
		IRCBot.registerCommand(tonkpoints_command);
		Database.addPreparedStatement("getTonkCount", "SELECT count(*) FROM JsonData;");
		Database.addPreparedStatement("getTonkUsers", "SELECT mykey, store FROM JsonData WHERE mykey LIKE 'tonkrecord_%' ORDER BY CAST(store AS DECIMAL) DESC;");
	}
	static String html;
	
	public Tonk() throws IOException {
		InputStream htmlIn = getClass().getResourceAsStream("/html/tonk.html");
		html = CharStreams.toString(new InputStreamReader(htmlIn, Charsets.UTF_8));
	}

	private void initCommands() {
		local_command = new Command("tonk", 60) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				nick = nick.replaceAll("\\p{C}", "");
				String tonkin = Database.getJsonData("lasttonk");
				String tonk_record = Database.getJsonData("tonkrecord");
				long now = new Date().getTime();
				IRCBot.log.info("tonkin :" + tonkin + " tonk_record: " + tonk_record);
				if (tonkin == "" || tonk_record == "") {
					Helper.sendMessage(target, "You got the first Tonk " + nick + ", but this is only the beginning.");
					Database.storeJsonData("tonkrecord", "0;" + nick);
                    Database.storeJsonData("lasttonk", String.valueOf(now));
					IRCBot.log.info("No previous tonk found");
				} else {
					long lasttonk = 0;
					try {
						lasttonk = Long.parseLong(tonkin);
					} catch (Exception ex) {
						System.out.println(ex.getMessage());
						IRCBot.log.info(ex.getClass() + ": " + ex.getMessage());
					}

					long diff = now - lasttonk;

					try {
						String tonk[] = tonk_record.split(";");
						long tonk_record_long = Long.parseLong(tonk[0]);
						String recorder = tonk[1].trim();
                        boolean nick_is_recorder = nick.equals(recorder);

						if (tonk_record_long < diff) {
							IRCBot.log.info("New record");
							IRCBot.log.info("'" + recorder + "' == '" + nick + "' => " + (nick_is_recorder ? "true" : "false"));

							String personal_record_key = "tonkrecord_" + nick;
							double hours = GetHoursDouble(diff - tonk_record_long, 2);

							double tonk_record_personal = 0;
							try {
								tonk_record_personal = Double.parseDouble(Database.getJsonData(personal_record_key));
							} catch (Exception ignored) {}

							if (!nick_is_recorder) {
                                System.out.println("Hours added to score: " + hours);
                                tonk_record_personal += hours;
                                Database.storeJsonData(personal_record_key, String.valueOf(tonk_record_personal));
                            } else {
							    System.out.println("No points gained because nick equals record holder");
                            }

							Helper.sendMessage(target, Curse.getRandomCurse() + "! " + nick + "! You beat " + (nick_is_recorder ? "your own" : recorder + "'s") + " previous record of " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)) + "! I hope you're happy!");
                            DecimalFormat dec = new DecimalFormat(numberFormat);
							Helper.sendMessage(target, nick + "'s new record is " + Helper.timeString(Helper.parseMilliseconds(diff)) + "! " + Helper.timeString(Helper.parseMilliseconds(diff - tonk_record_long)) + " gained!" + ((Helper.round(hours / 1000d, 8) > 0) ? (!nick_is_recorder ? (" " + nick + " also gained " + dec.format(hours / 1000d) + " tonk points for stealing the tonk.") : " No points gained for stealing from yourself.") : ""));
							Database.storeJsonData("tonkrecord", diff + ";" + nick);
							Database.storeJsonData("lasttonk", String.valueOf(now));
						} else {
							if (nick_is_recorder) {
								Helper.sendMessage(target, "You still hold the record " + nick + ", for now... " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)));
							} else {
								IRCBot.log.info("No new record set");
								Helper.sendMessage(target, "I'm sorry " + nick + ", you were not able to beat " + recorder + "'s record of " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)) + " this time.");
								Helper.sendMessage(target, Helper.timeString(Helper.parseMilliseconds(diff)) + " were wasted! Missed by " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long - diff)) + "!");
								Database.storeJsonData("lasttonk", String.valueOf(now));
							}
						}
					} catch (Exception ex) {
						IRCBot.log.info(ex.getClass() + ": " + ex.getMessage());
					}
				}
			}
		};
		local_command.setHelpText("What is tonk? Tonk is life.");
		
		reset_command = new Command("resettonk", 60, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				long now = new Date().getTime();
				nick = nick.replaceAll("\\p{C}", "");
				Helper.sendMessage(target, "Tonk reset " + nick + ", you are the record holder!");
				Database.storeJsonData("tonkrecord", "0;" + nick);
				Database.storeJsonData("lasttonk", String.valueOf(now));
			}
		};
		reset_command.setHelpText("What is tonk? Tonk is life.");
		reset_command.registerAlias("tonkreset");

		tonkout_command = new Command("tonkout", 60) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
                nick = nick.replaceAll("\\p{C}", "");
                String tonkin = Database.getJsonData("lasttonk");
                String tonk_record = Database.getJsonData("tonkrecord");
                long now = new Date().getTime();
                IRCBot.log.info("tonkin :" + tonkin + " tonk_record: " + tonk_record);
                if (tonkin == "" || tonk_record == "") {
                    Helper.sendMessage(target, "You got the first Tonk " + nick + ", but this is only the beginning.");
                    Database.storeJsonData("tonkrecord", "0;" + nick);
                    Database.storeJsonData("lasttonk", String.valueOf(now));
                    IRCBot.log.info("No previous tonk found");
                } else {
                    long lasttonk = 0;
                    try {
                        lasttonk = Long.parseLong(tonkin);
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                        IRCBot.log.info(ex.getClass() + ": " + ex.getMessage());
                    }

                    long diff = now - lasttonk;

                    try {
                        String tonk[] = tonk_record.split(";");
                        long tonk_record_long = Long.parseLong(tonk[0]);
                        String recorder = tonk[1].trim();
                        boolean nick_is_recorder = nick.equals(recorder);

                        if (nick_is_recorder) {
                            if (tonk_record_long < diff) {
                                String personal_record_key = "tonkrecord_" + nick;

                                int hours = GetHours(tonk_record_long);

                                double tonk_record_personal = 0;
                                try {
                                    tonk_record_personal = Double.parseDouble(Database.getJsonData(personal_record_key));
                                } catch (Exception ignored) {
                                }

                                tonk_record_personal += hours;

                                if (applyBonusPoints && hours > 1)
                                    tonk_record_personal += 2d * (hours - 1);

                                Database.storeJsonData(personal_record_key, String.valueOf(tonk_record_personal));

                                DecimalFormat dec = new DecimalFormat(numberFormat);
                                Helper.sendMessage(target, nick + " has tonked out! Tonk has been reset! They gained " + dec.format(hours / 1000d) + " tonk points!" + ((applyBonusPoints && hours > 1) ? " plus " + dec.format((2d * (hours - 1)) / 1000d) + " bonus points for consecutive hours!" : "") + " Current score: " + dec.format(tonk_record_personal / 1000d));

                                Database.storeJsonData("tonkrecord", "0;" + nick);
                                Database.storeJsonData("lasttonk", String.valueOf(now));
                            } else {
                                Helper.sendMessage(target, "Time is fickle, but not fickle enough to let you tonk out without passing the current record.", nick);
                            }
                        } else {
                            Helper.sendMessage(target, "You are not the current record holder. It is " + recorder + ".", nick);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
		};
		tonkout_command.setHelpText("Cash in your tonks!");
		tonkout_command.registerAlias("tonktonk");

		tonkpoints_command = new Command("tonkpoints") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {

				nick = nick.replaceAll("\\p{C}", "");
				String data = Database.getJsonData("tonkrecord_" + nick);
				if (data != null && !data.isEmpty()) {
				    DecimalFormat dec = new DecimalFormat(numberFormat);
					Helper.sendMessage(target, "You currently have " + dec.format(Double.parseDouble(data) / 1000d) + " points!", nick);
				} else {
					Helper.sendMessage(target, "I can't find a record, so you have 0 points.", nick);
				}
			}
		};
	}

	static int GetHours(long tonk_time) {
		System.out.println("Record long: " + tonk_time);
		int hours = (int)Math.floor(tonk_time / 1000d / 60d / 60d);
		System.out.println("Hours: " + hours);

		return hours;
	}

	public static double GetHoursDouble(long tonk_time, int decimals) {
		System.out.println("Record long: " + tonk_time);
		double hours = tonk_time / 1000d / 60d / 60d;
		System.out.println("Hours: " + hours);

		return Helper.round(hours, decimals);
	}
	
	static class TonkHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {

			String target = t.getRequestURI().toString();
			String response = "";

			try {
				PreparedStatement getCount = Database.getPreparedStatement("getTonkCount");
				ResultSet res = getCount.executeQuery();
				System.out.println(res.getInt(1));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			String tonkLeaders = "<table>";
			try {
				PreparedStatement statement = Database.getPreparedStatement("getTonkUsers");
				ResultSet resultSet = statement.executeQuery();
				int count = 0;
				while (resultSet.next()) {
					count++;
					DecimalFormat dec = new DecimalFormat(numberFormat);
					tonkLeaders += "<tr><td>" + resultSet.getString(1).replace("tonkrecord_",  "") + "</td><td>" + dec.format(Double.parseDouble(resultSet.getString(2)) / 1000d) + "</td></tr>";
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			tonkLeaders += "</table>";
			List<NameValuePair> paramsList = URLEncodedUtils.parse(t.getRequestURI(),"utf-8");


			String navData = "";
		    Iterator it = httpd.pages.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pair = (Map.Entry)it.next();
		        navData += "<div class=\"innertube\"><h1><a href=\""+ pair.getValue() +"\">"+ pair.getKey() +"</a></h1></div>";
		    }
		    
			// convert String into InputStream
			InputStream is = new ByteArrayInputStream(html.getBytes());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String line = null;
				while ((line = br.readLine()) != null) {
					response = response + line.replace("#BODY#", target).replace("#BOTNICK#", IRCBot.getOurNick()).replace("#TONKDATA#", tonkLeaders)
							.replace("#NAVIGATION#", navData)+"\n";
				}
			}
			t.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
	
	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		target = Helper.getTarget(event);
		if (target.contains("#")) {
			local_command.tryExecute(command, nick, target, event, copyOfRange);
			reset_command.tryExecute(command, nick, target, event, copyOfRange);
			tonkout_command.tryExecute(command, nick, target, event, copyOfRange);
			tonkpoints_command.tryExecute(command, nick, target, event, copyOfRange);
		}
	}
}
