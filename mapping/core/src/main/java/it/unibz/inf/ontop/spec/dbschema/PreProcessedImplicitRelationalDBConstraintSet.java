package it.unibz.inf.ontop.spec.dbschema;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;

import java.util.Set;

/**
 * TODO: refactor these methods into an immutable style
 */
public interface PreProcessedImplicitRelationalDBConstraintSet extends PreProcessedImplicitDBConstraintSet {

    /**
     * Extracts relation IDs for all relations referred to by the user supplied foreign keys
     * (but not the relations of the foreign keys)
     *
     * @param idfac QuotedIDFactory
     * @return relation ids that are referred to by foreign keys
     */
    Set<RelationID> getReferredTables(QuotedIDFactory idfac);

    /**
     *
     * Inserts the user-supplied primary keys / unique constraints columns into the metadata object
     *
     * TODO: refactor into an immutable style
     *
     */
    void insertUniqueConstraints(DBMetadata md);

    /**
     *
     * Inserts the user-supplied foreign keys into the metadata object
     *
     * TODO: refactor into an immutable style
     */
    void insertForeignKeyConstraints(DBMetadata md);
}
