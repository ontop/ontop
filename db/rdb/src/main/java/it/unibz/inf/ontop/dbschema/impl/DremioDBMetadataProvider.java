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
import java.util.Arrays;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.dbschema.RelationID.TABLE_INDEX;

public class DremioDBMetadataProvider extends AbstractDBMetadataProvider {

    private final String[] defaultSchemaComponents;

    @AssistedInject
    DremioDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, metadata -> new DremioQuotedIDFactory(), coreSingletons);

        String[] localDefaultSchemaComponents = null;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT CURRENT_SCHEMA AS TABLE_SCHEM")) {
            rs.next();
            localDefaultSchemaComponents = rs.getString("TABLE_SCHEM").split("\\.");
        }
        catch (SQLException e) {
            /* NO-OP */
        }
        defaultSchemaComponents = localDefaultSchemaComponents;
    }

    @Override
    protected RelationID getCanonicalRelationId(RelationID id) {
        if (id.getComponents().size() > 1 || defaultSchemaComponents == null)
            return id;

        return createRelationID(
                defaultSchemaComponents,
                id.getComponents().get(TABLE_INDEX).getName());
    }

    @Override
    protected ImmutableList<RelationID> getAllIDs(RelationID id) {
        return hasDefaultSchema(id)
                ? ImmutableList.of(id, id.getTableOnlyID())
                : ImmutableList.of(id);
    }

    private boolean hasDefaultSchema(RelationID id) {
        if (defaultSchemaComponents == null
                || id.getComponents().size() != defaultSchemaComponents.length + 1)
            return false;

        for (int i = id.getComponents().size() - 1; i > 0; i--)
            if (!id.getComponents().get(i).getName()
                    .equals(defaultSchemaComponents[defaultSchemaComponents.length - i]))
                return false;

        return true;
    }

    @Override
    public NamedRelationDefinition getRelation(RelationID id0) throws MetadataExtractionException {
        try {
            return super.getRelation(id0);
        }
        catch (RelationNotFoundInMetadataException e) {
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
        return createRelationID(
                rs.getString(schemaNameColumn).split("\\."),
                rs.getString(tableNameColumn));
    }

    private RelationID createRelationID(String[] schemaComponents, String tableName) {
        String[] components = Arrays.copyOf(schemaComponents, schemaComponents.length + 1);
        components[schemaComponents.length] = tableName;
        return rawIdFactory.createRelationID(components);
    }

    @Override
    protected String getRelationCatalog(RelationID id) { return null; }

    @Override
    protected String getRelationSchema(RelationID id) {
        return id.getComponents().subList(1, id.getComponents().size()).reverse().stream()
                .map(QuotedID::getName) // IMPORTANT: no quotation marks!
                .collect(Collectors.joining("."));
    }

    @Override
    protected String getRelationName(RelationID id) {
        return id.getComponents().get(TABLE_INDEX).getName();
    }
}