package it.unibz.inf.ontop.model.atom;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

/**
 * Immutable data atom that only accepts variables and ground terms as arguments.
 *
 * In the future, this class could be disassociated from the Function class.
 */
public interface DataAtom<P extends AtomPredicate> extends Function {

    P getPredicate();

    /**
     * Effective arity (number of sub-terms).
     */
    int getEffectiveArity();

    @Override
    VariableOrGroundTerm getTerm(int index);

    ImmutableList<? extends VariableOrGroundTerm> getArguments();

    @Override
    ImmutableSet<Variable> getVariables();

    boolean containsGroundTerms();
}
