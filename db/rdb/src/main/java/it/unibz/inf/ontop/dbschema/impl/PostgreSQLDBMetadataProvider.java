package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostgreSQLDBMetadataProvider extends DefaultSchemaDBMetadataProvider {

    @AssistedInject
    PostgreSQLDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, metadata -> new PostgreSQLQuotedIDFactory(), coreSingletons);
                // current_catalog AS TABLE_CAT
                //"SELECT current_schema AS TABLE_SCHEM");
        // https://www.postgresql.org/docs/current/functions-info.html
        // CAREFUL: PostgreSQL uses a chain of schemas and goes through the list until it finds the relevant object
        // https://www.postgresql.org/docs/current/ddl-schemas.html
        // If you write a database name, it must be the same as the database you are connected to.
    }

    @Override
    protected ResultSet getRelationIDsResultSet() throws SQLException {
        return metadata.getTables(null, null, null, getSettings().exposeSystemTables()
                ? new String[] { "TABLE", "VIEW", "MATERIALIZED VIEW", "SYSTEM TABLE", "SYSTEM VIEW", "TEMPORARY TABLE" }
                : new String[] { "TABLE", "VIEW", "MATERIALIZED VIEW"});

    }

    private static final ImmutableList<String> IGNORED_SCHEMAS = ImmutableList.of( "_timescaledb_cache", "_timescaledb_catalog",
            "_timescaledb_internal", "_timescaledb_config");

    @Override
    protected boolean isRelationExcluded(RelationID id) {
        return IGNORED_SCHEMAS.contains(getRelationSchema(id));
    }
}
