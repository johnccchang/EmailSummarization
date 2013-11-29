package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import date.DateTime;

public class MySQL extends Database implements QueryAction {
	Connection dbCon = null;
	Statement stmt = null;
	PreparedStatement prestmt = null;
	ResultSet rs = null;
	ResultSetMetaData metadata = null;
	HashMap<String, Integer> data_type = new HashMap<String, Integer>();
	
	public MySQL() {
		try {
			// The newInstance() call is a work around for some
			// broken Java implementations
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			setDataType();
		} catch (Exception ex) {
			// handle any errors

		}
	}
	public static boolean isNotNullNotEmptyNotWhiteSpace(final String string) {
		return string != null && !string.isEmpty() && !string.trim().isEmpty();
	}
	
	

	private static java.sql.Timestamp getCurrentTimeStamp() {
		java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(today.getTime());

	}
	private void setDataType() {
		data_type.put("INT", 1);
		data_type.put("FLOAT", 2);
		data_type.put("DOUBLE", 3);
		data_type.put("VARCHAR", 4);
		data_type.put("TEXT", 5);
		data_type.put("DATE", 6);
		data_type.put("TIME", 7);
		data_type.put("TIMESTAMP", 8);
		data_type.put("DATETIME", 9);
		data_type.put("BLOB", 10);
	}
	private String getDBURL(String ip, String port, String db) {
		this.ip = ip;
		this.port = port;
		this.db = db;
		return "jdbc:mysql://" + ip + ":" + port + "/" + db;
	}
	@Override
	public boolean connect(String ip, String port, String db, String username, String password) {
		// getting database connection to MySQL server
		try {
			this.username = username;
			this.password = password;
			// create database connection
			dbCon = DriverManager
					.getConnection(getDBURL(ip, port, db), username, password);
			if (dbCon != null) return true;
			else return false;

		} catch (SQLException e) {
			// handle any errors
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		}
		return false;
	}

	@Override
	public void close() {
		try {
			stmt.close();
			prestmt.close();
			rs.close();
			dbCon.close();
			System.out.println("db close!");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Object insert(String table, HashMap<String, String> params) throws SQLException, ParseException {
			// implement a way to batch update multiple records into database
			// commit trasaction manually
			dbCon.setAutoCommit(false);
			HashMap<String, String> attrs = getColumnSet(table);
			String strSQL = generateSQLStatement("insert", table, attrs);
			prestmt       = dbCon.prepareStatement(strSQL);
			// set values into preparedStatement
			assignParameters(prestmt, attrs, params);

			prestmt.executeBatch();
			dbCon.commit();


		return null;
	}

	@Override
	public Object delete(ArrayList<Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object update(ArrayList<Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object query(ArrayList<Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	private String generateSQLStatement(String type, String table, HashMap<String, String> attrs) {
		String strSQL = "";
		if (type == "insert") {
			strSQL = "Insert Into " + table + " (";
			for (String attr : attrs.keySet()) strSQL += attr + ", ";
			strSQL = strSQL.substring(0, (strSQL.length() - 2)) + ") Values (";
			for (String attr : attrs.keySet()) strSQL += "?, ";
			strSQL = strSQL.substring(0, (strSQL.length() - 2)) + ")";
		}
		return strSQL;
	}

	private HashMap<String, String> getColumnSet(String table) {
		HashMap<String, String> attrs = new HashMap<String, String>();
		try {
			// Create a result set
			stmt = dbCon.createStatement();
			rs = stmt.executeQuery("SELECT * FROM " + table);

			// Get resultset metadata
			metadata = rs.getMetaData();

			int columnCount = metadata.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				if (metadata.isAutoIncrement(i)) continue; // skip the field with auto-increment
				String name = metadata.getColumnName(i);
				String type = metadata.getColumnTypeName(i);
				
				attrs.put(name, type);
			}
			return attrs;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void assignParameters(PreparedStatement prestmt, HashMap<String, String> attrs, HashMap<String, String> params) throws ParseException {
		try {
			int index = 0; 
			
			for (String column_name : attrs.keySet()) {
				index++;
				String type  = attrs.get(column_name);
				String value = params.get(column_name);
				switch (data_type.get(type)) {
				case 1: // INT
					if (isNotNullNotEmptyNotWhiteSpace(value)) prestmt.setInt(index, Integer.valueOf(value));
					else prestmt.setInt(index, 0);
					break;
				case 2: // FLOAT
					if (isNotNullNotEmptyNotWhiteSpace(value)) prestmt.setFloat(index, Float.valueOf(value));
					else prestmt.setFloat(index, 0);
					break;
				case 4: // VARCHAR
				case 5: // TEXT
					prestmt.setString(index, value);
					break;
				case 8: // TIMESTAMP
					prestmt.setTimestamp(index, getCurrentTimeStamp());
					break;
				case 9: // DATETIME
					prestmt.setTimestamp(index, java.sql.Timestamp.valueOf(value));
					break;
				}
			}
			prestmt.addBatch();
			System.out.println(prestmt);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
