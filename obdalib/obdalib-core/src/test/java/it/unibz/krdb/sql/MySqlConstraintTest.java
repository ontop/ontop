package it.unibz.krdb.sql;

public class MySqlConstraintTest extends AbstractConstraintTest {

	public MySqlConstraintTest(String method) {
		super(method);
	}

	@Override
	protected String getConnectionPassword() {
		return "fish";
	}

	@Override
	protected String getConnectionString() {
		return "jdbc:mysql://10.7.20.39/dbconstraints";
	}

	@Override
	protected String getConnectionUsername() {
		return "fish";
	}

	@Override
	protected String getDriverName() {
		return "com.mysql.jdbc.Driver";
	}
}
