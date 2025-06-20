package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.QueryContextEvaluator;
import it.unibz.inf.ontop.iq.type.impl.AbstractTermTransformer;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.QueryContextSimplifiableFunctionSymbol;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class AbstractQueryContextEvaluator implements QueryContextEvaluator {

    private final CoreSingletons coreSingletons;
    private final Predicate<FunctionSymbol> functionSymbolPredicate;

    protected AbstractQueryContextEvaluator(CoreSingletons coreSingletons,
                                            Predicate<FunctionSymbol> functionSymbolPredicate) {
        this.coreSingletons = coreSingletons;
        this.functionSymbolPredicate = functionSymbolPredicate;
    }

    @Override
    public IQ optimize(IQ iq, @Nonnull QueryContext queryContext) {
        if (queryContext == null)
            throw new IllegalArgumentException("The query context must not be null");

        var initialTree = iq.getTree();
        var newTree = initialTree.acceptVisitor(new QueryContextFunctionTransformer(queryContext));
        return newTree.equals(initialTree)
                ? iq
                : coreSingletons.getIQFactory().createIQ(iq.getProjectionAtom(), newTree);
    }


    protected class QueryContextFunctionTransformer extends AbstractTermTransformer {

        private final QueryContext queryContext;

        protected QueryContextFunctionTransformer(QueryContext queryContext) {
            super(coreSingletons);
            this.queryContext = queryContext;
        }

        @Override
        protected boolean isFunctionSymbolToReplace(FunctionSymbol functionSymbol) {
            return (functionSymbol instanceof QueryContextSimplifiableFunctionSymbol)
                    && functionSymbolPredicate.test(functionSymbol);
        }

        @Override
        protected ImmutableFunctionalTerm replaceFunctionSymbol(FunctionSymbol functionSymbol,
                                                                ImmutableList<ImmutableTerm> newTerms, IQTree tree) {
            ImmutableTerm newTerm = ((QueryContextSimplifiableFunctionSymbol) functionSymbol).simplifyWithContext(newTerms,
                    queryContext, termFactory);
            if (newTerm instanceof ImmutableFunctionalTerm)
                return (ImmutableFunctionalTerm) newTerm;
            return termFactory.getIdentityFunctionalTerm(newTerm);
        }
    }
}
