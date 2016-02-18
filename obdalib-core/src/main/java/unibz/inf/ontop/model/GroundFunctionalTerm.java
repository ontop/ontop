package unibz.inf.ontop.model;

import com.google.common.collect.ImmutableList;

/**
 * Immutable ground functional term.
 */
public interface GroundFunctionalTerm extends ImmutableFunctionalTerm, GroundTerm {

    @Override
    ImmutableList<? extends GroundTerm> getArguments();

}
