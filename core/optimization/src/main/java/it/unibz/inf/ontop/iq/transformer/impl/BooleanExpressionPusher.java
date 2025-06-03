package it.unibz.inf.ontop.iq.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.iq.visit.impl.AbstractIQVisitor;
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
public class BooleanExpressionPusher extends AbstractIQVisitor<Optional<IQTree>> {

    private final ImmutableExpression expressionToPushDown;
    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;

    protected BooleanExpressionPusher(ImmutableExpression expressionToPushDown,
                                      CoreSingletons coreSingletons) {
        this.expressionToPushDown = expressionToPushDown;
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
    }

    @Override
    public Optional<IQTree> transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
        ImmutableExpression newExpression = rootNode.getSubstitution().apply(expressionToPushDown);
        BooleanExpressionPusher newPusher = new BooleanExpressionPusher(newExpression, coreSingletons);
        return newPusher.visitPassingUnaryNode(rootNode, child);
    }

    @Override
    public Optional<IQTree> transformAggregation(UnaryIQTree tree, AggregationNode aggregationNode, IQTree child) {
        ImmutableSet<Variable> expressionVariables = getExpressionVariables();
        return aggregationNode.getGroupingVariables().containsAll(expressionVariables)
                ? visitPassingUnaryNode(aggregationNode, child)
                : Optional.empty();
    }

    /**
     * NB: focuses on the expressionToPushDown, NOT on pushing down its own expression
     */
    @Override
    public Optional<IQTree> transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
        Optional<IQTree> newChild = transformChild(child);

        UnaryIQTree newTree = newChild
                .map(c -> iqFactory.createUnaryIQTree(rootNode, c))
                .orElseGet(() -> wrapInFilter(
                        termFactory.getConjunction(
                                rootNode.getFilterCondition(), expressionToPushDown), child));

        return Optional.of(newTree);
    }

    @Override
    public Optional<IQTree> transformFlatten(UnaryIQTree tree, FlattenNode rootNode, IQTree child) {
        Optional<Variable> indexVariable = rootNode.getIndexVariable();
        return expressionToPushDown.getVariableStream()
                    .anyMatch(v -> v.equals(rootNode.getOutputVariable()) ||
                            (indexVariable.isPresent() && v.equals(indexVariable.get())))
                ? Optional.empty()
                : visitPassingUnaryNode(rootNode, child);
    }

    @Override
    public Optional<IQTree> transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
        return visitPassingUnaryNode(rootNode, child);
    }

    @Override
    public Optional<IQTree> transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
        // blocks
        return Optional.empty();
    }

    @Override
    public Optional<IQTree> transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
        return visitPassingUnaryNode(rootNode, child);
    }

    protected Optional<IQTree> visitPassingUnaryNode(UnaryOperatorNode rootNode, IQTree child) {
        IQTree newChild = transformChild(child)
                .orElseGet(() ->  wrapInFilter(expressionToPushDown, child));

        return Optional.of(iqFactory.createUnaryIQTree(rootNode, newChild));
    }

    /**
     * Only pushes on the left
     *
     * TODO: consider pushing on the right safe expressions
     */
    @Override
    public Optional<IQTree> transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        ImmutableSet<Variable> expressionVariables = getExpressionVariables();
        if (leftChild.getVariables().containsAll(expressionVariables)) {
            Optional<IQTree> newLeftChild = transformChild(leftChild);
            return newLeftChild
                    .map(l -> iqFactory.createBinaryNonCommutativeIQTree(rootNode, l, rightChild));
        }
        else
            return Optional.empty();
    }

    @Override
    public Optional<IQTree> transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        ImmutableSet<Variable> expressionVariables = getExpressionVariables();

        ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children,
                c -> c.getVariables().containsAll(expressionVariables)
                        ? transformChild(c).orElse(c)
                        : c);

        InnerJoinNode newJoinNode = newChildren.equals(children)
                // Refused by the children
                ? iqFactory.createInnerJoinNode(iqTreeTools.getConjunction(rootNode.getOptionalFilterCondition(), expressionToPushDown))
                : rootNode;

        return Optional.of(iqFactory.createNaryIQTree(newJoinNode, newChildren));
    }

    @Override
    public Optional<IQTree> transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children,
                c -> transformChild(c)
                        .orElseGet(() -> wrapInFilter(expressionToPushDown, c)));

        return Optional.of(iqFactory.createNaryIQTree(rootNode, newChildren));
    }

    private UnaryIQTree wrapInFilter(ImmutableExpression expression, IQTree child) {
        return iqFactory.createUnaryIQTree(iqFactory.createFilterNode(expression), child);
    }

    private ImmutableSet<Variable> getExpressionVariables() {
        return expressionToPushDown.getVariableStream().collect(ImmutableCollectors.toSet());
    }

    @Override
    public Optional<IQTree> transformIntensionalData(IntensionalDataNode dataNode) {
        return Optional.empty();
    }

    @Override
    public Optional<IQTree> transformExtensionalData(ExtensionalDataNode dataNode) {
        return Optional.empty();
    }

    @Override
    public Optional<IQTree> transformEmpty(EmptyNode node) {
        return Optional.empty();
    }

    @Override
    public Optional<IQTree> transformTrue(TrueNode node) {
        return Optional.empty();
    }

    @Override
    public Optional<IQTree> transformNative(NativeNode nativeNode) {
        return Optional.empty();
    }

    @Override
    public Optional<IQTree> transformValues(ValuesNode valuesNode) {
        return Optional.empty();
    }
}
