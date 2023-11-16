package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.optimizer.AllQueryContextFunctionSymbolEvaluator;
import it.unibz.inf.ontop.model.term.functionsymbol.db.QueryContextSimplifiableFunctionSymbol;

@Singleton
public class AllQueryContextFunctionSymbolEvaluatorImpl extends AbstractQueryContextEvaluator implements AllQueryContextFunctionSymbolEvaluator {

    @Inject
    protected AllQueryContextFunctionSymbolEvaluatorImpl(CoreSingletons coreSingletons) {
        super(coreSingletons, fs -> (fs instanceof QueryContextSimplifiableFunctionSymbol));
    }
}
