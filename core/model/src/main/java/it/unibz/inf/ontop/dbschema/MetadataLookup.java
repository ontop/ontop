package it.unibz.inf.ontop.dbschema;

import java.util.Optional;

public interface MetadataLookup {

    /**
     * Retrieves the data definition object based on its name.
     *
     * @param id
     */

    Optional<RelationDefinition> getRelation(RelationID id);

}
