
package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.dbschema.RelationID.TABLE_INDEX;

public class TeiidDBMetadataProvider extends AbstractDBMetadataProvider {

    @AssistedInject
    TeiidDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, metadata -> new TeiidQuotedIDFactory(), coreSingletons);
    }

    @Override
    protected RelationID getCanonicalRelationId(RelationID id) { return id; }

    @Override
    protected ImmutableList<RelationID> getAllIDs(RelationID id) { return ImmutableList.of(id); }



    @Override
    protected RelationID getRelationID(ResultSet rs, String catalogNameColumn, String schemaNameColumn, String tableNameColumn) throws SQLException {
        String[] schemaComponents = rs.getString(schemaNameColumn).split("\\.");
        String[] components = Arrays.copyOf(schemaComponents, schemaComponents.length + 1);
        components[schemaComponents.length] = rs.getString(tableNameColumn);
        return rawIdFactory.createRelationID(components);
    }

    @Override
    protected String getRelationCatalog(RelationID id) {
        String catalog = id.getComponents().toString();
        return null;
    }

    @Override
    protected String getRelationSchema(RelationID id) {
        String rs = id.getComponents().subList(1, id.getComponents().size()).reverse().stream()
                .map(QuotedID::getName) // IMPORTANT: no quotation marks!
                .collect(Collectors.joining("."));
        return rs;
    }

    @Override
    protected String getRelationName(RelationID id) {
        String rn = id.getComponents().get(TABLE_INDEX).getName();
        return rn;
    }

}