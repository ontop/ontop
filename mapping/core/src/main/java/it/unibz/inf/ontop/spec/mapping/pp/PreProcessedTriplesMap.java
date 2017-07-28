package it.unibz.inf.ontop.spec.mapping.pp;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;

/**
 * TODO: describe
 */
public interface PreProcessedTriplesMap {

    ImmutableList<ImmutableFunctionalTerm> getTargetAtoms();

    PPMappingAssertionProvenance getMappingAssertionProvenance(ImmutableFunctionalTerm targetAtom);

    PPTriplesMapProvenance getTriplesMapProvenance();
}
