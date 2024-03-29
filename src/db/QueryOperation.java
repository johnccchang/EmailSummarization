package db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

public interface QueryOperation {
	public Object insert(String table, ArrayList<HashMap<String, String>> params) throws SQLException, ParseException, InterruptedException;
	public int delete(String table, String cond) throws SQLException;
	public ResultSet query(String table, HashMap<String, String> params) throws SQLException;
	public int update(String table, String cols, String cond) throws SQLException;
}
