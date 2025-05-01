package it.unibz.inf.ontop.iq.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

/**
 * Tries to push down the expression given.
 * If it succeeds, return an IQTree, otherwise nothing.
 *
 * ONLY CARES about the expression given as input.
 *
 */
public class BooleanExpressionPusher implements IQVisitor<Optional<IQTree>> {

    private final ImmutableExpression expressionToPushDown;
    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;

    protected BooleanExpressionPusher(ImmutableExpression expressionToPushDown,
                                      CoreSingletons coreSingletons) {
        this.expressionToPushDown = expressionToPushDown;
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
        this.termFactory = coreSingletons.getTermFactory();
    }

    @Override
    public Optional<IQTree> transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
        Substitution<ImmutableTerm> substitution = rootNode.getSubstitution();

        ImmutableExpression newExpression = substitution.apply(expressionToPushDown);

        BooleanExpressionPusher newPusher = new BooleanExpressionPusher(newExpression, coreSingletons);
        IQTree newChild = child.acceptVisitor(newPusher)
                .orElseGet(() ->  iqFactory.createUnaryIQTree(
                        iqFactory.createFilterNode(newExpression),
                        child));

        return Optional.of(iqFactory.createUnaryIQTree(rootNode, newChild));
    }
    @Override
    public Optional<IQTree> transformAggregation(IQTree tree, AggregationNode aggregationNode, IQTree child) {
        ImmutableSet<Variable> expressionVariables = expressionToPushDown.getVariableStream()
                .collect(ImmutableCollectors.toSet());

        return (aggregationNode.getGroupingVariables().containsAll(expressionVariables))
                ? visitPassingUnaryNode(aggregationNode, child)
                : Optional.empty();
    }

    protected Optional<IQTree> visitPassingUnaryNode(UnaryOperatorNode rootNode, IQTree child) {
        IQTree newChild = child.acceptVisitor(this)
                .orElseGet(() ->  iqFactory.createUnaryIQTree(
                        iqFactory.createFilterNode(expressionToPushDown),
                        child));

        return Optional.of(iqFactory.createUnaryIQTree(rootNode, newChild));
    }

    /**
     * NB: focuses on the expressionToPushDown, NOT on pushing down its own expression
     */
    @Override
    public Optional<IQTree> transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
        Optional<IQTree> newChild = child.acceptVisitor(this);

        UnaryIQTree newTree = newChild
                .map(c -> iqFactory.createUnaryIQTree(rootNode, c))
                .orElseGet(() ->
                        iqFactory.createUnaryIQTree(
                                iqFactory.createFilterNode(
                                        termFactory.getConjunction(rootNode.getFilterCondition(), expressionToPushDown)),
                                child));

        return Optional.of(newTree);
    }

    @Override
    public Optional<IQTree> transformFlatten(IQTree tree, FlattenNode rootNode, IQTree child) {
        Optional<Variable> indexVariable = rootNode.getIndexVariable();
        return expressionToPushDown.getVariableStream()
                    .anyMatch(v -> v.equals(rootNode.getOutputVariable()) ||
                            (indexVariable.isPresent() && v.equals(indexVariable.get())))
                ? Optional.empty()
                : visitPassingUnaryNode(rootNode, child);
    }

    @Override
    public Optional<IQTree> transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
        return visitPassingUnaryNode(rootNode, child);
    }

    /**
     * The slice blocks
     */
    @Override
    public Optional<IQTree> transformSlice(IQTree tree, SliceNode sliceNode, IQTree child) {
        return Optional.empty();
    }

    @Override
    public Optional<IQTree> transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {
        return visitPassingUnaryNode(rootNode, child);
    }

    /**
     * Only pushes on the left
     *
     * TODO: consider pushing on the right safe expressions
     */
    @Override
    public Optional<IQTree> transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        ImmutableSet<Variable> expressionVariables = expressionToPushDown.getVariableStream()
                .collect(ImmutableCollectors.toSet());

        if (leftChild.getVariables().containsAll(expressionVariables)) {
            Optional<IQTree> newLeftChild = leftChild.acceptVisitor(this);
            return newLeftChild
                    .map(l -> iqFactory.createBinaryNonCommutativeIQTree(rootNode, l, rightChild));
        }
        else
            return Optional.empty();
    }

    @Override
    public Optional<IQTree> transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        ImmutableSet<Variable> expressionVariables = expressionToPushDown.getVariableStream()
                .collect(ImmutableCollectors.toSet());

        ImmutableList<IQTree> newChildren = children.stream()
                .map(c -> c.getVariables().containsAll(expressionVariables)
                        ? c.acceptVisitor(this).orElse(c)
                        : c)
                .collect(ImmutableCollectors.toList());

        InnerJoinNode newJoinNode = newChildren.equals(children)
                // Refused by the children
                ? iqFactory.createInnerJoinNode(
                rootNode.getOptionalFilterCondition()
                        .map(c -> termFactory.getConjunction(c, expressionToPushDown))
                        .orElse(expressionToPushDown))
                : rootNode;

        return Optional.of(iqFactory.createNaryIQTree(newJoinNode, newChildren));
    }

    @Override
    public Optional<IQTree> transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> newChildren = children.stream()
                .map(c -> c.acceptVisitor(this)
                        .orElseGet(() -> iqFactory.createUnaryIQTree(
                                iqFactory.createFilterNode(expressionToPushDown),
                                c)))
                .collect(ImmutableCollectors.toList());

        return Optional.of(iqFactory.createNaryIQTree(rootNode, newChildren));
    }

    /**
     * Leaf nodes do not accept expressions
     */
    private Optional<IQTree> visitLeafNode() {
        return Optional.empty();
    }

    @Override
    public Optional<IQTree> transformIntensionalData(IntensionalDataNode dataNode) {
        return visitLeafNode();
    }

    @Override
    public Optional<IQTree> transformExtensionalData(ExtensionalDataNode dataNode) {
        return visitLeafNode();
    }

    @Override
    public Optional<IQTree> transformEmpty(EmptyNode node) {
        return visitLeafNode();
    }

    @Override
    public Optional<IQTree> transformTrue(TrueNode node) {
        return visitLeafNode();
    }

    @Override
    public Optional<IQTree> transformNative(NativeNode nativeNode) {
        return visitLeafNode();
    }

    @Override
    public Optional<IQTree> transformValues(ValuesNode valuesNode) {
        return visitLeafNode();
    }
}
