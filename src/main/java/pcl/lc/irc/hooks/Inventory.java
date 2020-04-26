/**
 *
 */
package pcl.lc.irc.hooks;

import com.github.kevinsawicki.timeago.TimeAgo;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.pircbotx.Colors;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.httpd.httpd;
import pcl.lc.irc.*;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;
import pcl.lc.utils.Item;
import pcl.lc.utils.PasteUtils;
import pcl.lc.utils.db_items.InventoryItem;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Caitlyn
 */
@SuppressWarnings("rawtypes")
public class Inventory extends AbstractListener {
	private Command local_command;
	private Command sub_command_list;
	private Command sub_command_create;
	private Command sub_command_remove;
	private Command sub_command_preserve;
	private Command sub_command_unpreserve;
	private Command sub_command_count;
	private Command sub_command_favourite;
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
		local_command.registerSubCommand(sub_command_create);
		local_command.registerSubCommand(sub_command_remove);
		local_command.registerSubCommand(sub_command_preserve);
		local_command.registerSubCommand(sub_command_unpreserve);
		local_command.registerSubCommand(sub_command_count);
		local_command.registerSubCommand(sub_command_favourite);
		IRCBot.registerCommand(local_command);
		Database.addStatement("CREATE TABLE IF NOT EXISTS Inventory(id INTEGER PRIMARY KEY, item_name, uses_left INTEGER)");
		Database.addUpdateQuery(2, "ALTER TABLE Inventory ADD is_favourite BOOLEAN DEFAULT 0 NULL");
		Database.addUpdateQuery(2, "ALTER TABLE Inventory ADD added_by VARCHAR(255) DEFAULT '' NULL");
		Database.addUpdateQuery(2, "ALTER TABLE Inventory ADD added INT DEFAULT NULL NULL;");
		Database.addUpdateQuery(6, "ALTER TABLE Inventory ADD owner VARCHAR(255) DEFAULT null");
		Database.addUpdateQuery(6, "ALTER TABLE Inventory ADD cursed BOOLEAN DEFAULT 0 NULL");

		Database.addPreparedStatement("getCompressedSentences", "SELECT id, item_name, uses_left FROM Inventory WHERE item_name LIKE '%Compressed Sentence%'");
		Database.addPreparedStatement("setCompressedSentences", "UPDATE Inventory SET item_name = ?, uses_left = ? WHERE id = ?");
		Database.addPreparedStatement("newCompressedSentence", "INSERT INTO Inventory (item_name, uses_left, is_favourite, added_by, added) VALUES (?,?,?,?,?)");
		Database.addPreparedStatement("getItems", "SELECT id, item_name, uses_left, is_favourite, added_by, added FROM Inventory;");
		Database.addPreparedStatement("getItem", "SELECT id, item_name, uses_left, is_favourite, added_by, added FROM Inventory WHERE id = ?;");
		Database.addPreparedStatement("getFavouriteItem", "SELECT id, item_name, uses_left, is_favourite, added_by, added FROM Inventory WHERE is_favourite = 1 LIMIT 1");
		Database.addPreparedStatement("getItemByName", "SELECT id, item_name, uses_left, is_favourite, added_by, added FROM Inventory WHERE item_name = ?;");
		Database.addPreparedStatement("getRandomItem", "SELECT id, item_name, uses_left, is_favourite, added_by, added, owner, cursed FROM Inventory ORDER BY Random() LIMIT 1");
		Database.addPreparedStatement("getRandomItems", "SELECT id, item_name, uses_left, is_favourite, added_by, added, owner, cursed FROM Inventory ORDER BY Random() LIMIT ?");
		Database.addPreparedStatement("getRandomItemNonFavourite", "SELECT id, item_name, uses_left, is_favourite, added_by, added, owner, cursed FROM Inventory WHERE is_favourite IS 0 ORDER BY Random() LIMIT 1");
		Database.addPreparedStatement("getRandomItemsNonFavourite", "SELECT id, item_name, uses_left, is_favourite, added_by, added, owner, cursed FROM Inventory WHERE is_favourite IS 0 ORDER BY Random() LIMIT ?");
		Database.addPreparedStatement("addItem", "INSERT INTO Inventory (id, item_name, uses_left, is_favourite, added_by, added) VALUES (NULL, ?, ?, ?, ?, ?)");
		Database.addPreparedStatement("removeItemId", "DELETE FROM Inventory WHERE id = ?");
		Database.addPreparedStatement("removeItemName", "DELETE FROM Inventory WHERE item_name = ?");
		Database.addPreparedStatement("decrementUses", "UPDATE Inventory SET uses_left = uses_left - 1 WHERE id = ?");
		Database.addPreparedStatement("setUses", "UPDATE Inventory SET uses_left = ? WHERE id = ?");
		Database.addPreparedStatement("clearFavourite", "UPDATE Inventory SET is_favourite = 0 WHERE is_favourite = 1");
		Database.addPreparedStatement("preserveItem", "UPDATE Inventory SET uses_left = -1 WHERE item_name = ?");
		Database.addPreparedStatement("unPreserveItem", "UPDATE Inventory SET uses_left = 5 WHERE item_name = ?");
		httpd.registerContext("/inventory", new InventoryHandler(), "Inventory");
	}

	private void initCommands() {
		local_command = new Command("inventory", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				Helper.sendMessage(target, this.trySubCommandsMessage(params), nick);
			}
		};
		local_command.registerAlias("inv");
		local_command.setHelpText("Interact with the bots inventory");
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
					Helper.sendMessage(target, "The inventory contains " + getInventorySize() + " items.");
				} catch (Exception e) {
					e.printStackTrace();
					Helper.sendAction(target, "shrugs");
				}
			}
		};
		sub_command_create = new Command("create", 60, true) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (params.toLowerCase().equals("myself") || params.toLowerCase().equals(IRCBot.getOurNick()))
					Helper.sendMessage(target, "I can't add myself to the inventory.", nick);
				else if (nick.toLowerCase().equals(params.toLowerCase()))
					Helper.sendMessage(target, "You can't add yourself to the inventory.", nick);
				else {
					params = params.replaceAll("[.!?,‽]$", "");
					if (params.length() > 0)
						Helper.sendAction(target, addItem(params, nick, false, true));
					else
						Helper.sendAction(target, "adds nothing to " + Helper.parseSelfReferral("his") + " inventory.");
				}
			}
		};
		sub_command_create.registerAlias("add");
		sub_command_remove = new Command("remove", 60, true) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				boolean hasPermission = Permissions.hasPermission(IRCBot.bot, (MessageEvent) event, Permissions.ADMIN);
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
		sub_command_remove.registerAlias("del");
		sub_command_preserve = new Command("preserve", 0, true) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (Permissions.hasPermission(IRCBot.bot, (MessageEvent) event, Permissions.ADMIN)) {
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
				if (Permissions.hasPermission(IRCBot.bot, (MessageEvent) event, Permissions.ADMIN)) {
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
		sub_command_favourite = new Command("favourite", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				try {
					PreparedStatement getFav = Database.getPreparedStatement("getFavouriteItem");
					ResultSet fav = getFav.executeQuery();
					if (fav.next()) {
						Helper.sendMessage(target, "My favourite item is " + fav.getString(2) + Colors.NORMAL + " added by " + fav.getString(5), nick);
					} else {
						Helper.sendMessage(target, "I have no favourite item right now.", nick);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Helper.sendMessage(target, "Something went wrong.", nick);
				}
			}
		};
		sub_command_favourite.registerAlias("fav");
	}

	static class InventoryHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			TimeAgo time = new TimeAgo();
			String target = t.getRequestURI().toString();
			String response = "";
			List<NameValuePair> paramsList = URLEncodedUtils.parse(t.getRequestURI(),"utf-8");
			String items = "";
			if (paramsList.size() >= 0) {
				try {
					PreparedStatement statement = Database.getPreparedStatement("getItems");
					ResultSet resultSet = statement.executeQuery();
					items = "<table><tr><th>Item Name</th><th>Added By</th><th>Added</th></tr>";
					while (resultSet.next()) {
						
						items += "<tr><td>" + resultSet.getString(2) + ((resultSet.getInt(3) == -1) ? " (*)" : "") + "</td><td>" + ((resultSet.getString(5).isEmpty()) ? "N/A" : resultSet.getString(5)) + "</td><td>" + ((resultSet.getLong(6) == 0) ? "N/A" : time.timeAgo(resultSet.getLong(6))) + "</td></tr>";
					}
					items += "</table>";
					items = StringUtils.strip(items, "\n");
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			
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
					response = response + line.replace("#BODY#", target).replace("#BOTNICK#", IRCBot.getOurNick()).replace("#INVDATA#", items).replace("#NAVIGATION#", navData)+"\n";
				}
			}
			t.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	/**
	 * Calls getRandomItem(boolean can_be_favourite) with can_be_favourite = true
	 * @return Item
	 */
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
				return new Item(resultSet.getInt(1), resultSet.getString(2) + Colors.NORMAL, resultSet.getInt(3), resultSet.getBoolean(4), resultSet.getString(5), resultSet.getInt(6), resultSet.getString(7), resultSet.getBoolean(8));
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
	 * @return int Value of one of Inventory.ERROR_... statics
	 */
	public static int removeItem(String id_or_name, boolean override_favourite, boolean override_preserved) {
		InventoryItem item;
		try {
			item = InventoryItem.GetByID(Integer.parseInt(id_or_name));
		} catch (NumberFormatException e) {
			item = InventoryItem.GetByName(id_or_name);
		}

		if (item != null) {
			if (item.is_favourite && !override_favourite) {
				return ERROR_ITEM_IS_FAVOURITE;
			} else if (item.uses_left == -1 && !override_preserved) {
				return ERROR_ITEM_IS_PRESERVED;
			} else {
				boolean result = item.Delete();

				if (result)
					return 0;
			}
		}
		return ERROR_NO_ROWS_RETURNED;
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

	public static String addItem(String item) {
		return addItem(item, null, false, false);
	}

	public static String addItem(String item, String added_by, boolean override_duplicate_check) {
		return addItem(item, added_by, override_duplicate_check, false);
	}

	public static String addItem(String item, String added_by) {
		return addItem(item, added_by, false, false);
	}

	public static String addItem(String item, String added_by, boolean override_duplicate_check, boolean blob_instead_of_decline) {
		if (item.contains(IRCBot.ournick + "'s") || !item.contains(IRCBot.ournick)) {
			try {
				PreparedStatement getItemByName = Database.getPreparedStatement("getItemByName");
				getItemByName.setString(1, item);

				System.out.println("Duplicate item!");
				ResultSet result = getItemByName.executeQuery();
				if (result.next()) {
					if (!override_duplicate_check) {
						if (!blob_instead_of_decline)
							return "already has a few of those.";
						else if (result.getBoolean(4) || result.getInt(3) == -1)
							return "watches the summoning fizzle";
						else {
							Inventory.removeItem(new Item(item, true));
							Item has_blob = null;
							try {
								has_blob = new Item("Massive Blob", true);
							} catch (Exception ignored) {
								System.out.println("No blob found");
							}
							if (has_blob == null)
								Inventory.addItem("Massive Blob", added_by, true, false);
							return "watches the summoning misfire and the two identical items merge into a massive, unidentifiable blob";
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (item.length() > Item.maxItemNameLength)
				{
					PreparedStatement get = Database.getPreparedStatement("getCompressedSentences");
					ResultSet result = get.executeQuery();
					if (result.next())
					{
						int uses = result.getInt(3);
						int id = result.getInt(1);
						uses++;
						PreparedStatement update = Database.getPreparedStatement("setCompressedSentences");
						update.setString(1, uses + "x Compressed Sentences");
						update.setInt(2, uses);
						update.setInt(3, id);
						update.executeUpdate();
					}
					else
					{
						PreparedStatement add = Database.getPreparedStatement("newCompressedSentence");
						//item_name, uses_left, is_favourite, added_by, added
						add.setString(1, "1x Compressed Sentence");
						add.setInt(2, 1);
						add.setBoolean(3, false);
						add.setString(4, added_by);
						add.setLong(5, new Timestamp(System.currentTimeMillis()).getTime());
						add.executeUpdate();
					}
					return "compresses the sentence into a more manageable format since it was too long.";
				}

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
				int uses = getUsesFromName(item);
				addItem.setInt(2, uses);
				addItem.setInt(3, (favourite) ? 1 : 0);
				if (added_by != null)
					addItem.setString(4, added_by);
				else
					addItem.setString(4, "");
				addItem.setLong(5, new Timestamp(System.currentTimeMillis()).getTime());
				if (addItem.executeUpdate() > 0) {
					if (favourite)
						return "summons '" + item + Colors.NORMAL + "' and adds to " + Helper.parseSelfReferral("his") + " inventory. I love this! This is my new favourite thing!";
					else
						return "summons '" + item + Colors.NORMAL + "' and adds to " + Helper.parseSelfReferral("his") + " inventory. " + getUsesIndicator(uses);
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

	public static void addRawItem(Item item) {
		addRawItem(item.getNameRaw(), item.getUsesLeft(), item.isFavourite(), item.getAdded_by(), item.getAddedRaw());
	}

	public static void addRawItem(String item, int uses_left, boolean favourite, String added_by, int added) {
		try {
			PreparedStatement addItem = Database.getPreparedStatement("addItem");
			item = item.replaceAll(" ?\\(\\*\\)", ""); //Replace any (*) to prevent spoofing preserved item marks
			String itemEscaped = StringEscapeUtils.escapeHtml4(item);
			addItem.setString(1, itemEscaped);
			addItem.setInt(2, uses_left);
			addItem.setInt(3, (favourite) ? 1 : 0);
			if (added_by != null)
				addItem.setString(4, added_by);
			else
				addItem.setString(4, "");
			addItem.setInt(5, added);
			addItem.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getItemBreakString(String item) {
		return getItemBreakString(item, false);
	}

	public static String getItemBreakString(String item, boolean includeEndPunctuation) {
		ArrayList<String> strings = new ArrayList<>();
		strings.add("{item} poofs away in a sparkly cloud.");
		strings.add("{item} vanishes into a rift in space.");
		strings.add("{item} phases out of the dimension.");
		strings.add("{item} flickers and pops out of existence.");
		strings.add("{item} suddenly ceases to be.");
		strings.add("{item} ruptures and deflates.");
		strings.add("{item} melts into a puddle of unidentifiable goo.");
		strings.add("{item} rides off into the sunset on a horse with no name.");
		strings.add("{item} flies up into space and collides with a satellite.");
		strings.add("{item} falls into a chasm.");
		strings.add("{item} is eaten by a Grue.");
		strings.add("{item} sinks into quicksand.");
		strings.add("{item} vibrates into the ground.");
		strings.add("{item} gets lost in the woods and is never seen again.");
		strings.add("{item} flies into space and doesn't come back.");
		strings.add("{item} angered a witch and was turned into a toad.");
		strings.add("{item} took the red pill and exited the matrix.");
		strings.add("{item} took the blue pill and fell asleep.");
		strings.add("{item} was taken out by the mafia.");
		strings.add("{item} was loaned out to a friend and was never returned.");
		strings.add("{item} looked too much like a carrot and was eaten by a near-sighted bunny.");
		strings.add("{item} got into a fight with bigfoot and lost.");
		strings.add("{item} looked too much like a tooth and was claimed by the tooth-fairy.");
		strings.add("{item} looked into the void and was consumed.");
		strings.add("{item} fell into a vat of radioactive goo.");
		strings.add("{item} returned to it's original reality.");
		strings.add("{item} met the Doctor and went on numerous adventures through time and space.");
		strings.add("{item} met a Pikachu and was shocked.");
		strings.add("{item} was caught by Ash, gotta catch 'em all!");
		strings.add("{item} was claimed by a dragon and added to it's hoard.");
		strings.add("{item} miscalculated and teleported into space.");
		strings.add("{item} suddenly collapses into a singularity.");
		strings.add("{item} angered a gnome and didn't get away in time.");
		strings.add("{item} angered a gnome and didn't put up enough of a fight.");
		strings.add("{item} angered a fairy and was turned into a pie.");
		strings.add("{item} angered a dragon and was incinerated.");
		strings.add("{item} angered a unicorn and was pierced.");
		strings.add("{item} returned a DoesNotExistException.");
		strings.add("{item} ran out of memory.");
		strings.add("{item} tried to report a bug with no log and mysteriously vanished.");
		strings.add("{item} experienced a segfault.");
		strings.add("{item} was needed in a different plane of existence.");
		strings.add("an adventurer came by and claimed {item} was the artifact they were looking for to save their village.");
		strings.add("a bug was found in {item} and it was decommissioned.");
		strings.add("{item} was suddenly outlawed and confiscated by the MIB.");
		strings.add("it turns out {item} reacts poorly to acid.");
		strings.add("evidence of {item}'s poor resistance to corrosive chemicals is abundantly clear.");
		strings.add("{item} received a call it had won a million money, it wasn't seen again.");
		strings.add("{item} was shiny enough to be claimed by a dragon.");
		strings.add("if {item} had been less shiny it might not have attracted the attention of a dragon.");
		strings.add("turns out {item}'s weakness was common water all along!");
		strings.add("{item} didn't have an immunity to the common cold!");
		strings.add("{item} melted in the sun...");
		strings.add("right as {item} was at it's prime, reality caught up with it.");
		strings.add("{item} suddenly realized it had somewhere else to be!");
		strings.add("for all of {item}'s flaws, it's biggest was that it no longer exists.");
		strings.add("now you see {item}, now you don't!");
		strings.add("{item} ascended to a higher plane.");
		strings.add("{item} fell into a well that appeared out of nowhere!");

		String str = strings.get(Helper.getRandomInt(0, strings.size() - 1)).replace("{item}", item);
		if (!includeEndPunctuation)
			str = str.replaceAll("[!.?]{1,3}$", "");
		return str;
	}

	public static String fixItemName(String item, boolean sort_out_prefxies) {
		return fixItemName(item, sort_out_prefxies, false);
	}

	public static String fixItemName(String item, boolean sort_out_prefixes, boolean no_prefix) {
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
		return ((found_prefix && !no_prefix) ? "the " : "") + StringEscapeUtils.unescapeHtml4(item);
	}

	public static int getUsesFromName(String name) {
		int length_penalty = name.length() / 20; //this is the length where the bonus turns into a penalty
		int actual_penalty = (int) ((length_penalty < 1) ? 5 - Math.floor(length_penalty * 5) : -Math.floor(length_penalty));
		return Math.max(1, (Helper.getRandomInt(1, 4) + actual_penalty));
	}

	public static int getInventorySize() {
		int counter = 0;
		PreparedStatement getItems;
		try {
			getItems = Database.getPreparedStatement("getItems");
			ResultSet result = getItems.executeQuery();
			while (result.next())
				counter++;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return counter;
	}

	public String chan;
	public String target = null;

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args, String callingRelay) {
		if (local_command.shouldExecute(command, event) >= 0) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange, String callingRelay) {
		target = Helper.getTarget(event);
		local_command.tryExecute(command, nick, target, event, copyOfRange, false);
	}
}
