package it.unibz.krdb.sql;

public class MssqlDatatypeTest extends AbstractDatatypeTest {

	@Override
	protected String getConnectionPassword() {
		return "obdaps83";
	}

	@Override
	protected String getConnectionString() {
		return "jdbc:sqlserver://obdawin.unibz.it;databaseName=datatypes";
	}

	@Override
	protected String getConnectionUsername() {
		return "mssql";
	}

	@Override
	protected String getDriverName() {
		return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	}

	@Override
	protected String toString(String name) {
		return name;
	}
}
