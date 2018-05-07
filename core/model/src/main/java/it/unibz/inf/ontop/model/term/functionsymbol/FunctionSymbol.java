package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * FunctionSymbols are the functors needed to build ImmutableFunctionalTerms
 */
public interface FunctionSymbol extends Predicate {

    boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables);

}
