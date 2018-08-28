package pcl.lc.utils;

import com.google.api.client.util.DateTime;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
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

	private static boolean ignoreField(String field) {
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
		String query = "INSERT OR REPLACE INTO %s (%s) VALUES (%s);";

		ArrayList<String> fieldArray = new ArrayList<>();
		ArrayList<String> valueArray = new ArrayList<>();
		for (Field field : this.getClass().getDeclaredFields()) {
			if (!ignoreField(field.getName())) {
				fieldArray.add("'" + field.getName() + "'");
				try {
					if (field.getType() == int.class && field.getInt(this) == Integer.MIN_VALUE)
						valueArray.add("null");
					else if (field.get(this) == null)
						valueArray.add("null");
					else if (field.getType() == boolean.class)
						valueArray.add((field.getBoolean(this) ? "1" : "0"));
					else if (field.getType() == int.class)
						valueArray.add(String.valueOf(field.getInt(this)));
					else if (field.getType() == double.class)
						valueArray.add(String.valueOf(field.getDouble(this)));
					else if (field.getType() == long.class)
						valueArray.add(String.valueOf(field.getLong(this)));
					else if (field.getType() == short.class)
						valueArray.add(String.valueOf(field.getShort(this)));
					else
						valueArray.add("'" + field.get(this) + "'");
				} catch (Exception e) {
					valueArray.add("null");
					e.printStackTrace();
				}
			}
		}
		String fields = String.join(", ", fieldArray);
		String values = String.join(", ", valueArray);
		System.out.println("Found fields: " + fields);
		System.out.println("Found values: " + values);

		query = String.format(query, table, fields, values);

		System.out.println("Query: " + query);

		try {
			Database.statement.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
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
			e.printStackTrace();
		}
		return false;
	}

	protected static DatabaseEntry GetByField(DatabaseEntry output, String table, String field, Object value) {
		String query = "SELECT * FROM %s WHERE %s = %s";
		String str_value;

		if (value == null)
			str_value = "null";
		else if (value.getClass() == int.class)
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
				return output;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param new_entry Should be something like MyEntry::new, used to create new instances of objects to return
	 * @param table Target table for query
	 * @param fields Array of fields to use in condition
	 * @param values Array of values used in condition, should contain the same amount as fields, conditions are all combined with AND
	 * @param order_by null to ignore, optional order by statement ('field_name, other_field_name' or 'RANDOM()')
	 * @param limit null to ignore, optional limit ('1' or '1, 5')
	 * @return Returns ArrayList of DatabaseEntries, if new_entry was set properly the list can be re-cast to the proper type without issue
	 */
	protected static ArrayList<DatabaseEntry> GetManyByField(Callable new_entry, String table, String[] fields, Object[] values, @Nullable String order_by, @Nullable String limit) {
		ArrayList<DatabaseEntry> collection = new ArrayList<>();

		String query = "SELECT * FROM %s WHERE %s";

		if (order_by != null)
			query += " ORDER BY " + order_by;
		if (limit != null)
			query += " LIMIT " + limit;

		ArrayList<String> conditions = new ArrayList<>();

		for (int i = 0; i < fields.length; i++) {
			System.out.println("Value " + fields[i] + ": " + (values[i] == null ? "NULL" : values[i] + " (" + values[i].getClass() + ")"));
			if (values[i] == null)
				conditions.add(fields[i] + " IS NULL");
			else if (values[i].getClass() == int.class || values[i].getClass() == double.class || values[i].getClass() == long.class || values[i].getClass() == short.class)
				conditions.add(fields[i] + " = " + values[i]);
			else if (values[i].getClass() == boolean.class)
				conditions.add(fields[i] + " = " + ((boolean) values[i] ? "1" : "0"));
			else
				conditions.add(fields[i] + " = '" + values[i] + "'");
		}

		query = String.format(query, table, String.join(" AND ", conditions));

		try {
			System.out.println("Query: " + query);
			ResultSet result = Database.statement.executeQuery(query);

			while (result.next()) {
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
				collection.add(entry);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return collection;
	}
}
