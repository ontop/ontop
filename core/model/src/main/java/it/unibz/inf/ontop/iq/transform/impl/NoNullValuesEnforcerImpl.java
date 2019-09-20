package it.unibz.inf.ontop.iq.transform.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;

import java.util.Optional;


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

        Optional<ImmutableExpression> condition = termFactory.getConjunction(
                tree.getVariables().stream()
                        .map(termFactory::getDBIsNotNull));

        IQTree newTree = condition
                .map(iQFactory::createFilterNode)
                .map(n -> iQFactory.createUnaryIQTree(n, tree))
                .map(t -> t.normalizeForOptimization(originalQuery.getVariableGenerator()))
                .orElse(tree);

        return newTree.equals(tree)
                ? originalQuery
                : iQFactory.createIQ(originalQuery.getProjectionAtom(), newTree);
    }
}
