package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.MetadataLookup;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TrinoDBMetadataProvider extends DefaultSchemaCatalogDBMetadataProvider {

    @AssistedInject
    TrinoDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, metadata -> new TrinoQuotedIDFactory(), coreSingletons);
                // current_catalog AS TABLE_CAT
                //"SELECT current_schema AS TABLE_SCHEM");
        // https://www.postgresql.org/docs/current/functions-info.html
        // CAREFUL: PostgreSQL uses a chain of schemas and goes through the list until it finds the relevant object
        // https://www.postgresql.org/docs/current/ddl-schemas.html
        // If you write a database name, it must be the same as the database you are connected to.
    }

    private static final ImmutableList<String> IGNORED_SCHEMAS = ImmutableList.of("information_schema");
    private static final ImmutableList<String> IGNORED_CATALOGS = ImmutableList.of( "system");

    @Override
    protected boolean isRelationExcluded(RelationID id) {
        return IGNORED_SCHEMAS.contains(getRelationSchema(id)) || IGNORED_CATALOGS.contains(getRelationCatalog(id));
    }


    @Override
    public void insertIntegrityConstraints(NamedRelationDefinition relation, MetadataLookup metadataLookup) throws MetadataExtractionException {
        /* Trino does not support the extraction of integrity constraints.
           Furthermore, running the method `insertUniqueConstraints` throws an exception, because it accesses
           the method `getIndex` on the Trino MetaData which is not supported by the Trino JDBC.
           Therefore, we skip this method
        */
    }
}
