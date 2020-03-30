package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;

import java.sql.SQLException;

public interface RDBMetadataLoader {
    ImmutableList<RelationID> getRelationIDs() throws SQLException;

    ImmutableList<RelationID> getRelationIDs(ImmutableList<RelationID> seed);

    RelationDefinition.AttributeListBuilder getRelationAttributes(RelationID relationID);

    void insertIntegrityConstraints(RelationDefinition relation);
}
