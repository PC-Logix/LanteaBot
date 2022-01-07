package pcl.lc.utils;

import com.google.api.client.util.DateTime;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import org.reflections.Reflections;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

import java.lang.reflect.Field;
import java.net.ConnectException;
import java.sql.*;
import java.util.*;

class UpdateQuery {
	private int minVersion;
	private String updateQuery;

	UpdateQuery(int minVersion, String updateQuery) {
		this.minVersion = minVersion;
		this.updateQuery = updateQuery;
	}

	int getMinVersion() {
		return this.minVersion;
	}

	String getUpdateQuery() {
		return updateQuery;
	}
}

public class Database {
	public static Connection connection;
	private static Connection tempConnection;
	static Statement statement;
	private static Statement tempStatement;
	public static int DB_VER = 0; // Updated automatically
	public final static Map<String, PreparedStatement> preparedStatements = new HashMap<>();
	public static List<UpdateQuery> updateQueries = new ArrayList<>();

	public static String sqliteDefaultPath = "bot.db";

	public static boolean init() throws SQLException {
		if (Config.targetDbMode.equalsIgnoreCase("mysql")) {
			if (Config.mysqlDbHost == null || Config.mysqlDbHost.isEmpty()) {
				IRCBot.log.error("MySQL mode has been requested but no host has been specified.");
				return false;
			}
			if (Config.mysqlDbName == null || Config.mysqlDbName.isEmpty()) {
				IRCBot.log.error("MySQL mode has been requested but no database name has been specified.");
				return false;
			}
			try {
//				Class.forName("com.mysql.cj.jdbc.Driver");
				String url = "jdbc:mysql://" + Config.mysqlDbHost + (Config.mysqlDbPort == null || Config.mysqlDbPort.equals("") ? ":3306" : ":" + Config.mysqlDbPort) + "/" + Config.mysqlDbName + "?rewriteBatchedStatements=true&useUnicode=true";
				connection = DriverManager.getConnection(url, Config.mysqlDbUser, Config.mysqlDbPass);
				statement = connection.createStatement();
				IRCBot.log.info("Connected to " + url);
			} catch (SQLNonTransientConnectionException | CommunicationsException e) {
				IRCBot.log.error(" ### Failed to connect to MySQL database at " + Config.mysqlDbHost + " ### ");
				IRCBot.log.error(e.getMessage());
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else if (Config.targetDbMode.equalsIgnoreCase("sqlite")) {
			try {
				connection = DriverManager.getConnection("jdbc:sqlite:" + Config.sqlitePath);
				statement = connection.createStatement();
				statement.setPoolable(true);
				statement.setQueryTimeout(30);  // set timeout to 30 sec.
			} catch (SQLNonTransientConnectionException e) {
				IRCBot.log.error(" ### Failed to connect to SQLite database at " + Config.sqlitePath + " ###");
				IRCBot.log.error(e.getMessage());
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		// DB transfer if targetDbMode is set to other db type than currentDbMode
		// Will attempt to transfer contents of db to new database
		if (!Config.currentDbMode.equalsIgnoreCase(Config.targetDbMode)) {
			if (Config.currentDbMode.equals("mysql")) {
				try {
//					Class.forName("com.mysql.cj.jdbc.Driver");
					String url = "jdbc:mysql://" + Config.mysqlDbHost + (Config.mysqlDbPort == null || Config.mysqlDbPort.equals("") ? ":3306" : ":" + Config.mysqlDbPort) + "/" + Config.mysqlDbName + "?rewriteBatchedStatements=true&useUnicode=true";
					tempConnection = DriverManager.getConnection(url, Config.mysqlDbUser, Config.mysqlDbPass);
					tempStatement = tempConnection.createStatement();
				} catch (SQLNonTransientConnectionException | CommunicationsException e) {
					IRCBot.log.error(" ### Failed to connect to MySQL database at " + Config.mysqlDbHost + " ### ");
					IRCBot.log.error(e.getMessage());
					return false;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			} else if (Config.currentDbMode.equals("sqlite")) {
				try {
					tempConnection = DriverManager.getConnection("jdbc:sqlite:" + Config.sqlitePath);
					tempStatement = tempConnection.createStatement();
					tempStatement.setPoolable(true);
					tempStatement.setQueryTimeout(30);  // set timeout to 30 sec.
				} catch (SQLNonTransientConnectionException e) {
					IRCBot.log.error(" ### Failed to connect to SQLite database at " + Config.sqlitePath + " ###");
					IRCBot.log.error(e.getMessage());
					return false;
				}
			}
			Config.prop.setProperty("currentDbMode", Config.targetDbMode);
			Config.saveProps();
			System.out.println("Post db migration");
			return false;
		}
		return true;
	}

	public static boolean tableInit() {
		IRCBot.log.info("Init database entries in utils.db_items (create tables)");
		Reflections db_items = new Reflections("pcl.lc.utils.db_items");
		Set<Class<? extends DatabaseEntry>> allClasses = db_items.getSubTypesOf(DatabaseEntry.class);
		System.out.println("Found " + allClasses.size() + " db_item entries");
		for (Class<? extends DatabaseEntry> s : allClasses) {
			String table = "?";
			try {
				table = s.getDeclaredField("table").get(null).toString();
				System.out.println(s.getName());
				String query = "CREATE TABLE IF NOT EXISTS '" + table + "'(%s);";
				ArrayList<String> fields = new ArrayList<>();
				for (Field myField : s.getDeclaredFields()) {
					if (!DatabaseEntry.ignoreField(myField.getName())) {
						String primary = "";
						if (myField.getName().equals(s.getDeclaredField("primary_key").get("null").toString())) {
							primary = " PRIMARY KEY";
//							if (myField.getType() == int.class)
//								primary += " AUTOINCREMENT";
						}
						if (myField.getType() == boolean.class)
							fields.add("'" + myField.getName() + "' BOOLEAN" + primary);
						else if (myField.getType() == String.class)
							fields.add("'" + myField.getName() + "' VARCHAR(1000)" + primary);
						else if (myField.getType() == int.class)
							fields.add("'" + myField.getName() + "' INTEGER" + primary);
						else if (myField.getType() == float.class)
							fields.add("'" + myField.getName() + "' FLOAT" + primary);
						else if (myField.getType() == double.class)
							fields.add("'" + myField.getName() + "' DOUBLE" + primary);
						else if (myField.getType() == DateTime.class)
							fields.add("'" + myField.getName() + "' DATETIME" + primary);
					}
				}
				query = String.format(query, String.join(", ", fields));
				System.out.println(query);
				Database.statement.executeUpdate(query);
			} catch (Exception e) {
				IRCBot.log.error("An exception occurred while initializing database table '" + table + "'");
				e.printStackTrace();
			}
		}
		return true;
	}

	public static boolean addStatement(String sql) {
		try {
			statement.executeUpdate(sql);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean addPreparedStatement(String name, String sql) {
		try {
			preparedStatements.put(name, connection.prepareStatement(sql));
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean addPreparedStatement(String name, String sql, int options) {
		try {
			preparedStatements.put(name, connection.prepareStatement(sql, options));
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static Connection getConnection() {
		return connection;
	}

	public static PreparedStatement getPreparedStatement(String statement) throws Exception {
		if (!preparedStatements.containsKey(statement)) {
			throw new Exception("Invalid statement!");
		}
		return preparedStatements.get(statement);
	}

	public static int getDBVer() {
		try {
			ResultSet dbVerQuery = Database.getConnection().createStatement().executeQuery("PRAGMA user_version;");
			return dbVerQuery.getInt("user_version");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static int setDBVer(int dbVer) {
		if (getDBVer() < dbVer) {
			try {
				return Database.getConnection().createStatement().executeUpdate("PRAGMA user_version = " + dbVer + ";");
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	/**
	 * @param minVersion int
	 * @param sql String
	 */
	public static void addUpdateQuery(int minVersion, String sql) {
		updateQueries.add(new UpdateQuery(minVersion, sql));
		if (minVersion > DB_VER) {
			DB_VER = minVersion;
		}
	}

	public static void updateDatabase() {
		int counter = 0;
		int currentVer = getDBVer();
		IRCBot.log.info("Updating database! Current version: " + currentVer);
		for (UpdateQuery query : updateQueries) {
			if (currentVer < query.getMinVersion()) {
				try {
					Database.getConnection().createStatement().executeUpdate(query.getUpdateQuery());
					setDBVer(query.getMinVersion());
					counter++;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		IRCBot.log.info("Database update complete! New version: " + getDBVer());
		IRCBot.log.info("Database update ran " + counter + " queries");
	}

	public static void storeJsonData(String key, HashMap<String, ? extends Object> data) throws Exception {
		storeJsonData(key, new Gson().toJson(data));
	}

	/**
	 *
	 * @param key The key the data should be stored with, overwrites existing data if key exists
	 * @param data The data to be stored
	 */
	public static void storeJsonData(String key, String data) throws Exception {
		IRCBot.log.info("storeJsonData: ('" + key.toLowerCase() + "', '" + data + "')");
		PreparedStatement stmt = getPreparedStatement("storeJSON");
		stmt.setString(1, key);
		stmt.setString(2, data);
		stmt.executeUpdate();
	}

	public static HashMap<String, Integer> getJsonHashMapInt(String key) throws Exception {
		return new Gson().fromJson(getJsonData(key), new TypeToken<HashMap<String, Integer>>(){}.getType());
	}

	public static HashMap<String, Double> getJsonHashMapDouble(String key) throws Exception {
		return new Gson().fromJson(getJsonData(key), new TypeToken<HashMap<String, Double>>(){}.getType());
	}

	public static HashMap<String, Boolean> getJsonHashMapBoolean(String key) throws Exception {
		return new Gson().fromJson(getJsonData(key), new TypeToken<HashMap<String, Boolean>>(){}.getType());
	}

	public static HashMap<String, String> getJsonHashMapString(String key) throws Exception {
		return new Gson().fromJson(getJsonData(key), new TypeToken<HashMap<String, String>>(){}.getType());
	}

	/**
	 * Fetch JSON data from database with key
	 * @param key The key of the requested row
	 * @return Returns the contents of the row matching key, or an empty string if key was not found
	 */
	public static String getJsonData(String key) throws Exception {
		PreparedStatement stmt = getPreparedStatement("retreiveJSON");
		stmt.setString(1, key);

		ResultSet theResult = stmt.executeQuery();
		if (theResult.next()) {
			String result = theResult.getString(1);
			IRCBot.log.info("JsonData: " + result);
			return result;
		}
		IRCBot.log.error("JsonData was empty, returning empty string");
		return "";
	}

	public static void destroyJsonData(String key) throws Exception {
		PreparedStatement stmt = getPreparedStatement("destroyJSON");
		stmt.setString(1, key);
		stmt.executeUpdate();
	}

	public static ResultSet ExecuteQuery(String query) throws SQLException {
		return statement.executeQuery(query);
	}
}
