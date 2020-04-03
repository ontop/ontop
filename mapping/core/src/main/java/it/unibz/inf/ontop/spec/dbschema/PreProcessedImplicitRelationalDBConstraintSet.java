package it.unibz.inf.ontop.spec.dbschema;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;

import java.util.Set;

/**
 * TODO: refactor these methods into an immutable style
 */
public interface PreProcessedImplicitRelationalDBConstraintSet {

    /**
     * Extracts relation IDs for all relations referred to by the user supplied foreign keys
     * (but not the relations of the foreign keys)
     *
     * @return relation ids that are referred to by foreign keys
     */
    ImmutableList<RelationID> getRelationIDs();

    /**
     *
     * Inserts the user-supplied primary keys / unique constraints columns into the metadata object
     * Inserts the user-supplied foreign keys into the metadata object
     */
    void insertIntegrityConstraints(DBMetadata md);
}
