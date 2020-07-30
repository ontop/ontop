package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.request.DefinitionPushDownRequest;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm.FunctionalTermDecomposition;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLAggregationFunctionSymbol.AggregationSimplification;

public class AggregationSimplificationImpl implements AggregationSimplification {

    private final FunctionalTermDecomposition decomposition;
    private final ImmutableSet<DefinitionPushDownRequest> pushDownRequests;

    public AggregationSimplificationImpl(FunctionalTermDecomposition decomposition,
                                         ImmutableSet<DefinitionPushDownRequest> pushDownRequests) {
        this.decomposition = decomposition;
        this.pushDownRequests = pushDownRequests;
    }

    @Override
    public FunctionalTermDecomposition getDecomposition() {
        return decomposition;
    }

    @Override
    public ImmutableSet<DefinitionPushDownRequest> getPushDownRequests() {
        return pushDownRequests;
    }
}
