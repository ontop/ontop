package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AthenaDBMetadataProvider extends TrinoDBMetadataProvider {

    @AssistedInject
    AthenaDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, coreSingletons);
    }

    /**
     * No filter on type (by default tables in Athena appears as "EXTERNAL_TABLE")
     */
    @Override
    protected ResultSet getRelationIDsResultSet() throws SQLException {
        return metadata.getTables(null, null, null, null);
    }
}
