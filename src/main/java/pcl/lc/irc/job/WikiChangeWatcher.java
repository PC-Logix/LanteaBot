package pcl.lc.irc.job;
/*
 * Author AfterLifeLochie
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;


import pcl.lc.irc.IRCBot;
import pcl.lc.utils.EnumChangeType;

public class WikiChangeWatcher extends Thread {

	private class WikiChange {
		public EnumChangeType changeType = EnumChangeType.UNKNOWN;

		public String changeTarget = "";
		public String changeUser = "";

		public String changeReason = "";
		public int changeInstanceCount = 1;

		public WikiChange(EnumChangeType type, String target, String user, String reason) {
			changeType = type;
			changeTarget = target;
			changeUser = user;
			changeReason = reason;
		}

		public boolean isChangeIdentical(WikiChange compareTo) {
			if (!changeType.equals(compareTo.changeType)) {
				return false;
			}
			if (!changeTarget.equals(compareTo.changeTarget)) {
				return false;
			}
			if (!changeUser.equals(compareTo.changeUser)) {
				return false;
			}
			if (!changeReason.equals(compareTo.changeReason)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append("type=").append(changeType.val);
			buf.append(",target=").append(changeTarget);
			buf.append(",user=").append(changeUser);
			buf.append(",reason=").append(changeReason);
			buf.append(",instances=").append(changeInstanceCount);
			return buf.toString();
		}

	}

	private boolean abort = false;
	private WikiChange lastChange;
	private ArrayList<WikiChange> changeSet = new ArrayList<WikiChange>();


	@Override
	public void run() {
		while (!abort) {
			try {
				URLConnection c = new URL(
						"http://lanteacraft.com/wiki/api.php?action=query&list=recentchanges&format=json&rcprop=user|userid|comment|title|flags|loginfo&rclimit=10")
				.openConnection();
				c.setRequestProperty("User-Agent", IRCBot.USER_AGENT);

				StringBuilder buffer = new StringBuilder();
				BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
				for (String line; (line = reader.readLine()) != null;)
					buffer.append(line).append("\r\n");
				reader.close();

				JSONObject o = new JSONObject(buffer.toString());
				JSONObject oQueryNode = o.getJSONObject("query");
				JSONArray oRecentData = oQueryNode.getJSONArray("recentchanges");
				if (oRecentData.length() > 0) {
					if (lastChange == null) {
						lastChange = createChangeFromJSON(oRecentData.getJSONObject(0));	
					}

					if (!lastChange.isChangeIdentical(createChangeFromJSON(oRecentData.getJSONObject(0)))) {
						System.out.println(oRecentData.length());
						for (int i = 0; i < oRecentData.length(); i++) {
							JSONObject oChange = oRecentData.getJSONObject(i);
							WikiChange subChange = createChangeFromJSON(oChange);
							if (!lastChange.isChangeIdentical(subChange)) {
								changeSet.add(subChange);
							} else
								break;
						}

						lastChange = createChangeFromJSON(oRecentData
								.getJSONObject(0));
					}

					ArrayList<WikiChange> outList = new ArrayList<WikiChange>();

					boolean instanceAlreadyExists = false;
					for (WikiChange w : changeSet) {
						instanceAlreadyExists = false;
						for (WikiChange cI : outList) {
							if (instanceAlreadyExists)
								break;
							if (w.changeType.equals(cI.changeType)
									&& w.changeUser.equals(cI.changeUser)) {
								cI.changeInstanceCount++;
								instanceAlreadyExists = true;
								break;
							}
						}
						if (!instanceAlreadyExists)
							outList.add(w);
					}

					changeSet.clear();

					for (WikiChange w : outList) {
						StringBuilder ob = new StringBuilder();
						if (w.changeInstanceCount != 1) {
							// User
							ob.append("'").append(w.changeUser).append("'");
							// Action
							ob.append(" ").append(w.changeType.val).append("");
							// Number of items
							ob.append(" [").append(w.changeInstanceCount)
							.append(" instances]");
							ob.append(" ( ")
							.append("http://lanteacraft.com/wiki/")
							.append("index.php?title=Special:RecentChanges")
							.append(" ).");
						} else {
							@SuppressWarnings("deprecation")
							String safeURL = URLEncoder.encode(w.changeTarget);
							// User
							ob.append("'").append(w.changeUser).append("'");
							// Action
							ob.append(" ").append(w.changeType.val).append("");
							// Target
							ob.append(" '").append(w.changeTarget).append("':");
							ob.append(" '").append(w.changeReason).append("'.");
							ob.append(" ( ")
							.append("http://lanteacraft.com/wiki/")
							.append("index.php?title=").append(safeURL)
							.append(" ).");
						}
						IRCBot.bot.sendIRC().message("#LanteaCraft", ob.toString());
					}

					outList.clear();
				}


			} catch (Exception e) {
				e.printStackTrace();
				if (e instanceof IOException)
					IRCBot.bot.sendIRC().message("#LanteaCraft", "An IOException occured when contacting lanteacraft.com.");
			}

			try {
				Thread.sleep(1 * 60 * 1000L);
			} catch (InterruptedException iex) {
			}
		}
	}

	public WikiChange createChangeFromJSON(JSONObject oChange) {
		try {
			String comment = oChange.getString("comment");
			comment = comment.replace("/*", "").replace("*/", "");

			EnumChangeType action = EnumChangeType.UNKNOWN;
			String type = oChange.getString("type");

			if (type.equals("log")) {
				if (oChange.getString("logaction").equals("block")) {
					action = EnumChangeType.ADMIN_BLOCKED_ACCOUNT;
				} else if (oChange.getString("logtype").equals("newusers")
						&& oChange.getString("logaction").equals("create")) {
					// action represents user creation
					action = EnumChangeType.CREATED_ACCOUNT;
				} else if (oChange.getString("logtype").equals("rights")) {
					// action represents rights change
					action = EnumChangeType.ADMIN_CHANGED_RIGHTS;
				} else if (oChange.getString("logaction").equals("delete")) {
					// action represents deletion action
					action = EnumChangeType.DELETED_PAGE;
				} else if (oChange.getString("logaction").equals("move")) {
					// action represents move operation
					action = EnumChangeType.MOVED_PAGE;
				} else if (oChange.getString("logaction").equals("upload")) {
					// action represents upload operation
					action = EnumChangeType.UPLOADED_FILE;
				}
			} else if (type.equals("edit")) {
				if (oChange.getString("title").startsWith("User:")) {
					action = EnumChangeType.EDITED_USER_PAGE;
				} else if (oChange.getString("title").startsWith("Talk:")) {
					action = EnumChangeType.EDITED_TALK_PAGE;
				} else if (oChange.getString("title").startsWith("User talk:")) {
					action = EnumChangeType.EDITED_USER_TALK_PAGE;
				} else {
					action = EnumChangeType.EDITED_PAGE;
				}
			} else if (type.equals("new")) {
				action = EnumChangeType.CREATED_PAGE;
			}

			return new WikiChange(action, oChange.getString("title"),
					oChange.getString("user"), comment);
		} catch (Exception x) {
			System.err.println("Could not create change from JSON object.");
			return null;
		}
	}

	public void abort() {
		this.abort = true;
	}

}