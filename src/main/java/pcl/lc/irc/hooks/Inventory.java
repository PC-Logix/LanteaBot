/**
 *
 */
package pcl.lc.irc.hooks;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import pcl.lc.httpd.httpd;
import pcl.lc.irc.*;
import pcl.lc.utils.Helper;
import pcl.lc.utils.PasteUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Caitlyn
 */
@SuppressWarnings("rawtypes")
public class Inventory extends AbstractListener {
	private Command local_command;
	private Command sub_command_list;
	private Command sub_command_add;
	private Command sub_command_remove;
	private Command sub_command_preserve;
	private Command sub_command_unpreserve;
	private Command sub_command_count;
	private static double favourite_chance = 0.01;

	static int ERROR_ITEM_IS_FAVOURITE = 1;
	static int ERROR_INVALID_STATEMENT = 2;
	static int ERROR_SQL_ERROR = 3;
	static int ERROR_NO_ROWS_RETURNED = 4;
	static int ERROR_ID_NOT_SET = 5;
	static int ERROR_ITEM_IS_PRESERVED = 6;

	static String html;
	
	public Inventory() throws IOException {
		InputStream htmlIn = getClass().getResourceAsStream("/html/inventory.html");
		html = CharStreams.toString(new InputStreamReader(htmlIn, Charsets.UTF_8));
	}
	
	@Override
	protected void initHook() {
		initCommands();
		local_command.registerSubCommand(sub_command_list);
		local_command.registerSubCommand(sub_command_add);
		local_command.registerSubCommand(sub_command_remove);
		local_command.registerSubCommand(sub_command_preserve);
		local_command.registerSubCommand(sub_command_unpreserve);
		local_command.registerSubCommand(sub_command_count);
		IRCBot.registerCommand(local_command, "Interact with the bots inventory");
		Database.addStatement("CREATE TABLE IF NOT EXISTS Inventory(id INTEGER PRIMARY KEY, item_name, uses_left INTEGER)");
		Database.addUpdateQuery(2, "ALTER TABLE Inventory ADD is_favourite BOOLEAN DEFAULT 0 NULL");
		Database.addUpdateQuery(2, "ALTER TABLE Inventory ADD added_by VARCHAR(255) DEFAULT '' NULL");
		Database.addUpdateQuery(2, "ALTER TABLE Inventory ADD added INT DEFAULT NULL NULL;");

		Database.addPreparedStatement("getItems", "SELECT id, item_name, uses_left, is_favourite, added_by, added FROM Inventory;");
		Database.addPreparedStatement("getItem", "SELECT id, item_name, uses_left, is_favourite, added_by, added FROM Inventory WHERE id = ?;");
		Database.addPreparedStatement("getItemByName", "SELECT id, item_name, uses_left, is_favourite, added_by, added FROM Inventory WHERE item_name = ?;");
		Database.addPreparedStatement("getRandomItem", "SELECT id, item_name, uses_left, is_favourite, added_by, added FROM Inventory ORDER BY Random() LIMIT 1");
		Database.addPreparedStatement("getRandomItems", "SELECT id, item_name, uses_left, is_favourite, added_by, added FROM Inventory ORDER BY Random() LIMIT ?");
		Database.addPreparedStatement("getRandomItemNonFavourite", "SELECT id, item_name, uses_left, is_favourite, added_by, added FROM Inventory WHERE is_favourite IS 0 ORDER BY Random() LIMIT 1");
		Database.addPreparedStatement("getRandomItemsNonFavourite", "SELECT id, item_name, uses_left, is_favourite, added_by, added FROM Inventory WHERE is_favourite IS 0 ORDER BY Random() LIMIT ?");
		Database.addPreparedStatement("addItem", "INSERT INTO Inventory (id, item_name, uses_left, is_favourite, added_by, added) VALUES (NULL, ?, ?, ?, ?, ?)");
		Database.addPreparedStatement("removeItemId", "DELETE FROM Inventory WHERE id = ?");
		Database.addPreparedStatement("removeItemName", "DELETE FROM Inventory WHERE item_name = ?");
		Database.addPreparedStatement("decrementUses", "UPDATE Inventory SET uses_left = uses_left - 1 WHERE id = ?");
		Database.addPreparedStatement("setUses", "UPDATE Inventory SET uses_left = ? WHERE id = ?");
		Database.addPreparedStatement("clearFavourite", "UPDATE Inventory SET is_favourite = 0 WHERE is_favourite = 1");
		Database.addPreparedStatement("preserveItem", "UPDATE Inventory SET uses_left = -1 WHERE item_name = ?");
		Database.addPreparedStatement("unPreserveItem", "UPDATE Inventory SET uses_left = 5 WHERE item_name = ?");
		IRCBot.httpServer.registerContext("/inventory", new InventoryHandler());
	}

	private void initCommands() {
		local_command = new Command("inventory", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "Unknown sub-command '" + params + "' (Try: " + local_command.getSubCommandsAsString(true) + ")", nick);
			}
		};
		local_command.registerAlias("inv");
		sub_command_list = new Command("list", 0, true) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					if (Config.httpdEnable.equals("true")){
						Helper.sendMessage(target, "Here's my inventory: " + httpd.getBaseDomain() + "/inventory", nick);
					} else {
						String items = "";
						try {
							PreparedStatement statement = Database.getPreparedStatement("getItems");
							ResultSet resultSet = statement.executeQuery();
							while (resultSet.next()) {
								items += resultSet.getString(2) + ((resultSet.getInt(3) == -1) ? " (*)" : "") + "\n";
							}
							if (items == "") {
								Helper.sendMessage(target, "There are no items.", nick);
							} else {
								items = StringUtils.strip(items, "\n");
								Helper.sendMessage(target, "Here's my inventory: " + PasteUtils.paste(items), nick);
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					Helper.sendMessage(target, "Wrong things happened! (5)", nick);
				}
			}
		};
		sub_command_count = new Command("count", 0, true) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				try {
					PreparedStatement getItems = Database.getPreparedStatement("getItems");
					ResultSet result = getItems.executeQuery();
					int counter = 0;
					while (result.next())
						counter++;
					Helper.sendMessage(target, "The inventory contains " + counter + " items.");
				} catch (Exception e) {
					e.printStackTrace();
					Helper.sendAction(target, "shrugs");
				}
			}
		};
		sub_command_add = new Command("add", 0, true) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, addItem(params, nick), nick);
			}
		};
		sub_command_remove = new Command("remove", 0, true) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				boolean hasPermission = Permissions.hasPermission(IRCBot.bot, (MessageEvent) event, 4);
				int removeResult = removeItem(params, hasPermission, hasPermission);
				if (removeResult == 0)
					Helper.sendMessage(target, "Removed item from inventory", nick);
				else if (removeResult == ERROR_ITEM_IS_FAVOURITE)
					Helper.sendMessage(target, "This is my favourite thing. You can't make me get rid of it.", nick);
				else if (removeResult == ERROR_ITEM_IS_PRESERVED)
					Helper.sendMessage(target, "I've been told to preserve this. You can't remove it.", nick);
				else if (removeResult == ERROR_NO_ROWS_RETURNED)
					Helper.sendMessage(target, "No such item", nick);
				else
					Helper.sendMessage(target, "Wrong things happened! (" + removeResult + ")", nick);
			}
		};
		sub_command_remove.registerAlias("rem");
		sub_command_preserve = new Command("preserve", 0, true) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (Permissions.hasPermission(IRCBot.bot, (MessageEvent) event, 4)) {
					try {
						PreparedStatement preserveItem = Database.getPreparedStatement("preserveItem");
						preserveItem.setString(1, params);
						preserveItem.executeUpdate();
						Helper.sendMessage(target, "Item preserved", nick);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					Helper.sendMessage(target, "I'm afraid you don't have the power to preserve this item.", nick);
				}
			}
		};
		sub_command_preserve.registerAlias("pre");
		sub_command_unpreserve = new Command("unpreserve", 0, true) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (Permissions.hasPermission(IRCBot.bot, (MessageEvent) event, 4)) {
					try {
						PreparedStatement unPreserveItem = Database.getPreparedStatement("unPreserveItem");
						unPreserveItem.setString(1, params);
						unPreserveItem.executeUpdate();
						Helper.sendMessage(target, "Item un-preserved", nick);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					Helper.sendMessage(target, "I'm afraid you don't have the power to preserve this item.", nick);
				}
			}
		};
		sub_command_unpreserve.registerAlias("unpre");
	}

	static class InventoryHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {

			String target = t.getRequestURI().toString();
			String response = "";
			List<NameValuePair> paramsList = URLEncodedUtils.parse(t.getRequestURI(),"utf-8");
			String items = "";
			if (paramsList.size() >= 0) {
				try {
					PreparedStatement statement = Database.getPreparedStatement("getItems");
					ResultSet resultSet = statement.executeQuery();
					while (resultSet.next()) {
						items += resultSet.getString(2) + ((resultSet.getInt(3) == -1) ? " (*)" : "") + "<br>";
					}
					items = StringUtils.strip(items, "\n");
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				/*try {
					PreparedStatement getAllQuotes = Database.getPreparedStatement("getAllQuotes");
					ResultSet results = getAllQuotes.executeQuery();
					while (results.next()) {
						quoteList = quoteList + "<a href=\"?id=" + results.getString(1) +"\">Quote #"+results.getString(1)+"</a><br>\n";
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}*/
			}
			// convert String into InputStream
			InputStream is = new ByteArrayInputStream(html.getBytes());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String line = null;
				while ((line = br.readLine()) != null) {
					response = response + line.replace("#BODY#", target).replace("#BOTNICK#", IRCBot.getOurNick()).replace("#INVDATA#", items)+"\n";
				}
			}
			t.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	public static Item getRandomItem() {
		return getRandomItem(true);
	}

	public static Item getRandomItem(boolean can_be_favourite) {
		try {
			PreparedStatement statement;
			if (can_be_favourite)
				statement = Database.getPreparedStatement("getRandomItem");
			else
				statement = Database.getPreparedStatement("getRandomItemNonFavourite");
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next())
				return new Item(resultSet.getInt(1), resultSet.getString(2), resultSet.getInt(3), resultSet.getBoolean(4), resultSet.getString(5), resultSet.getInt(6));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int removeItem(Item item) {
		return removeItem(item.getId());
	}

	public static int removeItem(Integer id) {
		return removeItem(id.toString(), false, false);
	}

	public static int removeItem(Integer id, boolean override_favourite, boolean override_preserved) {
		return removeItem(id.toString(), override_favourite, override_preserved);
	}
	
	public static int removeItem(String id) {
		return removeItem(id, false, false);
	}

	/**
	 * Will not remove the item marked as favourite unless override is true
	 * Returns 0 on success or higher on failure
	 *
	 * @param id_or_name         String
	 * @param override_favourite boolean
	 * @return int
	 */
	@SuppressWarnings("Duplicates")
	public static int removeItem(String id_or_name, boolean override_favourite, boolean override_preserved) {
		Integer id = null;
		Boolean id_is_string = true;
		PreparedStatement removeItem;
		try {
			id = Integer.valueOf(id_or_name);
			id_is_string = false;

			try {
				removeItem = Database.getPreparedStatement("removeItemId");
				removeItem.setString(1, id.toString());
			}
			catch (Exception e) {
				e.printStackTrace();
				return ERROR_INVALID_STATEMENT;
			}
		}
		catch (NumberFormatException e) {
			try {
				removeItem = Database.getPreparedStatement("removeItemName");
				removeItem.setString(1, id_or_name);
			}
			catch (Exception ex) {
				ex.printStackTrace();
				return ERROR_INVALID_STATEMENT;
			}
		}

		if (!override_favourite || !override_preserved) {
			PreparedStatement getItem;
			try {
				if (id_is_string) {
					getItem = Database.getPreparedStatement("getItemByName");
					getItem.setString(1, id_or_name);
				}
				else if (id != null) {
					getItem = Database.getPreparedStatement("getItem");
					getItem.setInt(1, id);
				}
				else
					return ERROR_ID_NOT_SET;

				ResultSet result = getItem.executeQuery();

				if (result.next()) {
					if (result.getBoolean(4) && !override_favourite)
						return ERROR_ITEM_IS_FAVOURITE;
					else if (result.getInt(3) == -1 && !override_preserved)
						return ERROR_ITEM_IS_PRESERVED;
				}
				else
					return ERROR_NO_ROWS_RETURNED;
			}
			catch (Exception e) {
				e.printStackTrace();
				return ERROR_INVALID_STATEMENT;
			}
		}

		try {
			if (removeItem.executeUpdate() > 0) {
				return 0;
			}
			else {
				return ERROR_NO_ROWS_RETURNED;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return ERROR_SQL_ERROR;
		}
	}

	public static String getUsesIndicator(int uses) {
		switch (uses) {
			case 1: case 2: case 3:
				return "This seems rather fragile...";
			case 4: case 5: case 6: case 7:
				return "I could get some good swings in with this.";
			case 8: case 9: case 10:
				return "This seems very sturdy.";
			default:
				return "Is this indestructible?";
		}
	}

	public static String addItem(Item item) {
		return addItem(item.getName(), item.getAdded_by());
	}

	private static String addItem(String item) {
		return addItem(item, null, false);
	}

	private static String addItem(String item, String added_by) {
		return addItem(item, added_by, false);
	}

	private static String addItem(String item, String added_by, boolean override_duplicate_check) {
		if (item.contains(IRCBot.ournick + "'s") || !item.contains(IRCBot.ournick)) {
			try {
				PreparedStatement getItemByName = Database.getPreparedStatement("getItemByName");
				getItemByName.setString(1, item);

				ResultSet result = getItemByName.executeQuery();
				if (!override_duplicate_check && result.next()) {
					return "I already have one of those.";
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			try {
				boolean favourite = false;
				int fav_roll = Helper.getRandomInt(0, 100);
				System.out.println("Favourite roll: " + fav_roll);
				if (fav_roll < (100 * favourite_chance)) {
					System.out.println("New favourite! Clearing old favourite.");
					favourite = true;
					PreparedStatement clearFavourite = Database.getPreparedStatement("clearFavourite");
					clearFavourite.executeUpdate();
				}
				System.out.println("Favourites cleared, adding item");
				PreparedStatement addItem = Database.getPreparedStatement("addItem");
				item = item.replaceAll(" ?\\(\\*\\)", ""); //Replace any (*) to prevent spoofing preserved item marks
				String itemEscaped = StringEscapeUtils.escapeHtml4(item);
				addItem.setString(1, itemEscaped);
				int length_penalty = item.length() / 20; //this is the length where the bonus turns into a penalty
				int actual_penalty = (int) ((length_penalty < 1) ? 5 - Math.floor(length_penalty * 5) : -Math.floor(length_penalty));
				int uses = Math.max(1, (Helper.getRandomInt(1, 4) + actual_penalty));
				addItem.setInt(2, uses);
				addItem.setInt(3, (favourite) ? 1 : 0);
				if (added_by != null)
					addItem.setString(4, added_by);
				else
					addItem.setString(4, "");
				addItem.setLong(5, new Timestamp(System.currentTimeMillis()).getTime());
				if (addItem.executeUpdate() > 0) {
					if (favourite)
						return "Added '" + item + "' to inventory. I love this! This is my new favourite thing!";
					else
						return "Added '" + item + "' to inventory. " + getUsesIndicator(uses);
				}
				else {
					return "Wrong things happened! (1)";
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				return "Wrong things happened! (2)";
			}
		}
		else
			return "I can't put myself in my inventory silly.";
	}

	public static String getItemBreakString() {
		ArrayList<String> strings = new ArrayList<>();
		strings.add("poofs away in a sparkly cloud");
		strings.add("vanishes into a rift in space");
		strings.add("phases out of the dimension");
		strings.add("flickers and pops out of existence");
		strings.add("suddenly ceases to be");
		strings.add("ruptures and deflates");
		strings.add("melts into a puddle of unidentifiable goo");
		strings.add("rides off into the sunset on a horse with no name");
		strings.add("flies up into space and collides with a satellite");
		strings.add("falls into a chasm");
		strings.add("is eaten by a Grue");
		strings.add("sinks into quicksand");
		strings.add("vibrates into the ground");

		return strings.get(Helper.getRandomInt(0, strings.size() - 1));
	}

	public static String fixItemName(String item, boolean sort_out_prefixes) {
		boolean found_prefix = false;
		if (sort_out_prefixes) {
			ArrayList<String> prefixes = new ArrayList<>();
			prefixes.add("^ ?the ");
			prefixes.add("^ ?an ");
			prefixes.add("^ ?a ");

			for (String exp : prefixes) {
				String new_item = item.replaceAll("(?i)" + exp, "");
				System.out.println("'" + item + "' != '" + new_item + "' (" + exp + ")");
				if (item != new_item)
					found_prefix = true;
				item = new_item;
			}
		}
		return ((found_prefix) ? "the " : "") + StringEscapeUtils.unescapeHtml4(item);
	}

	public String chan;
	public String target = null;

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (local_command.shouldExecute(command) >= 0) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
			target = nick;
		} else {
			target = chan;
		}
		local_command.tryExecute(command, nick, target, event, copyOfRange, false);
	}

	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		// TODO Auto-generated method stub

	}
}
