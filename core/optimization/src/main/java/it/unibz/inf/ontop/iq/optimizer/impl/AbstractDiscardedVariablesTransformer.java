package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultNonRecursiveIQTreeTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.IntStream;

/**
 * TODO: find a better name
 *
 * IMPORTANT: Assumes that cardinality does matter for the current tree
 *
 */
public abstract class AbstractDiscardedVariablesTransformer extends DefaultNonRecursiveIQTreeTransformer {

    private final IQTreeTransformer lookForDistinctTransformer;

    /**
     * Variables that are not considered by the upper tree
     */
    private final ImmutableSet<Variable> discardedVariables;
    protected final CoreSingletons coreSingletons;

    protected AbstractDiscardedVariablesTransformer(ImmutableSet<Variable> discardedVariables,
                                                    IQTreeTransformer lookForDistinctTransformer,
                                                    CoreSingletons coreSingletons) {
        this.discardedVariables = discardedVariables;
        this.coreSingletons = coreSingletons;
        this.lookForDistinctTransformer = lookForDistinctTransformer;
    }

    protected abstract AbstractDiscardedVariablesTransformer update(ImmutableSet<Variable> newDiscardedVariables);

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        ImmutableSet<Variable> discardedVariablesAfterJoiningCondition = rootNode.getOptionalFilterCondition()
                .map(c -> Sets.difference(discardedVariables, c.getVariables()).immutableCopy())
                .orElse(discardedVariables);

        // Only "interaction" between child: their common variables
        ImmutableList<ImmutableSet<Variable>> discardedVariablesPerChild = IntStream.range(0, children.size())
                .boxed()
                .map(i -> extractDiscardedVariablesForInnerJoinChild(i, children, discardedVariablesAfterJoiningCondition))
                .collect(ImmutableCollectors.toList());

        ImmutableList<IQTree> partiallySimplifiedChildren = IntStream.range(0, children.size())
                .boxed()
                .map(i -> update(discardedVariablesPerChild.get(i)).transform(children.get(i)))
                .collect(ImmutableCollectors.toList());


        return furtherSimplifyInnerJoinChildren(
                    discardedVariablesPerChild,
                    rootNode.getOptionalFilterCondition(),
                    partiallySimplifiedChildren)
                .orElseGet(() -> partiallySimplifiedChildren.equals(children)
                        ? tree
                        : coreSingletons.getIQFactory().createNaryIQTree(rootNode, partiallySimplifiedChildren));
    }

    /**
     * TODO: find a better name
     */
    private ImmutableSet<Variable> extractDiscardedVariablesForInnerJoinChild(int childPosition, ImmutableList<IQTree> children,
                                                                              ImmutableSet<Variable> discardedVariablesAfterJoiningCondition) {
        return Sets.difference(discardedVariablesAfterJoiningCondition,
                IntStream.range(0, children.size())
                        .filter(i -> i != childPosition)
                        .boxed()
                        .map(children::get)
                        .flatMap(c -> c.getVariables().stream())
                        .collect(ImmutableCollectors.toSet())).immutableCopy();
    }

    /**
     * Takes in account the interaction between children
     *
     * Returns empty() if no further optimization has been applied
     *
     */
    protected abstract Optional<IQTree> furtherSimplifyInnerJoinChildren(
            ImmutableList<ImmutableSet<Variable>> discardedVariablesPerChild,
            Optional<ImmutableExpression> optionalFilterCondition, ImmutableList<IQTree> children);

    /**
     * NB: For the sake of simplicity, does not "take into account" (i.e. optimize) discarded variables projected by the construction node.
     * This should not occur if the tree is normalized.
     *
     */
    @Override
    public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
        // NB: Does take into consideration what the parent declared as "discarded" variables.
        ImmutableSet<Variable> newDiscardedVariables = Sets.difference(
                child.getVariables(), rootNode.getLocallyRequiredVariables()).immutableCopy();

        AbstractDiscardedVariablesTransformer newTransformer = update(newDiscardedVariables);

        IQTree newChild = child.acceptTransformer(newTransformer);

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
        ImmutableSet<Variable> newDiscardedVariables = Sets.difference(
                discardedVariables, rootNode.getFilterCondition().getVariables()).immutableCopy();

        AbstractDiscardedVariablesTransformer newTransformer = update(newDiscardedVariables);

        IQTree newChild = child.acceptTransformer(newTransformer);

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
        ImmutableSet<Variable> newDiscardedVariables = rootNode.getOptionalFilterCondition()
                .map(c -> Sets.difference(discardedVariables, c.getVariables()).immutableCopy())
                .orElse(discardedVariables);

        AbstractDiscardedVariablesTransformer newTransformer = update(newDiscardedVariables);

        IQTree newLeftChild = leftChild.acceptTransformer(newTransformer);
        IQTree newRightChild = rightChild.acceptTransformer(newTransformer);

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
