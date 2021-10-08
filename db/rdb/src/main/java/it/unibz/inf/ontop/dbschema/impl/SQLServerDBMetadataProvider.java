package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;

import javax.annotation.Nullable;
import java.sql.Connection;

public class SQLServerDBMetadataProvider extends DefaultSchemaCatalogDBMetadataProvider {

    @AssistedInject
    SQLServerDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, metadata -> new SQLServerQuotedIDFactory(), coreSingletons);
    }

    private static final ImmutableSet<String> IGNORED_SCHEMAS = ImmutableSet.of("sys", "INFORMATION_SCHEMA");

    @Override
    protected boolean isRelationExcluded(RelationID id) {
        return IGNORED_SCHEMAS.contains(getRelationSchema(id));
    }

    @Override
    protected @Nullable String escapeRelationIdComponentPattern(@Nullable String s) {
        return s;
    }

    @Override
    protected String makeQueryMinimizeResultSet(String query) {
        return String.format("SELECT * FROM (%s) subQ ORDER BY (SELECT NULL) OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY", query);
    }

    /*
                "SELECT DB_NAME() AS TABLE_CAT, SCHEMA_NAME() AS TABLE_SCHEM");
        https://msdn.microsoft.com/en-us/library/ms175068.aspx
        https://docs.microsoft.com/en-us/sql/t-sql/functions/schema-name-transact-sql
        https://docs.microsoft.com/en-us/sql/t-sql/functions/db-name-transact-sql

          return "SELECT TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME " +
					"FROM INFORMATION_SCHEMA.TABLES " +
					"WHERE TABLE_TYPE='BASE TABLE' OR TABLE_TYPE='VIEW'";
    */
}
