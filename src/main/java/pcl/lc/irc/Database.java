package pcl.lc.irc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class Database {
	private static Connection connection;
	/**
	 * This is the database version that the bot expects the database to be at.
	 */
	public static int DB_VER = 1;
	public final static Map<String, PreparedStatement> preparedStatements = new HashMap<>();
	static Statement statement;
	
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
		try {
			int dbVerQuery = Database.getConnection().createStatement().executeUpdate("PRAGMA user_version = "+dbVer+";");
			return dbVerQuery;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
