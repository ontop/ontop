package it.unibz.inf.ontop.model.term.functionsymbol.db;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;

import javax.annotation.Nonnull;

public interface QueryContextSimplifiableFunctionSymbol extends FunctionSymbol {

    /**
     * This simplification is applied only in the presence of the query context.
     * No query context, no simplification.
     */
    ImmutableTerm simplifyWithContext(ImmutableList<ImmutableTerm> terms, @Nonnull QueryContext queryContext,
                                      TermFactory termFactory);
}
