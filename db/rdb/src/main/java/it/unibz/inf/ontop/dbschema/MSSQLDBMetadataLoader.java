package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableSet;

import java.sql.Connection;

public class MSSQLDBMetadataLoader extends JDBCRDBMetadataLoader {

    private final ImmutableSet<String> ignoreSchema = ImmutableSet.of("sys", "INFORMATION_SCHEMA");

    MSSQLDBMetadataLoader(Connection connection, QuotedIDFactory idFactory) {
        super(connection, idFactory);
    }

    // SELECT SCHEMA_NAME() would give default schema name
    // https://msdn.microsoft.com/en-us/library/ms175068.aspx

    @Override
    protected boolean ignoreSchema(String schema) {
        return ignoreSchema.contains(schema);
    }

    /*
	private static final RelationListProvider MSSQLServerRelationListProvider = new RelationListProvider() {
		@Override
		public String getQuery() {
			return "SELECT TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME " +
					"FROM INFORMATION_SCHEMA.TABLES " +
					"WHERE TABLE_TYPE='BASE TABLE' OR TABLE_TYPE='VIEW'";
		}

		@Override
		public RelationID getTableID(ResultSet rs) throws SQLException {
			//String tblCatalog = rs.getString("TABLE_CATALOG");
			return RelationID.createRelationIdFromDatabaseRecord(rs.getString("TABLE_SCHEMA"), rs.getString("TABLE_NAME"));
		}
	};
*/
}
