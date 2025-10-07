package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.transform.impl.DefaultDelegatingIQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transform.node.DefaultQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

public class EnforceNullOrderNormalizer extends DefaultDelegatingIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    protected EnforceNullOrderNormalizer(CoreSingletons coreSingletons) {
        super(new Transformer(coreSingletons).treeTransformer());
    }

    private static class Transformer extends DefaultQueryNodeTransformer {
        private final TermFactory termFactory;

        Transformer(CoreSingletons coreSingletons) {
            super(coreSingletons.getIQFactory());
            this.termFactory = coreSingletons.getTermFactory();
        }

        @Override
        public OrderByNode transform(OrderByNode rootNode, UnaryIQTree tree) {
            VariableNullability variableNullability = tree.getChild().getVariableNullability();
            return iqFactory.createOrderByNode(rootNode.getComparators().stream()
                    .flatMap(c -> extendCondition(c, variableNullability))
                    .collect(ImmutableCollectors.toList()));
        }

        /**
         * Tries to append an IS_NOT_NULL order condition before to enforce NULL as the smallest value
         *
         * NB: we don't simplify the expression in case a DISTINCT is present.
         *
         */
        private Stream<OrderByNode.OrderComparator> extendCondition(OrderByNode.OrderComparator condition,
                                                                    VariableNullability variableNullability) {
            ImmutableExpression isNotNullCondition = termFactory.getDBIsNotNull(condition.getTerm());

            Optional<OrderByNode.OrderComparator> additionalCondition = Optional.of(isNotNullCondition)
                    .filter(e -> e.evaluate(variableNullability).getExpression().isPresent())
                    .map(e -> (NonGroundTerm) e)
                    .map(t -> iqFactory.createOrderComparator(t, condition.isAscending()));

            return additionalCondition
                    .map(c -> Stream.of(c, condition))
                    .orElseGet(() -> Stream.of(condition));
        }
    }
}
