package it.unibz.inf.ontop.model.term;

import com.google.common.collect.ImmutableList;

/**
 * Immutable ground functional term.
 */
public interface GroundFunctionalTerm extends ImmutableFunctionalTerm, GroundTerm {

    @Override
    ImmutableList<? extends GroundTerm> getTerms();

}
