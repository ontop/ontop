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

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static it.unibz.inf.ontop.dbschema.RelationID.TABLE_INDEX;

public class DremioDBMetadataProvider extends AbstractDBMetadataProvider {

    @Nullable // the default schema is optional
    private final QuotedID defaultSchema;

    private static final int SCHEMA_INDEX = 1;

    @AssistedInject
    DremioDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, metadata -> new DremioQuotedIDFactory(), coreSingletons);

        QuotedID localDefaultSchema = null;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT CURRENT_SCHEMA AS TABLE_SCHEM")) {
            rs.next();
            String defaultSchemaName = rs.getString("TABLE_SCHEM");
            RelationID id = rawIdFactory.createRelationID(defaultSchemaName, "DUMMY");
            localDefaultSchema = id.getComponents().get(SCHEMA_INDEX);
        }
        catch (SQLException e) {
            /* NO-OP */
        }
        defaultSchema = localDefaultSchema;
    }

    @Override
    protected RelationID getCanonicalRelationId(RelationID id) {
        return (defaultSchema == null || id.getComponents().size() > SCHEMA_INDEX)
            ? id
            : new RelationIDImpl(ImmutableList.of(id.getComponents().get(TABLE_INDEX), defaultSchema));
    }

    @Override
    protected ImmutableList<RelationID> getAllIDs(RelationID id) {
        return hasDefaultSchema(id)
                ? ImmutableList.of(id, id.getTableOnlyID())
                : ImmutableList.of(id);
    }

    private boolean hasDefaultSchema(RelationID id) {
        if (defaultSchema == null || id.getComponents().size() < SCHEMA_INDEX + 1)
            return false;

        return id.getComponents().get(SCHEMA_INDEX).equals(defaultSchema);
    }

    @Override
    public NamedRelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
        try {
            return super.getRelation(id);
        }
        catch (RelationNotFoundInMetadataException e) {
            // In case the metadata is not loaded yet, we run a simple query to force Dremio to load it.
            try (Statement st = connection.createStatement()) {
                st.execute("SELECT * FROM " + id.getSQLRendering() + " WHERE 1 = 0");
            }
            catch (SQLException ex) {
                throw new MetadataExtractionException(ex);
            }
            return super.getRelation(id); // try again
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

    @Override
    protected ResultSet getRelationIDsResultSet() throws SQLException {
        return metadata.getTables(null, null, null, getSettings().exposeSystemTables()
                ? new String[] { "TABLE", "VIEW", "MATERIALIZED VIEW", "SYSTEM_TABLE", "SYSTEM_VIEW" }
                : new String[] { "TABLE", "VIEW", "MATERIALIZED VIEW"});

    }

}