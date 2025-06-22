package it.unibz.inf.ontop.iq.type.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.node.DefaultQueryNodeTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;


public final class QueryNodeTransformerAdapter extends DefaultQueryNodeTransformer {

    private final IQTreeTools iqTreeTools;
    private final AbstractTermTransformer termTransformer;

    // this constructor is needed because some uses are in the "parts" of CoreSingletons,
    // which would introduce a cyclic dependency for Guice
    QueryNodeTransformerAdapter(IntermediateQueryFactory iqFactory,
                                IQTreeTools iqTreeTools,
                                AbstractTermTransformer termTransformer) {
        super(iqFactory);
        this.iqTreeTools = iqTreeTools;
        this.termTransformer = termTransformer;
    }

    @Override
    public ConstructionNode transform(ConstructionNode rootNode, UnaryIQTree tree) {
        return iqTreeTools.replaceSubstitution(rootNode,
                s -> s.transform(t -> termTransformer.transformTerm(t, tree.getChild())));
    }

    @Override
    public AggregationNode transform(AggregationNode rootNode, UnaryIQTree tree) {
        return iqFactory.createAggregationNode(
                rootNode.getGroupingVariables(),
                rootNode.getSubstitution()
                        .transform(t -> termTransformer.transformFunctionalTerm(t, tree.getChild())));
    }

    @Override
    public FilterNode transform(FilterNode rootNode, UnaryIQTree tree) {
        return iqFactory.createFilterNode(
                termTransformer.transformExpression(rootNode.getFilterCondition(), tree));
    }

    @Override
    public OrderByNode transform(OrderByNode rootNode, UnaryIQTree tree) {
        return iqFactory.createOrderByNode(rootNode.getComparators().stream()
                .map(c -> iqFactory.createOrderComparator(
                        termTransformer.transformNonGroundTerm(c.getTerm(), tree),
                        c.isAscending()))
                .collect(ImmutableCollectors.toList()));
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode rootNode, BinaryNonCommutativeIQTree tree) {
        return iqFactory.createLeftJoinNode(rootNode.getOptionalFilterCondition()
                        .map(e -> termTransformer.transformExpression(e, tree)));
    }

    @Override
    public InnerJoinNode transform(InnerJoinNode rootNode, NaryIQTree tree) {
        return iqFactory.createInnerJoinNode(
                rootNode.getOptionalFilterCondition()
                .map(e -> termTransformer.transformExpression(e, tree)));
    }
}
