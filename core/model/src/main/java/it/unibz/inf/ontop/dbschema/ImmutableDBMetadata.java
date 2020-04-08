package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;

public interface ImmutableDBMetadata extends MetadataLookup {

    /**
     * Retrieves the tables list form the metadata.
     */
    ImmutableList<RelationDefinition> getAllRelations();

    DBParameters getDBParameters();
}
