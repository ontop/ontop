package it.unibz.inf.ontop.iq.transform.node;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.IQTreeVisitingNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

public class DefaultQueryNodeTransformer implements QueryNodeTransformer {
    protected final IntermediateQueryFactory iqFactory;

    public DefaultQueryNodeTransformer(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    public IQVisitor<IQTree> treeTransformer() {
        return new IQTreeVisitingNodeTransformer(this, iqFactory);
    }

    @Override
    public FilterNode transform(FilterNode filterNode, UnaryIQTree tree) {
        return filterNode;
    }

    @Override
    public ExtensionalDataNode transform(ExtensionalDataNode extensionalDataNode) {
        return extensionalDataNode;
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode leftJoinNode, BinaryNonCommutativeIQTree tree) {
        return leftJoinNode;
    }

    @Override
    public UnionNode transform(UnionNode unionNode, NaryIQTree tree) {
        return unionNode;
    }

    @Override
    public IntensionalDataNode transform(IntensionalDataNode intensionalDataNode) {
        return intensionalDataNode;
    }

    @Override
    public InnerJoinNode transform(InnerJoinNode innerJoinNode, NaryIQTree tree) {
        return innerJoinNode;
    }

    @Override
    public ConstructionNode transform(ConstructionNode constructionNode, UnaryIQTree tree) {
        return constructionNode;
    }

    @Override
    public AggregationNode transform(AggregationNode aggregationNode, UnaryIQTree tree) {
        return aggregationNode;
    }

    @Override
    public FlattenNode transform(FlattenNode flattenNode, UnaryIQTree tree) {
        return flattenNode;
    }

    @Override
    public EmptyNode transform(EmptyNode emptyNode) {
        return emptyNode;
    }

    @Override
    public TrueNode transform(TrueNode trueNode) {
        return trueNode;
    }

    @Override
    public ValuesNode transform(ValuesNode valuesNode) {
        return valuesNode;
    }

    @Override
    public DistinctNode transform(DistinctNode distinctNode, UnaryIQTree tree) {
        return distinctNode;
    }

    @Override
    public SliceNode transform(SliceNode sliceNode, UnaryIQTree tree) {
        return sliceNode;
    }

    @Override
    public OrderByNode transform(OrderByNode orderByNode, UnaryIQTree tree) {
        return orderByNode;
    }
}
