package it.unibz.krdb.sql;

public class PgSqlMetadataInfoTest extends AbstractDbMetadataInfoTest {

	@Override
	protected String getConnectionPassword() {
		return "postgres";
	}

	@Override
	protected String getConnectionString() {
		return "jdbc:postgresql://10.7.20.39/datatypes";
	}

	@Override
	protected String getConnectionUsername() {
		return "postgres";
	}

	@Override
	protected String getDriverName() {
		return "org.postgresql.Driver";
	}
}
