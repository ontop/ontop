package it.unibz.krdb.sql;

public class OracleConstraintTest extends AbstractConstraintTest {

	public OracleConstraintTest(String method) {
		super(method);
	}

	@Override
	protected String getConnectionPassword() {
		return "obdaps83";
	}

	@Override
	protected String getConnectionString() {
		return "jdbc:oracle:thin:@//10.7.20.91:1521/xe";
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
