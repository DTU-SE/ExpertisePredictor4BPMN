package moderare.expertise.utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class DatabaseUtils {

	public static Map<String, Object> map(ResultSet rs) throws SQLException {
		ResultSetMetaData md = rs.getMetaData();
		int columns = md.getColumnCount();
		HashMap<String, Object> row = new HashMap<String, Object>();
		for (int i = 1; i <= columns; i++) {
			int columnType = md.getColumnType(i);
			if (columnType == Types.REAL || columnType == Types.TINYINT) {
				row.put(md.getColumnName(i), rs.getDouble(i));
			} else {
				row.put(md.getColumnName(i), rs.getString(i));
			}
		}
		return row;
	}
}
