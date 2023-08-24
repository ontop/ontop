package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.AuthorizationFunctionEvaluator;
import it.unibz.inf.ontop.iq.type.impl.AbstractExpressionTransformer;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.AuthorizationFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BooleanAuthorizationFunctionSymbol;

import javax.annotation.Nullable;

@Singleton
public class AuthorizationFunctionEvaluatorImpl implements AuthorizationFunctionEvaluator {

    private final CoreSingletons coreSingletons;

    @Inject
    protected AuthorizationFunctionEvaluatorImpl(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
    }

    @Override
    public IQ optimize(IQ iq, @Nullable QueryContext queryContext) {
        var transformer = new AuthorizationFunctionTransformer(queryContext, coreSingletons);

        var initialTree = iq.getTree();
        var newTree = initialTree.acceptTransformer(transformer);
        return newTree.equals(initialTree)
                ? iq
                : coreSingletons.getIQFactory().createIQ(iq.getProjectionAtom(), newTree);
    }


    protected static class AuthorizationFunctionTransformer extends AbstractExpressionTransformer {

        @Nullable
        private final QueryContext queryContext;

        protected AuthorizationFunctionTransformer(@Nullable QueryContext queryContext, CoreSingletons coreSingletons) {
            super(coreSingletons.getIQFactory(), coreSingletons.getUniqueTermTypeExtractor(), coreSingletons.getTermFactory());
            this.queryContext = queryContext;
        }

        @Override
        protected boolean isFunctionSymbolToReplace(FunctionSymbol functionSymbol) {
            return (functionSymbol instanceof AuthorizationFunctionSymbol);
        }

        @Override
        protected ImmutableFunctionalTerm replaceFunctionSymbol(FunctionSymbol functionSymbol,
                                                                ImmutableList<ImmutableTerm> newTerms, IQTree tree) {
            if (functionSymbol instanceof BooleanAuthorizationFunctionSymbol) {
                return ((BooleanAuthorizationFunctionSymbol) functionSymbol).simplifyWithContext(
                        newTerms, queryContext, termFactory);
            }
            ImmutableTerm newTerm = ((AuthorizationFunctionSymbol) functionSymbol).simplifyWithContext(newTerms,
                    queryContext, termFactory);
            if (newTerm instanceof ImmutableFunctionalTerm)
                return (ImmutableFunctionalTerm) newTerm;
            return termFactory.getIdentityFunctionalTerm(newTerm);
        }
    }
}
