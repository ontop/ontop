package it.unibz.inf.ontop.model.atom;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

/**
 * TODO: explain
 *
 *  Most of the time, does not provide any type for its arguments
 */
public interface AtomPredicate extends Predicate {


    /**
     * TODO: get rid of it after splitting predicates and functional symbols
     *
     * Is expected to return false
     */
    @Override
    @Deprecated
    boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables);

}
