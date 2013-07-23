package it.unibz.krdb.sql;

public class PgSqlConstraintTest extends AbstractConstraintTest {

	public PgSqlConstraintTest(String method) {
		super(method);
	}

	@Override
	protected String getConnectionPassword() {
		return "postgres";
	}

	@Override
	protected String getConnectionString() {
		return "jdbc:postgresql://10.7.20.39/dbconstraints";
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
