package it.unibz.inf.ontop.model.term.functionsymbol.db;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;

public interface DBIfElseNullFunctionSymbol extends DBIfThenFunctionSymbol {

    /**
     * Returns BOOL_IF_ELSE_NULL(c, f(v)) where f is an unary BooleanFunctionSymbol, and c and v and the initial terms
     */
    ImmutableExpression liftUnaryBooleanFunctionSymbol(ImmutableList<? extends ImmutableTerm> ifElseNullTerms,
                                                       BooleanFunctionSymbol unaryBooleanFunctionSymbol,
                                                       TermFactory termFactory);
}
