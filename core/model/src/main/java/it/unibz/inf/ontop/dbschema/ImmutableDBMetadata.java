package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;

public interface ImmutableDBMetadata {
    /**
     * Retrieves the tables list form the metadata.
     */
    ImmutableList<RelationDefinition> getDatabaseRelations();

    /**
     * Retrieves the data definition object based on its name.
     *
     * @param id
     */
    RelationDefinition getRelation(RelationID id);

    DBParameters getDBParameters();
}
