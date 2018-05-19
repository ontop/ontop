package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Optional;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.AND;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.IS_NOT_NULL;


public class NoNullValuesEnforcerImpl implements NoNullValueEnforcer {


    private final IntermediateQueryFactory iQFactory;
    private final TermFactory termFactory;

    @Inject
    private NoNullValuesEnforcerImpl(IntermediateQueryFactory iQFactory, TermFactory termFactory) {
        this.iQFactory = iQFactory;
        this.termFactory = termFactory;
    }

    @Override
    public IQ transform(IQ originalQuery) {
        IQTree tree = originalQuery.getTree();
        ImmutableSet<ImmutableSet<Variable>> nullableGroups = tree.getVariableNullability().getNullableGroups();

        return nullableGroups.isEmpty() ?
                originalQuery :
                insertFilter(originalQuery, nullableGroups);
    }


    private ImmutableExpression computeFilterExpression(ImmutableSet<ImmutableSet<Variable>> nullableGroups) {
        return nullableGroups.stream()
                .map(g -> g.stream().findFirst())
                .map(Optional::get)
                .map(v -> termFactory.getImmutableExpression(IS_NOT_NULL, v))
                .reduce(null, (a, b) -> (a == null) ? b : termFactory.getImmutableExpression(AND, a, b));
    }

    private IQ insertFilter(IQ originalQuery, ImmutableSet<ImmutableSet<Variable>> nullableVariables) {
        FilterNode filterNode = iQFactory.createFilterNode(computeFilterExpression(nullableVariables));
        UnaryIQTree newTree = iQFactory.createUnaryIQTree(filterNode, originalQuery.getTree());

        // TODO: normalize it
        return iQFactory.createIQ(originalQuery.getProjectionAtom(), newTree);
    }
}
