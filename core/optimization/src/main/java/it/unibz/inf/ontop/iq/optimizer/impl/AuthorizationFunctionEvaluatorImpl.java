package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.optimizer.AuthorizationFunctionEvaluator;
import it.unibz.inf.ontop.model.term.functionsymbol.db.AuthorizationFunctionSymbol;

@Singleton
public class AuthorizationFunctionEvaluatorImpl extends AbstractQueryContextEvaluator implements AuthorizationFunctionEvaluator {

    @Inject
    protected AuthorizationFunctionEvaluatorImpl(CoreSingletons coreSingletons) {
        super(coreSingletons, fs -> (fs instanceof AuthorizationFunctionSymbol));
    }
}
