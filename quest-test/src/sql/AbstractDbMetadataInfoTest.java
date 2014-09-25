package it.unibz.krdb.sql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDbMetadataInfoTest extends TestCase {
	
	private DBMetadata metadata;
	
	private static Logger log = LoggerFactory.getLogger(AbstractDbMetadataInfoTest.class);
	
	@Override
	public void setUp() {

		try {
			Connection conn = DriverManager.getConnection(getConnectionString(), getConnectionUsername(), getConnectionPassword());
			metadata = JDBCConnectionManager.getMetaData(conn);
		} catch (SQLException e) { 
			e.printStackTrace();
		}
	}
	
	public void testPropertyInfo() throws SQLException {
		final Driver driver = DriverManager.getDriver(getConnectionString());

		DriverPropertyInfo[] propInfo = null;
		try {
			propInfo = driver.getPropertyInfo(getConnectionString(), null);
		} catch (final RuntimeException err) {
			// Some drivers (Sun's ODBC-JDBC) throw null pointer exceptions ...
			// Try again, but with an empty properties ...
			try {
				propInfo = driver.getPropertyInfo(getConnectionString(), new Properties());
			} catch (final RuntimeException err2) {
				// Okay, give up
			}
		}

		for (final DriverPropertyInfo info : propInfo) {
			StringBuilder choices = new StringBuilder();
			if (info.choices != null) {
				choices.append("[");
				boolean needComma = false;
				for (String opt : info.choices) {
					if (needComma) {
						choices.append(", ");
					}
					choices.append(opt);
					needComma = true;
				}
				choices.append("]");
			}
			String msg = String.format("%s : %s : %s : %s : %s", info.name, info.value, choices.toString(), info.required, info.description);
			System.out.println(msg);
		}
	}
	
	protected abstract String getDriverName();
	protected abstract String getConnectionString();
	protected abstract String getConnectionUsername();
	protected abstract String getConnectionPassword();
}
