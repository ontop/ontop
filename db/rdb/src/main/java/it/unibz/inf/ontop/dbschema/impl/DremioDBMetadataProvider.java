package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.exception.RelationNotFoundInMetadataException;
import it.unibz.inf.ontop.injection.CoreSingletons;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static it.unibz.inf.ontop.dbschema.RelationID.TABLE_INDEX;

public class DremioDBMetadataProvider extends AbstractDBMetadataProvider {

    private final String defaultSchemaComponent;

    private static final int SCHEMA_INDEX = 1;

    @AssistedInject
    DremioDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, metadata -> new DremioQuotedIDFactory(), coreSingletons);

        String localdefaultSchemaComponent = null;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT CURRENT_SCHEMA AS TABLE_SCHEM")) {
            rs.next();
            localdefaultSchemaComponent = rs.getString("TABLE_SCHEM");
        }
        catch (SQLException e) {
            /* NO-OP */
        }
        defaultSchemaComponent = localdefaultSchemaComponent;
    }

    @Override
    protected RelationID getCanonicalRelationId(RelationID id) {
        if (defaultSchemaComponent == null || id.getComponents().size() > SCHEMA_INDEX)
            return id;

        return rawIdFactory.createRelationID(defaultSchemaComponent, id.getComponents().get(TABLE_INDEX).getName());
    }

    @Override
    protected ImmutableList<RelationID> getAllIDs(RelationID id) {
        return hasDefaultSchema(id)
                ? ImmutableList.of(id, id.getTableOnlyID())
                : ImmutableList.of(id);
    }

    private boolean hasDefaultSchema(RelationID id) {
        if (defaultSchemaComponent == null || id.getComponents().size() <= SCHEMA_INDEX)
            return false;

        return id.getComponents().get(SCHEMA_INDEX).getName().equals(defaultSchemaComponent);
    }

    @Override
    public NamedRelationDefinition getRelation(RelationID id0) throws MetadataExtractionException {
        try {
            return super.getRelation(id0);
        }
        catch (RelationNotFoundInMetadataException e) {
            //In case the metadata is not loaded yet, we run a simple query to force Dremio to load it.
            try (Statement st = connection.createStatement()) {
                st.execute("SELECT * FROM " + id0.getSQLRendering() + " WHERE 1 = 0");
            }
            catch (SQLException ex) {
                throw new MetadataExtractionException(ex);
            }
            return super.getRelation(id0);
        }
    }

    @Override
    protected RelationID getRelationID(ResultSet rs, String catalogNameColumn, String schemaNameColumn, String tableNameColumn) throws SQLException {
        return rawIdFactory.createRelationID(rs.getString(schemaNameColumn), rs.getString(tableNameColumn));
    }

    @Override
    protected String getRelationCatalog(RelationID id) { return null; }

    @Override
    protected String getRelationSchema(RelationID id) {
        return  id.getComponents().get(SCHEMA_INDEX).getName();
    }

    @Override
    protected String getRelationName(RelationID id) {
        return id.getComponents().get(TABLE_INDEX).getName();
    }

}