package pcl.lc.utils;

import com.google.api.client.util.DateTime;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.apache.commons.lang3.StringEscapeUtils;
import pcl.lc.irc.IRCBot;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Created by Forecaster on 02/06/2018 for the LanteaBot project.
 */
public class DatabaseEntry {
	public static String table;
	public static String primary_key;

	public boolean stored_item = false;

	public static boolean ignoreField(String field) {
		switch (field) {
			case "table":
			case "primary_key":
				return true;
			default:
				return false;
		}
	}

	/**
	 * To use in extending class, create non-overriding method called "Save" that take no arguments. Call super.Save(table) and return result
	 * @param table The target table for query
	 * @return Returns true on successful save, otherwise false
	 */
	protected boolean Save(String table) {
		String query;
		if (this.stored_item)
			query = "REPLACE INTO %s (%s) VALUES (%s)";
		else
			query = "INSERT INTO %s (%s) VALUES (%s)";

		ArrayList<String> fieldArray = new ArrayList<>();
		ArrayList<String> valuePlaceholderArray = new ArrayList<>();
		for (Field field : this.getClass().getDeclaredFields()) {
			if (!ignoreField(field.getName())) {
				try {
					if (!this.stored_item && field.getName().equals(this.getClass().getDeclaredField("primary_key").get(null).toString()) && field.getType() == int.class) {
						System.out.println("Skipped adding primary key '" + field.getName() + "'");
					} else {
						fieldArray.add("'" + field.getName() + "'");
						if (field.getType() == int.class && field.getInt(this) == Integer.MIN_VALUE) {
							valuePlaceholderArray.add("?");
						} else if (field.get(this) == null) {
							valuePlaceholderArray.add("?");
						} else if (field.getType() == boolean.class) {
							valuePlaceholderArray.add("?");
						} else if (field.getType() == int.class) {
							valuePlaceholderArray.add("?");
						} else if (field.getType() == double.class) {
							valuePlaceholderArray.add("?");
						} else if (field.getType() == long.class) {
							valuePlaceholderArray.add("?");
						} else if (field.getType() == short.class) {
							valuePlaceholderArray.add("?");
						} else {
							valuePlaceholderArray.add("?");
						}
					}
				} catch (Exception e) {
					valuePlaceholderArray.add("?");
					e.printStackTrace();
				}
			}
		}
		String fields = String.join(", ", fieldArray);
		String values = String.join(", ", valuePlaceholderArray);
		System.out.println("Found fields: " + fields);
		System.out.println("Found values: " + values);

		query = String.format(query, table, fields, values);
		System.out.println("Query: " + query);

		try {
			PreparedStatement preparedStatement = Database.connection.prepareStatement(query);
			int index = 1;
			for (Field field : this.getClass().getDeclaredFields()) {
				if (!ignoreField(field.getName())) {
					try {
						if (!this.stored_item && field.getName().equals(this.getClass().getDeclaredField("primary_key").get(null).toString()) && field.getType() == int.class) {
							System.out.println("Skipped adding primary key '" + field.getName() + "'");
						} else {
							if (field.getType() == int.class && field.getInt(this) == Integer.MIN_VALUE) {
								preparedStatement.setString(index, null);
							} else if (field.get(this) == null) {
								preparedStatement.setString(index, null);
							} else if (field.getType() == boolean.class) {
								preparedStatement.setBoolean(index, field.getBoolean(this));
							} else if (field.getType() == int.class) {
								preparedStatement.setInt(index, field.getInt(this));
							} else if (field.getType() == double.class) {
								preparedStatement.setDouble(index, field.getDouble(this));
							} else if (field.getType() == long.class) {
								preparedStatement.setLong(index, field.getLong(this));
							} else if (field.getType() == short.class) {
								preparedStatement.setShort(index, field.getShort(this));
							} else {
								preparedStatement.setString(index, String.valueOf(field.get(this)));
							}
							index++;
						}
					} catch (Exception e) {
						e.printStackTrace();
						preparedStatement.setString(index, null);
					}
				}
			}
			preparedStatement.execute();
		} catch (SQLException e) {
			IRCBot.log.error("An exception occurred in DatabaseEntry.Save");
			e.printStackTrace();
			return false;
		}
		this.stored_item = true;
		return true;
	}

	/**
	 * To use in extending class, create non-overriding method called "Delete" that take no arguments. Call super.Delete(table, primary_key) and return result
	 * @param table The target table for query
	 * @param primary_key Field representing the unique id for this table
	 * @return Returns true on successful save, otherwise false
	 */
	protected boolean Delete(String table, String primary_key) {
		String query = "DELETE FROM %s WHERE %s = %s";

		try {
			for (Field field : this.getClass().getDeclaredFields()) {
				if (!ignoreField(field.getName()) && field.getName() == primary_key) {
					String value;
					if (field.getType() == int.class)
						value = String.valueOf(field.getInt(this));
					else if (field.getType() == double.class)
						value = String.valueOf(field.getDouble(this));
					else if (field.getType() == long.class)
						value = String.valueOf(field.getLong(this));
					else if (field.getType() == short.class)
						value = String.valueOf(field.getShort(this));
					else if (field.getType() == String.class)
						value = "'" + StringEscapeUtils.escapeHtml4((String) field.get(this)) + "'";
					else {
						throw new Exception("Primary key not allowed to be type '" + field.getType() + "'");
					}
					query = String.format(query, table, primary_key, value);
					Database.statement.executeUpdate(query);
					return true;
				}
			}
		} catch (Exception e) {
			IRCBot.log.error("An exception occurred in DatabaseEntry.Delete");
			e.printStackTrace();
		}
		this.stored_item = false;
		return false;
	}

	protected static DatabaseEntry GetByField(DatabaseEntry output, String table, String field, Object value) {
		String query = "SELECT * FROM %s WHERE %s = %s";
		String str_value;

		if (value == null)
			str_value = "null";
		else if (value.getClass() == int.class || value instanceof Integer)
			str_value = String.valueOf(value);
		else if (value.getClass() == double.class)
			str_value = String.valueOf(value);
		else if (value.getClass() == long.class)
			str_value = String.valueOf(value);
		else if (value.getClass() == short.class)
			str_value = String.valueOf(value);
		else if (value.getClass() == boolean.class)
			str_value = (boolean) value ? "1" : "0";
		else
			str_value = "'" + StringEscapeUtils.escapeHtml4((String) value) + "'";

		query = String.format(query, table, field, str_value);

		try {
			System.out.println("Execute query: '" + query + "'");
			ResultSet result = Database.statement.executeQuery(query);

			if (result.next()) {
				for (Field myField : output.getClass().getDeclaredFields()) {
					if (!ignoreField(myField.getName())) {
						if (myField.getType() == boolean.class)
							myField.set(output, result.getBoolean(myField.getName()));
						else if (myField.getType() == String.class)
							myField.set(output, result.getString(myField.getName()));
						else if (myField.getType() == int.class)
							myField.set(output, result.getInt(myField.getName()));
						else if (myField.getType() == float.class)
							myField.set(output, result.getFloat(myField.getName()));
						else if (myField.getType() == double.class)
							myField.set(output, result.getDouble(myField.getName()));
						else if (myField.getType() == DateTime.class)
							myField.set(output, result.getDate(myField.getName()));
					}
				}
				output.stored_item = true;
				return output;
			}
		} catch (Exception e) {
			IRCBot.log.error("An exception occurred in DatabaseEntry.GetByField");
			e.printStackTrace();
		}
		return null;
	}

	protected static DatabaseEntryCollection GetManyByField(DatabaseEntryCollection collection, Callable new_entry, String table) {
		return GetManyByField(collection, new_entry, table, null, null, null, null);
	}

	protected static DatabaseEntryCollection GetManyByField(DatabaseEntryCollection collection, Callable new_entry, String table, @Nullable String[] fields, @Nullable Object[] values) {
		return GetManyByField(collection, new_entry, table, fields, values, null, null);
	}

	/**
	 * @param collection A collectio to which items will be added before returning it
	 * @param new_entry Should be something like MyEntry::new, used to create new instances of objects to return
	 * @param table Target table for query
	 * @param fields Array of fields to use in condition
	 * @param values Array of values used in condition, should contain the same amount as fields, conditions are all combined with AND
	 * @param order_by null to ignore, optional order by statement ('field_name, other_field_name' or 'RANDOM()')
	 * @param limit null to ignore, optional limit ('1' or '1, 5')
	 * @return Returns ArrayList of DatabaseEntries
	 */
	protected static DatabaseEntryCollection GetManyByField(DatabaseEntryCollection collection, Callable new_entry, String table, @Nullable String[] fields, @Nullable Object[] values, @Nullable String order_by, @Nullable String limit) {
		String query = "SELECT * FROM %s";

		if (fields != null && values != null && fields.length > 0 && values.length > 0) {
			query += " WHERE %s";

			ArrayList<String> conditions = new ArrayList<>();

			for (int i = 0; i < fields.length; i++) {
				System.out.println("Value " + fields[i] + ": " + (values[i] == null ? "NULL" : values[i] + " (" + values[i].getClass() + ")"));
				if (values[i] == null)
					conditions.add("\"" + fields[i] + "\" IS NULL");
				else if (values[i].getClass() == int.class || values[i].getClass() == double.class || values[i].getClass() == long.class || values[i].getClass() == short.class)
					conditions.add("\"" + fields[i] + "\" = " + values[i]);
				else if (values[i].getClass() == boolean.class)
					conditions.add("\"" + fields[i] + "\" = " + ((boolean) values[i] ? "1" : "0"));
				else
					conditions.add("\"" + fields[i] + "\" = '" + values[i] + "'");
			}
			query = String.format(query, table, String.join(" AND ", conditions));
		} else {
			query = String.format(query, table);
		}

		if (order_by != null)
			query += " ORDER BY \"" + order_by + "\"";
		if (limit != null)
			query += " LIMIT " + limit;

		try {
			System.out.println("Query: " + query);
			ResultSet result = Database.statement.executeQuery(query);

			int counter = 0;
			while (result.next()) {
				counter++;
				DatabaseEntry entry = (DatabaseEntry) new_entry.call();
				for (Field myField : entry.getClass().getDeclaredFields()) {
					if (!ignoreField(myField.getName())) {
						if (myField.getType() == boolean.class)
							myField.set(entry, result.getBoolean(myField.getName()));
						else if (myField.getType() == String.class)
							myField.set(entry, result.getString(myField.getName()));
						else if (myField.getType() == int.class)
							myField.set(entry, result.getInt(myField.getName()));
						else if (myField.getType() == float.class)
							myField.set(entry, result.getFloat(myField.getName()));
						else if (myField.getType() == double.class)
							myField.set(entry, result.getDouble(myField.getName()));
						else if (myField.getType() == DateTime.class)
							myField.set(entry, result.getDate(myField.getName()));
					}
				}
				entry.stored_item = true;
				collection.AddItem(entry);
			}
			System.out.println("Parsed " + counter + " result(s), Items: " + collection.items.size());
		} catch (Exception e) {
			IRCBot.log.error("An error occurred in DatabaseEntry.GetManyByField");
			e.printStackTrace();
		}
		return collection;
	}

	public static ArrayList<String> GetUniqueByField(String table, String field) {
		String query = "SELECT DISTINCT \"" + field + "\" FROM \"" + table + "\";";

		ArrayList<String> results = new ArrayList<>();
		try {
			ResultSet resultSet = Database.statement.executeQuery(query);
			while (resultSet.next()) {
				results.add(resultSet.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return results;
	}
}
