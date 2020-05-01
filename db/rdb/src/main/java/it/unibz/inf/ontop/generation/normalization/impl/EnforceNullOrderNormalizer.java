package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

public class EnforceNullOrderNormalizer implements DialectExtraNormalizer {

    private final IQTreeVisitingTransformer transformer;

    @Inject
    protected EnforceNullOrderNormalizer(IntermediateQueryFactory iqFactory,
                                         TermFactory termFactory) {
        transformer = new EnforceNullOrderIQTreeVisitingTransformer(iqFactory, termFactory);
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return transformer.transform(tree);
    }


    protected static class EnforceNullOrderIQTreeVisitingTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final TermFactory termFactory;

        protected EnforceNullOrderIQTreeVisitingTransformer(IntermediateQueryFactory iqFactory, TermFactory termFactory) {
            super(iqFactory);
            this.termFactory = termFactory;
        }

        @Override
        public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {
            VariableNullability variableNullability = child.getVariableNullability();
            ImmutableList<OrderByNode.OrderComparator> conditions = rootNode.getComparators().stream()
                    .flatMap(c -> extendCondition(c, variableNullability))
                    .collect(ImmutableCollectors.toList());

            return iqFactory.createUnaryIQTree(
                    iqFactory.createOrderByNode(conditions),
                    child.acceptTransformer(this));
        }

        /**
         * Tries to append a IS_NOT_NULL order condition before so as to enforce NULL as the smallest value
         *
         * NB: we don't simplify the expression in case a DISTINCT is present.
         *
         */
        private Stream<OrderByNode.OrderComparator> extendCondition(OrderByNode.OrderComparator condition,
                                                                    VariableNullability variableNullability) {
            ImmutableExpression isNotNullCondition = termFactory.getDBIsNotNull(condition.getTerm());

            Optional<OrderByNode.OrderComparator> additionalCondition = Optional.of(isNotNullCondition)
                    .filter(e -> e.evaluate(variableNullability).getExpression().isPresent())
                    .map(e -> iqFactory.createOrderComparator((NonGroundTerm) e, condition.isAscending()));

            return additionalCondition
                    .map(c -> Stream.of(c, condition))
                    .orElseGet(() -> Stream.of(condition));
        }
    }


}
