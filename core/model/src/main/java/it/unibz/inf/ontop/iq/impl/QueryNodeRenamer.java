package it.unibz.inf.ontop.iq.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;

import it.unibz.inf.ontop.iq.transform.node.DefaultQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.*;

/**
 * Renames query nodes according to one renaming substitution.
 */
public class QueryNodeRenamer extends DefaultQueryNodeTransformer {

    private final InjectiveSubstitution<Variable> renamingSubstitution;

    public QueryNodeRenamer(IntermediateQueryFactory iqFactory, InjectiveSubstitution<Variable> renamingSubstitution) {
        super(iqFactory);
        this.renamingSubstitution = renamingSubstitution;
    }

    @Override
    public FilterNode transform(FilterNode filterNode, UnaryIQTree tree) {
        return filterNode.applyFreshRenaming(renamingSubstitution);
    }

    @Override
    public ExtensionalDataNode transform(ExtensionalDataNode extensionalDataNode) {
        return extensionalDataNode.applyFreshRenaming(renamingSubstitution);
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode leftJoinNode, BinaryNonCommutativeIQTree tree) {
        return leftJoinNode.applyFreshRenaming(renamingSubstitution);
    }

    @Override
    public UnionNode transform(UnionNode unionNode, NaryIQTree tree) {
        return unionNode.applyFreshRenaming(renamingSubstitution);
    }

    @Override
    public IntensionalDataNode transform(IntensionalDataNode intensionalDataNode) {
        return intensionalDataNode.applyFreshRenaming(renamingSubstitution);
    }

    @Override
    public InnerJoinNode transform(InnerJoinNode innerJoinNode, NaryIQTree tree) {
        return innerJoinNode.applyFreshRenaming(renamingSubstitution);
    }

    @Override
    public ConstructionNode transform(ConstructionNode constructionNode, UnaryIQTree tree) {
        return constructionNode.applyFreshRenaming(renamingSubstitution);
    }

    @Override
    public AggregationNode transform(AggregationNode aggregationNode, UnaryIQTree tree) {
        return aggregationNode.applyFreshRenaming(renamingSubstitution);
    }

    @Override
    public FlattenNode transform(FlattenNode flattenNode, UnaryIQTree tree) {
        return flattenNode.applyFreshRenaming(renamingSubstitution);
    }

    @Override
    public EmptyNode transform(EmptyNode emptyNode) {
        return emptyNode.applyFreshRenaming(renamingSubstitution);
    }

    @Override
    public TrueNode transform(TrueNode trueNode) {
        return trueNode.applyFreshRenaming(renamingSubstitution);
    }

    @Override
    public ValuesNode transform(ValuesNode valuesNode) {
        return valuesNode.applyFreshRenaming(renamingSubstitution);
    }

    @Override
    public DistinctNode transform(DistinctNode distinctNode, UnaryIQTree tree) {
        return distinctNode.applyFreshRenaming(renamingSubstitution);
    }

    @Override
    public SliceNode transform(SliceNode sliceNode, UnaryIQTree tree) {
        return sliceNode.applyFreshRenaming(renamingSubstitution);
    }

    @Override
    public OrderByNode transform(OrderByNode orderByNode, UnaryIQTree tree) {
        return orderByNode.applyFreshRenaming(renamingSubstitution);
    }
}
