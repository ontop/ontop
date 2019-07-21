package it.unibz.inf.ontop.model.term.functionsymbol.db;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;

import java.util.stream.Stream;

/**
 * Abstraction for CASE, IF-ELSE-NULL and so on
 */
public interface DBIfThenFunctionSymbol extends DBFunctionSymbol {

    Stream<ImmutableTerm> extractPossibleValues(ImmutableList<? extends ImmutableTerm> arguments);

    /**
     * Pushes a unary boolean function symbol down to the "then" arguments
     *
     * For instance, f(CASE(c1, t1, c2, t2, t3)) -> IS_TRUE(CASE(c1, f(t1), c2, f(t2), f(t3)))
     */
    ImmutableExpression pushDownUnaryBoolean(ImmutableList<? extends ImmutableTerm> arguments,
                                             BooleanFunctionSymbol unaryBooleanFunctionSymbol,
                                             TermFactory termFactory);
}
