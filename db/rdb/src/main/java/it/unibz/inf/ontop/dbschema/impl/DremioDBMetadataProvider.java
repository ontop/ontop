package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.dbschema.RelationID.TABLE_INDEX;

public class DremioDBMetadataProvider extends AbstractDBMetadataProvider {

    @AssistedInject
    DremioDBMetadataProvider(@Assisted Connection connection, TypeFactory typeFactory) throws MetadataExtractionException {
        super(connection, metadata -> new DremioQuotedIDFactory(), typeFactory);
    }

    @Override
    protected RelationID getCanonicalRelationId(RelationID id) { return id; }

    @Override
    protected ImmutableList<RelationID> getAllIDs(RelationID id) { return ImmutableList.of(id); }

    @Override
    public NamedRelationDefinition getRelation(RelationID id0) throws MetadataExtractionException {
        try (Statement st = connection.createStatement()) {
            st.execute("SELECT * FROM " + id0.getSQLRendering() + " WHERE 1 = 0");
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
        return super.getRelation(id0);
    }

    @Override
    protected RelationID getRelationID(ResultSet rs, String catalogNameColumn, String schemaNameColumn, String tableNameColumn) throws SQLException {
        String[] schemaComponents = rs.getString(schemaNameColumn).split("\\.");
        String[] components = Arrays.copyOf(schemaComponents, schemaComponents.length + 1);
        components[schemaComponents.length] = rs.getString(tableNameColumn);
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