package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class DremioDBMetadataProvider extends DefaultDBMetadataProvider {

    @AssistedInject
    DremioDBMetadataProvider(@Assisted Connection connection, TypeFactory typeFactory) throws MetadataExtractionException {
        super(connection, new DefaultSchemaProvider() {
            @Override
            public String getSchema() { return null; }
            @Override
            public String getCatalog() { return null; }
        }, typeFactory);
    }

    @Override
    protected RelationID getCanonicalRelationId(RelationID id) {
        return id;
    }

    protected RelationID getRelationID(ResultSet rs) throws SQLException {
        return getRelationID(rs, "TABLE_SCHEM","TABLE_NAME");
    }

    protected RelationID getPKRelationID(ResultSet rs) throws SQLException {
        return getRelationID(rs, "PKTABLE_SCHEM","PKTABLE_NAME");
    }

    protected RelationID getFKRelationID(ResultSet rs) throws SQLException {
        return getRelationID(rs, "FKTABLE_SCHEM","FKTABLE_NAME");
    }

    private RelationID getRelationID(ResultSet rs, String schemaNameColumn, String tableNameColumn) throws SQLException {
        String[] components = rs.getString(schemaNameColumn).split("\\.");
        String[] allComponents = Arrays.copyOf(components, components.length + 1);
        allComponents[components.length] = rs.getString(tableNameColumn);
        return rawIdFactory.createRelationID(allComponents);
    }

}