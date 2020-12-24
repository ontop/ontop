package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.iq.IQ;

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

}
