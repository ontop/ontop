package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQ;

import javax.annotation.Nonnull;

/**
 * Ontop view definitions are temporarily mutable, until their IQs are stabilized.
 */
public interface OntopViewDefinition extends NamedRelationDefinition {

    /**
     * IQ defining views from lower-level relations
     */
    IQ getIQ();

    /**
     * Level must be at least 1 for an Ontop view (0 refers to database relations)
     *
     * A level 1 view is defined from database relations only
     *
     * A Level 2 view is from at least an Ontop view of level 1 and none of higher level.
     *
     */
    int getLevel();

    /**
     * If called after freezing, throw an IllegalStateException
     */
    void updateIQ(@Nonnull IQ newIQ) throws IllegalStateException;

    /**
     * After freezing the IQ cannot be changed anymore.
     */
    void freeze();

}
