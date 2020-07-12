package pcl.lc.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import pcl.lc.irc.IRCBot;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	//private static Connection connection;
	/**
	 * Updated automatically
	 */
	public static int DB_VER = 0;
	public final static Map<String, PreparedStatement> preparedStatements = new HashMap<>();
	static Statement statement;
	public static List<UpdateQuery> updateQueries = new ArrayList<>();

	public static void init() throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://" + Config.mysqlDbHost + (Config.mysqlDbPort == null || Config.mysqlDbPort == "" ? ":3306" : ":" + Config.mysqlDbPort) + "/" + Config.mysqlDbName + "?rewriteBatchedStatements=true&useUnicode=true";
			connection = DriverManager.getConnection(url, Config.mysqlDbUser, Config.mysqlDbPass);
			statement = connection.createStatement();
			IRCBot.log.info("Connected to " + url);
		} catch (Exception e) {
			e.printStackTrace();
			IRCBot.log.info("Failed to connect to MySQL database at " + Config.mysqlDbHost + ", falling back to SQLite");
			connection = DriverManager.getConnection("jdbc:sqlite:michibot.db");
			statement = connection.createStatement();
			statement.setPoolable(true);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
		}
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
