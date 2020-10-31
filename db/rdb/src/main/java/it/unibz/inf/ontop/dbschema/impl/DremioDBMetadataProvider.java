package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    protected RelationID getRelationID(ResultSet rs, String catalogNameColumn, String schemaNameColumn, String tableNameColumn) throws SQLException {
        String[] components = rs.getString(schemaNameColumn).split("\\.");
        String[] allComponents = Arrays.copyOf(components, components.length + 1);
        allComponents[components.length] = rs.getString(tableNameColumn);
        System.out.println("DREMIOOO: " + Arrays.toString(allComponents));
        return rawIdFactory.createRelationID(allComponents);
    }

    @Override
    protected String getRelationCatalog(RelationID id) { return null; }

    @Override
    protected String getRelationSchema(RelationID id) {
        return id.getComponents().subList(TABLE_INDEX, id.getComponents().size()).reverse().stream()
                .map(QuotedID::getSQLRendering)
                .collect(Collectors.joining("."));
    }

    @Override
    protected String getRelationName(RelationID id) {
        return id.getComponents().get(TABLE_INDEX).getName();
    }

}