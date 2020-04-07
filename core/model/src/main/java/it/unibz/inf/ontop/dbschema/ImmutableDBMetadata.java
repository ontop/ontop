package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;

public interface ImmutableDBMetadata {
    /**
     * Retrieves the tables list form the metadata.
     */
    ImmutableList<DatabaseRelationDefinition> getDatabaseRelations();

    DBParameters getDBParameters();
}
