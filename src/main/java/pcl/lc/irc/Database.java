package pcl.lc.irc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class Database {
	private static Connection connection;
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
}
