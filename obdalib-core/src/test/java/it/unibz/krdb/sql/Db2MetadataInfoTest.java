package it.unibz.krdb.sql;

public class Db2MetadataInfoTest extends AbstractDbMetadataInfoTest {

	@Override
	protected String getConnectionPassword() {
		return "fish";
	}

	@Override
	protected String getConnectionString() {
		return "jdbc:db2://10.7.20.39:50000/datatypes";
	}

	@Override
	protected String getConnectionUsername() {
		return "db2inst1";
	}

	@Override
	protected String getDriverName() {
		return "com.ibm.db2.jcc.DB2Driver";
	}
}
