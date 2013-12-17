package it.unibz.krdb.sql;

public class MssqlMetadataInfoTest extends AbstractDbMetadataInfoTest {

	@Override
	protected String getConnectionPassword() {
		return "obdaps83";
	}

	@Override
	protected String getConnectionString() {
		return "jdbc:sqlserver://10.7.20.91;databaseName=datatypes";
	}

	@Override
	protected String getConnectionUsername() {
		return "mssql";
	}

	@Override
	protected String getDriverName() {
		return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	}
}
