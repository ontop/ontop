package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DenodoMetadataProvider extends DefaultSchemaDBMetadataProvider {
    @AssistedInject
    DenodoMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, DefaultDBMetadataProvider::getQuotedIDFactory, coreSingletons,
                 c -> new String[]{ c.getCatalog(), "DUMMY" });
    }

    @Override
    protected String getRelationCatalog(RelationID relationID) { return super.getRelationSchema(relationID); }

    @Override
    protected String getRelationSchema(RelationID relationID) { return null; }

    @Override
    protected RelationID getRelationID(ResultSet rs, String catalogNameColumn, String schemaNameColumn, String tableNameColumn) throws SQLException {
        return rawIdFactory.createRelationID(rs.getString(catalogNameColumn), rs.getString(tableNameColumn));
    }
}
