package it.unibz.inf.ontop.model.term.functionsymbol.db;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;

import java.util.stream.Stream;

/**
 * Abstraction for CASE, IF-ELSE-NULL and so on
 */
public interface DBIfThenFunctionSymbol extends DBFunctionSymbol {

    Stream<ImmutableTerm> extractPossibleValues(ImmutableList<? extends ImmutableTerm> arguments);

    /**
     * Pushes a unary boolean function symbol down to the "then" arguments
     *
     * For instance,
     * {@code f(CASE(c1, t1, c2, t2, t3), t4) -> BOOL_CASE(c1, f(t1,t4), c2, f(t2,t4), f(t3,t4))}
     */
    ImmutableExpression pushDownExpression(ImmutableExpression expression,
                                           int indexOfDBIfThenFunctionSymbol,
                                           TermFactory termFactory);

    /**
     * Similar to pushDownExpression(...) but for regular functional terms
     */
    ImmutableFunctionalTerm pushDownRegularFunctionalTerm(ImmutableFunctionalTerm functionalTerm,
                                                          int indexOfDBIfThenFunctionSymbol,
                                                          TermFactory termFactory);
}
