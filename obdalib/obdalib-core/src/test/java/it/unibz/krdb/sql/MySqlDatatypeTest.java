package it.unibz.krdb.sql;

public class MySqlDatatypeTest extends AbstractDatatypeTest {

	@Override
	protected String getConnectionPassword() {
		return "fish";
	}

	@Override
	protected String getConnectionString() {
		return "jdbc:mysql://10.7.20.39/datatypes";
	}

	@Override
	protected String getConnectionUsername() {
		return "fish";
	}

	@Override
	protected String getDriverName() {
		return "com.mysql.jdbc.Driver";
	}

	@Override
	protected String toString(String name) {
		return name;
	}
}
