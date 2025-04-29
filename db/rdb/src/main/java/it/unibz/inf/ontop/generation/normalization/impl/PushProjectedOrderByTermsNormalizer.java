package it.unibz.inf.ontop.generation.normalization.impl;

import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

/*
Used when an ORDER BY node accesses expressions that are defined in a CONSTRUCT above it. Some dialects (like GoogleSQL) do
not support that, so instead we push the CONSTRUCT down into the ORDER BY so the ORDER BY can use the variables defined by
the CONSTRUCT.
Generally, an `AlwaysProjectOrderByTerms` normalizer is expected to be run before calling this normalizer.
 */
public class PushProjectedOrderByTermsNormalizer implements DialectExtraNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final boolean onlyDistinct;

    protected PushProjectedOrderByTermsNormalizer(boolean onlyDistinct,
                                                  CoreSingletons coreSingletons) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.onlyDistinct = onlyDistinct;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptTransformer(new Transformer());
    }

    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        Transformer() {
            super(PushProjectedOrderByTermsNormalizer.this.iqFactory);
        }

        @Override
        public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
            var decomposition = ProjectOrderByTermsNormalizer.Decomposition.decomposeTree(tree);
            if (decomposition.constructionNode.isEmpty() || decomposition.distinctNode.isEmpty() || decomposition.orderByNode.isEmpty()) {
                var newDescendantTree = transform(decomposition.descendantTree);
                return decomposition.descendantTree.equals(newDescendantTree)
                        ? tree
                        : decomposition.rebuildWithNewDescendantTree(newDescendantTree, iqFactory);
            }
            return normalizeWithDistinct(decomposition);
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
            if (onlyDistinct)
                return super.transformConstruction(tree, rootNode, child);

            var decomposition = ProjectOrderByTermsNormalizer.Decomposition.decomposeTree(tree);
            if(decomposition.constructionNode.isEmpty() || decomposition.orderByNode.isEmpty()) {
                var newDescendantTree = transform(decomposition.descendantTree);
                return decomposition.descendantTree.equals(newDescendantTree)
                        ? tree
                        : decomposition.rebuildWithNewDescendantTree(newDescendantTree, iqFactory);
            }
            return normalize(decomposition);
        }

        IQTree normalizeWithDistinct(ProjectOrderByTermsNormalizer.Decomposition decomposition) {
            var distinct = decomposition.distinctNode.get();

            return iqFactory.createUnaryIQTree(distinct, normalize(decomposition));
        }

        IQTree normalize(ProjectOrderByTermsNormalizer.Decomposition decomposition) {
            var construct = decomposition.constructionNode.get();
            var orderBy = decomposition.orderByNode.get();
            var remainingSubtree = transform(decomposition.descendantTree);

            var substitution = construct.getSubstitution();

            //Get map of terms used in ORDER BY that are defined in CONSTRUCT
            var orderByTerms = orderBy.getComparators().stream()
                    .map(OrderByNode.OrderComparator::getTerm)
                    .collect(ImmutableCollectors.toSet());

            var definedInConstruct = orderByTerms.stream()
                    .filter(term -> substitution.getRangeSet().contains(term))
                    .collect(ImmutableCollectors.toMap(
                            term -> term,
                            term -> (NonGroundTerm) substitution.getDomain().stream()
                                    .filter(t -> substitution.get(t).equals(term))
                                    .findFirst()
                                    .orElseThrow(() -> new MinorOntopInternalBugException(
                                            "Was expecting a definition with value " + term))));

            //Define new ORDER BY node that uses variables from CONSTRUCT instead, where possible
            var newOrderBy = iqFactory.createOrderByNode(orderBy.getComparators().stream()
                    .map(comp -> iqFactory.createOrderComparator(definedInConstruct
                            .getOrDefault(comp.getTerm(), comp.getTerm()), comp.isAscending()))
                    .collect(ImmutableCollectors.toList()));

            //Change order from DISTINCT -> CONSTRUCT -> ORDER BY to DISTINCT -> ORDER BY -> CONSTRUCT
            var newOrderBySubtree = iqFactory.createUnaryIQTree(construct, remainingSubtree);
            return iqFactory.createUnaryIQTree(newOrderBy, newOrderBySubtree);
        }
    }
}
