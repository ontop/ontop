package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.Connection;

public class SQLServerDBMetadataProvider extends DefaultDBMetadataProvider {

    @AssistedInject
    SQLServerDBMetadataProvider(@Assisted Connection connection, TypeFactory typeFactory) throws MetadataExtractionException {
        super(connection, new QueryBasedDefaultSchemaProvider("SELECT DB_NAME()", "SELECT SCHEMA_NAME()"), typeFactory);
        // https://msdn.microsoft.com/en-us/library/ms175068.aspx
        // https://docs.microsoft.com/en-us/sql/t-sql/functions/schema-name-transact-sql
        // https://docs.microsoft.com/en-us/sql/t-sql/functions/db-name-transact-sql
    }

    private static final ImmutableSet<String> IGNORED_SCHEMAS = ImmutableSet.of("sys", "INFORMATION_SCHEMA");

    @Override
    protected boolean isRelationExcluded(RelationID id) {
        return IGNORED_SCHEMAS.contains(getRelationSchema(id));
    }

    /*       return "SELECT TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME " +
					"FROM INFORMATION_SCHEMA.TABLES " +
					"WHERE TABLE_TYPE='BASE TABLE' OR TABLE_TYPE='VIEW'";
    */
}
