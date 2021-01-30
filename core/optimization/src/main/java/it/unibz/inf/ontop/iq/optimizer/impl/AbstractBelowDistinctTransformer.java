package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultNonRecursiveIQTreeTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

/**
 * Transformer where the cardinality does matter for the current tree
 *
 */
public abstract class AbstractBelowDistinctTransformer extends DefaultNonRecursiveIQTreeTransformer {

    private final IQTreeTransformer lookForDistinctTransformer;
    protected final CoreSingletons coreSingletons;

    protected AbstractBelowDistinctTransformer(IQTreeTransformer lookForDistinctTransformer,
                                               CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
        this.lookForDistinctTransformer = lookForDistinctTransformer;
    }

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> transformedChildren = children.stream()
                .map(this::transform)
                .collect(ImmutableCollectors.toList());


        return furtherSimplifyInnerJoinChildren(
                    rootNode.getOptionalFilterCondition(),
                    transformedChildren)
                .orElseGet(() -> transformedChildren.equals(children)
                        ? tree
                        : coreSingletons.getIQFactory().createNaryIQTree(rootNode, transformedChildren));
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
    public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
        IQTree newChild = transform(child);

        return (newChild.equals(child))
                ? tree
                : coreSingletons.getIQFactory().createUnaryIQTree(rootNode, newChild);
    }


    @Override
    public IQTree transformSlice(IQTree tree, SliceNode sliceNode, IQTree child) {
        IQTree newChild = transform(child);
        return (newChild.equals(child))
                ? tree
                : coreSingletons.getIQFactory().createUnaryIQTree(sliceNode, newChild);
    }

    @Override
    public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {
        IQTree newChild = transform(child);
        return (newChild.equals(child))
                ? tree
                : coreSingletons.getIQFactory().createUnaryIQTree(rootNode, newChild);
    }

    @Override
    public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
        IQTree newChild = transform(child);

        return (newChild.equals(child))
                ? tree
                : coreSingletons.getIQFactory().createUnaryIQTree(rootNode, newChild);
    }

    @Override
    public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> newChildren = children.stream()
                .map(this::transform)
                .collect(ImmutableCollectors.toList());

        return newChildren.equals(children)
                ? tree
                : coreSingletons.getIQFactory().createNaryIQTree(rootNode, newChildren);
    }

    @Override
    public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        IQTree newLeftChild = transform(leftChild);
        IQTree newRightChild = transform(rightChild);

        return (newLeftChild.equals(leftChild) && newRightChild.equals(rightChild))
                ? tree
                : coreSingletons.getIQFactory().createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild);
    }

    /**
     * By default, switch back to the "LookForDistinctTransformer"
     */
    protected IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
        return lookForDistinctTransformer.transform(tree);
    }

    /**
     * By default, switch back to the "LookForDistinctTransformer"
     */
    protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        return lookForDistinctTransformer.transform(tree);
    }

    /**
     * By default, switch back to the "LookForDistinctTransformer"
     */
    protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                       IQTree leftChild, IQTree rightChild) {
        return lookForDistinctTransformer.transform(tree);
    }
}
