package it.unibz.inf.ontop.model.term.functionsymbol.db;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;

import javax.annotation.Nullable;

public interface QueryContextSimplifiableFunctionSymbol extends FunctionSymbol {

    ImmutableTerm simplifyWithContext(ImmutableList<ImmutableTerm> terms, @Nullable QueryContext queryContext,
                                                TermFactory termFactory);
}
