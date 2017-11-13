package it.unibz.inf.ontop.spec.mapping.pp;

import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;

/**
 * TODO: describe
 */
public interface PreProcessedTriplesMap {

    PPMappingAssertionProvenance getMappingAssertionProvenance(ImmutableFunctionalTerm targetAtom);

    PPTriplesMapProvenance getTriplesMapProvenance();
}
