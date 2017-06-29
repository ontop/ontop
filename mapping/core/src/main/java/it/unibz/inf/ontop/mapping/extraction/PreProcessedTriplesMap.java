package it.unibz.inf.ontop.mapping.extraction;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;

/**
 * TODO: describe
 */
public interface PreProcessedTriplesMap {

    ImmutableList<ImmutableFunctionalTerm> getTargetAtoms();

    PPMappingAssertionProvenance getProvenance(ImmutableFunctionalTerm targetAtom);
}
