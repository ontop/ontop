package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;

import java.sql.Connection;

public class JTDSDBMetadataProvider extends DefaultSchemaCatalogDBMetadataProvider {

    @AssistedInject
    JTDSDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, metadata -> new SQLServerQuotedIDFactory(), coreSingletons,
                "SELECT DB_NAME() AS TABLE_CAT, SCHEMA_NAME() AS TABLE_SCHEM");
        // jTDS driver does not properly implement .getSchema() - it throws an exception (unimplemented method)
        // https://msdn.microsoft.com/en-us/library/ms175068.aspx
        // https://docs.microsoft.com/en-us/sql/t-sql/functions/schema-name-transact-sql
        // https://docs.microsoft.com/en-us/sql/t-sql/functions/db-name-transact-sql
    }

    private static final ImmutableSet<String> IGNORED_SCHEMAS = ImmutableSet.of("sys", "INFORMATION_SCHEMA");

    @Override
    protected boolean isRelationExcluded(RelationID id) {
        return IGNORED_SCHEMAS.contains(getRelationSchema(id));
    }
}
