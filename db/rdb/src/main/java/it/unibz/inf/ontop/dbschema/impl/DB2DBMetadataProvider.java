package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

import java.sql.Connection;

public class DB2DBMetadataProvider extends DefaultDBMetadataProvider {

    private final ImmutableSet<String> ignoreSchema = ImmutableSet.of("SYSTOOLS", "SYSCAT", "SYSIBM", "SYSIBMADM", "SYSSTAT");

    private final QuotedID defaultSchema;

    DB2DBMetadataProvider(Connection connection, DBTypeFactory dbTypeFactory) throws MetadataExtractionException {
        super(connection, dbTypeFactory);

        defaultSchema = retrieveDefaultSchema("select CURRENT SCHEMA  from  SYSIBM.SYSDUMMY1");
    }

    @Override
    public QuotedID getDefaultSchema() {
        return defaultSchema;
    }

    @Override
    protected boolean isSchemaIgnored(String schema) {
        return ignoreSchema.contains(schema);
    }
/*
    // Alternative solution for DB2 to print column names
    // Queries directly the system table SysCat.Columns
    //  ROMAN (20 Sep 2015): use PreparedStatement instead?
			try (Statement st = connection.createStatement()) {
        String sqlQuery = String.format("SELECT colname, typename \n FROM SysCat.Columns \n" +
                "WHERE tabname = '%s' AND tabschema = '%s'", tableName, tableSchema);

        try (ResultSet results = st.executeQuery(sqlQuery)) {
            while (results.next()) {
                log.debug("Column={} Type={}", results.getString("colname"), results.getString("typename"));
            }
        }
    }
*/
    /*
	private static final RelationListProvider DB2RelationListProvider = new RelationListProvider() {
		@Override
		public String getQuery() {
			return "SELECT TABSCHEMA, TABNAME " +
			       "FROM SYSCAT.TABLES " +
			       "WHERE OWNERTYPE='U' AND (TYPE='T' OR TYPE='V') " +
			       "     AND TBSPACEID IN (SELECT TBSPACEID FROM SYSCAT.TABLESPACES WHERE TBSPACE LIKE 'USERSPACE%')";
		}

		@Override
		public RelationID getTableID(ResultSet rs) throws SQLException {
			return RelationID.createRelationIdFromDatabaseRecord(rs.getString("TABSCHEMA"), rs.getString("TABNAME"));
		}
	};
*/

}
