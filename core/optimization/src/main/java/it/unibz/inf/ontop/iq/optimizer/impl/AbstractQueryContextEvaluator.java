package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.QueryContextEvaluator;
import it.unibz.inf.ontop.iq.type.impl.AbstractExpressionTransformer;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.QueryContextSimplifiableFunctionSymbol;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

        var transformer = new QueryContextFunctionTransformer(queryContext, coreSingletons, functionSymbolPredicate);

        var initialTree = iq.getTree();
        var newTree = initialTree.acceptTransformer(transformer);
        return newTree.equals(initialTree)
                ? iq
                : coreSingletons.getIQFactory().createIQ(iq.getProjectionAtom(), newTree);
    }


    protected static class QueryContextFunctionTransformer extends AbstractExpressionTransformer {

        private final QueryContext queryContext;
        private final Predicate<FunctionSymbol> functionSymbolPredicate;

        protected QueryContextFunctionTransformer(QueryContext queryContext, CoreSingletons coreSingletons,
                                                  Predicate<FunctionSymbol> functionSymbolPredicate) {
            super(coreSingletons.getIQFactory(), coreSingletons.getUniqueTermTypeExtractor(), coreSingletons.getTermFactory());
            this.queryContext = queryContext;
            this.functionSymbolPredicate = functionSymbolPredicate;
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
