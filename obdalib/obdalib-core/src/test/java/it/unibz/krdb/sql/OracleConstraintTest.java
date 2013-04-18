package it.unibz.krdb.sql;

public class OracleConstraintTest extends AbstractConstraintTest {

	@Override
	protected String getConnectionPassword() {
		return "obdaps83";
	}

	@Override
	protected String getConnectionString() {
		return "jdbc:oracle:thin:@//obdawin.unibz.it:1521/xe";
	}

	@Override
	protected String getConnectionUsername() {
		return "system";
	}

	@Override
	protected String getDriverName() {
		return "oracle.jdbc.OracleDriver";
	}
}
