package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.iq.IQ;

import javax.annotation.Nonnull;

/**
 * Lenses are temporarily mutable, until their IQs are stabilized.
 */
public interface Lens extends NamedRelationDefinition {

    /**
     * IQ defining views from lower-level relations
     */
    IQ getIQ();

    /**
     * Level must be at least 1 for a lens (0 refers to database relations)
     *
     * A level 1 lens is defined from database relations only
     *
     * A Level 2 lens is from at least a lens of level 1 and none of higher level.
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
