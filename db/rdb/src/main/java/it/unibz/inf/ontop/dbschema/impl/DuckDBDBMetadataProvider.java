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

public class DuckDBDBMetadataProvider extends DefaultSchemaDBMetadataProvider {

    @AssistedInject
    DuckDBDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, metadata -> new DuckDBQuotedIDFactory(), coreSingletons);
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
