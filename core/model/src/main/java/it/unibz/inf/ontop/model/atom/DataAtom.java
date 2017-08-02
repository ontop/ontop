package it.unibz.inf.ontop.model.atom;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

/**
 * Immutable data atom that only accepts variables and ground terms as arguments.
 *
 * In the future, this class could be disassociated from the Function class.
 */
public interface DataAtom extends ImmutableFunctionalTerm {

    AtomPredicate getPredicate();

    /**
     * Effective arity (number of sub-terms).
     */
    int getEffectiveArity();

    @Override
    VariableOrGroundTerm getTerm(int index);

    boolean hasSamePredicateAndArity(DataAtom otherAtom);

    @Override
    ImmutableList<? extends VariableOrGroundTerm> getArguments();

    boolean containsGroundTerms();
}
