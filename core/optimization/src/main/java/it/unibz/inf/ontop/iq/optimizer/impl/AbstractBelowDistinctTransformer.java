package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultNonRecursiveIQTreeTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;

import java.util.Optional;

/**
 * Transformer where the cardinality does matter for the current tree
 *
 */
public abstract class AbstractBelowDistinctTransformer extends DefaultNonRecursiveIQTreeTransformer {

    private final IQTreeTransformer lookForDistinctOrLimit1Transformer;
    protected final CoreSingletons coreSingletons;
    protected final IntermediateQueryFactory iqFactory;

    protected AbstractBelowDistinctTransformer(IQTreeTransformer lookForDistinctOrLimit1Transformer,
                                               CoreSingletons coreSingletons) {
        this.lookForDistinctOrLimit1Transformer = lookForDistinctOrLimit1Transformer;
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
    }

    @Override
    public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> transformedChildren = NaryIQTreeTools.transformChildren(children, this::transformChild);

        return furtherSimplifyInnerJoinChildren(
                    rootNode.getOptionalFilterCondition(),
                    transformedChildren)
                .orElseGet(() -> transformedChildren.equals(children)
                        ? tree
                        : iqFactory.createNaryIQTree(rootNode, transformedChildren));
    }

    /**
     * Takes in account the interaction between children
     *
     * Returns empty() if no further optimization has been applied
     *
     */
    protected abstract Optional<IQTree> furtherSimplifyInnerJoinChildren(
            Optional<ImmutableExpression> optionalFilterCondition, ImmutableList<IQTree> children);

    @Override
    public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
        IQTree newChild = transformChild(child);
        return newChild.equals(child)
                ? tree
                : iqFactory.createUnaryIQTree(rootNode, newChild);
    }


    @Override
    public IQTree transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
        IQTree newChild = transformChild(child);
        return newChild.equals(child)
                ? tree
                : iqFactory.createUnaryIQTree(sliceNode, newChild);
    }

    @Override
    public IQTree transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
        IQTree newChild = transformChild(child);
        return newChild.equals(child)
                ? tree
                : iqFactory.createUnaryIQTree(rootNode, newChild);
    }

    @Override
    public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
        IQTree newChild = transformChild(child);
        return newChild.equals(child)
                ? tree
                : iqFactory.createUnaryIQTree(rootNode, newChild);
    }

    @Override
    public IQTree transformFlatten(UnaryIQTree tree, FlattenNode node, IQTree child) {
        IQTree newChild = transformChild(child);
        return newChild.equals(child)
                ? tree
                : iqFactory.createUnaryIQTree(node, newChild);
    }

    @Override
    public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children, this::transformChild);
        return newChildren.equals(children)
                ? tree
                : iqFactory.createNaryIQTree(rootNode, newChildren);
    }

    @Override
    public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        IQTree newLeftChild = transformChild(leftChild);
        IQTree newRightChild = transformChild(rightChild);

        return newLeftChild.equals(leftChild) && newRightChild.equals(rightChild)
                ? tree
                : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild);
    }

    /**
     * By default, switch back to the "LookForDistinctTransformer"
     */
    @Override
    protected IQTree transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode rootNode, IQTree child) {
        return lookForDistinctOrLimit1Transformer.transform(tree);
    }

    /**
     * By default, switch back to the "LookForDistinctTransformer"
     */
    protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        return lookForDistinctOrLimit1Transformer.transform(tree);
    }

    /**
     * By default, switch back to the "LookForDistinctTransformer"
     */
    protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                       IQTree leftChild, IQTree rightChild) {
        return lookForDistinctOrLimit1Transformer.transform(tree);
    }
}
