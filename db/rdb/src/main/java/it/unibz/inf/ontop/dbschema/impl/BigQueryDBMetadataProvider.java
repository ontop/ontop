package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import static it.unibz.inf.ontop.dbschema.RelationID.TABLE_INDEX;

public class BigQueryDBMetadataProvider extends DefaultSchemaCatalogDBMetadataProvider {

    @AssistedInject
    BigQueryDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, metadata -> new BigQueryQuotedIDFactory(), coreSingletons);
    }

    @Override
    protected ResultSet getRelationIDsResultSet() throws SQLException {
        return metadata.getTables(null, null, null, new String[] { "TABLE", "VIEW", "MATERIALIZED VIEW" });
    }

}
