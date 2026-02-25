package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

import static it.unibz.inf.ontop.iq.impl.UnaryIQTreeTools.UnaryIQTreeDecomposition;

/*
Used when an ORDER BY node accesses expressions that are defined in a CONSTRUCT above it. Some dialects (like GoogleSQL) do
not support that, so instead we push the CONSTRUCT down into the ORDER BY so the ORDER BY can use the variables defined by
the CONSTRUCT.
Generally, an `AlwaysProjectOrderByTerms` normalizer is expected to be run before calling this normalizer.
 */

/*public*/ final class PushProjectedOrderByTermsTransformer extends DefaultRecursiveIQTreeVisitingTransformer {
    private final IQTreeTools iqTreeTools;
    private final boolean onlyDistinct;

    PushProjectedOrderByTermsTransformer(boolean onlyDistinct, CoreSingletons coreSingletons) {
        super(coreSingletons.getIQFactory());
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.onlyDistinct = onlyDistinct;
    }

    @Override
    public IQTree transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
        var construction = UnaryIQTreeDecomposition.of(child, ConstructionNode.class);
        var orderBy = UnaryIQTreeDecomposition.of(construction, OrderByNode.class);

        return transform(
                Optional.of(rootNode),
                construction.getOptionalNode(),
                orderBy.getOptionalNode(),
                orderBy.getTail())
                .orElse(tree);
    }

    @Override
    public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
        if (onlyDistinct)
            return super.transformConstruction(tree, rootNode, child);

        var orderBy = UnaryIQTreeDecomposition.of(child, OrderByNode.class);

        return transform(
                Optional.empty(),
                Optional.of(rootNode),
                orderBy.getOptionalNode(),
                orderBy.getTail())
                .orElse(tree);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    Optional<IQTree> transform(Optional<DistinctNode> distinctNode, Optional<ConstructionNode> constructionNode, Optional<OrderByNode> orderByNode, IQTree descendantTree) {

        var newDescendantTree = transform(descendantTree);

        if (!(constructionNode.isPresent() && orderByNode.isPresent())) {
            return descendantTree.equals(newDescendantTree)
                    ? Optional.empty()
                    : Optional.of(iqTreeTools.unaryIQTreeBuilder()
                    .append(distinctNode)
                    .append(constructionNode)
                    .append(orderByNode)
                    .build(newDescendantTree));
        }

        var newOrderBy = liftOrderBy(constructionNode.get(), orderByNode.get());

        //Change order from [DISTINCT] -> CONSTRUCT -> ORDER BY to [DISTINCT] -> ORDER BY -> CONSTRUCT
        return Optional.of(iqTreeTools.unaryIQTreeBuilder()
                .append(distinctNode)
                .append(newOrderBy)
                .append(constructionNode)
                .build(newDescendantTree));
    }

    OrderByNode liftOrderBy(ConstructionNode construction, OrderByNode orderBy) {

        var substitution = construction.getSubstitution();
        ImmutableSet<ImmutableTerm> rangeSet = substitution.getRangeSet();

        //Get map of terms used in ORDER BY that are defined in CONSTRUCT
        ImmutableMap<NonGroundTerm, NonGroundTerm> definedInConstruct = orderBy.getComparators().stream()
                .map(OrderByNode.OrderComparator::getTerm)
                .filter(rangeSet::contains)
                .distinct()
                .collect(ImmutableCollectors.toMap(
                        term -> term,
                        term -> substitution.getPreImage(t -> t.equals(term)).stream()
                                .findFirst()
                                .orElseThrow(() -> new MinorOntopInternalBugException(
                                        "Was expecting a definition with value " + term))));

        //Define new ORDER BY node that uses variables from CONSTRUCT instead, where possible
        return iqFactory.createOrderByNode(iqTreeTools.transformComparators(
                orderBy.getComparators(), t -> definedInConstruct.getOrDefault(t, t)));
    }
}
