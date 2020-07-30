package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.Connection;

public class DB2DBMetadataProvider extends DefaultDBMetadataProvider {

    private final ImmutableSet<String> ignoredSchemas = ImmutableSet.of("SYSTOOLS", "SYSCAT", "SYSIBM", "SYSIBMADM", "SYSSTAT");
    private final QuotedID defaultSchema;

    @AssistedInject
    DB2DBMetadataProvider(@Assisted Connection connection, TypeFactory typeFactory) throws MetadataExtractionException {
        super(connection, typeFactory);
        // https://www.ibm.com/support/knowledgecenter/en/SSEPGG_11.5.0/com.ibm.db2.luw.sql.ref.doc/doc/r0005881.html
        // https://www.ibm.com/support/knowledgecenter/en/SSEPGG_11.5.0/com.ibm.db2.luw.sql.ref.doc/doc/r0000720.html
        defaultSchema = retrieveDefaultSchema("select CURRENT SCHEMA  from  SYSIBM.SYSDUMMY1");
    }

    @Override
    public QuotedID getDefaultSchema() {
        return defaultSchema;
    }

    @Override
    protected boolean isSchemaIgnored(String schema) { return ignoredSchemas.contains(schema); }


    /*
    // Alternative solution for DB2 to print column names
        String sqlQuery = String.format("SELECT colname, typename \n FROM SysCat.Columns \n" +
                "WHERE tabname = '%s' AND tabschema = '%s'", tableName, tableSchema);

			return "SELECT TABSCHEMA, TABNAME " +
			       "FROM SYSCAT.TABLES " +
			       "WHERE OWNERTYPE='U' AND (TYPE='T' OR TYPE='V') " +
			       "     AND TBSPACEID IN (SELECT TBSPACEID FROM SYSCAT.TABLESPACES WHERE TBSPACE LIKE 'USERSPACE%')";
    */
}
