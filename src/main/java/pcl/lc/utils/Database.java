package pcl.lc.utils;

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
	private static Connection connection;
	/**
	 * Updated automatically
	 */
	public static int DB_VER = 0;
	public final static Map<String, PreparedStatement> preparedStatements = new HashMap<>();
	static Statement statement;
	public static List<UpdateQuery> updateQueries = new ArrayList<>();

	public static void init() throws SQLException {
		connection = DriverManager.getConnection("jdbc:sqlite:michibot.db");
		statement = connection.createStatement();
		statement.setPoolable(true);
		statement.setQueryTimeout(30);  // set timeout to 30 sec.
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

	public static boolean storeJsonData(String key, String data) {
		try {
			statement.executeQuery("CREATE TABLE IF NOT EXISTS JsonData (mykey VARCHAR(255) PRIMARY KEY NOT NULL, store TEXT DEFAULT NULL); CREATE UNIQUE INDEX JsonData_key_uindex ON JsonData (mykey)");
		} catch (SQLException e) {
			if (e.getErrorCode() != 101)
				e.printStackTrace();
		}
		try {
			IRCBot.log.info("storeJsonData: ('" + key.toLowerCase() + "', '" + data + "')");
			statement.executeUpdate("INSERT OR REPLACE INTO JsonData (mykey, store) VALUES ('" + key.toLowerCase() + "', '" + data + "')");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		IRCBot.log.error("storeJsonData false");
		return false;
	}

	public static String getJsonData(String key) {
		try {
			statement.executeQuery("CREATE TABLE IF NOT EXISTS JsonData (mykey VARCHAR(255) PRIMARY KEY NOT NULL, store TEXT DEFAULT NULL); CREATE UNIQUE INDEX JsonData_key_uindex ON JsonData (mykey)");
		} catch (SQLException e) {
			if (e.getErrorCode() != 101)
				e.printStackTrace();
		}
		try {
			ResultSet resultSet = statement.executeQuery("SELECT store FROM JsonData WHERE mykey = '" + key.toLowerCase() + "'");
			if (resultSet.next()) {
				IRCBot.log.error("JsonData: " + resultSet.getString(1));
				return resultSet.getString(1);
			}
			IRCBot.log.error("JsonData was empty, returning empty string");
			return "";
		} catch (SQLException e) {
			IRCBot.log.info("Code: " + e.getErrorCode());
			e.printStackTrace();
		}
		IRCBot.log.error("JsonData try/catch failed");
		return "";
	}

	public static ResultSet ExecuteQuery(String query) throws SQLException {
		return statement.executeQuery(query);
	}
}
