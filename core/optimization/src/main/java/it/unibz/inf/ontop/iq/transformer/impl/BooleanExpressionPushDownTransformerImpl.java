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
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transformer.BooleanExpressionPushDownTransformer;
import it.unibz.inf.ontop.iq.visit.impl.AbstractIQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.BiFunction;

public class BooleanExpressionPushDownTransformerImpl implements BooleanExpressionPushDownTransformer {

    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;
    private final IntermediateQueryFactory iqFactory;
    private final Transformer transformer;

    @Inject
    protected BooleanExpressionPushDownTransformerImpl(CoreSingletons coreSingletons) {
        this.termFactory = coreSingletons.getTermFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.iqFactory = coreSingletons.getIQFactory();
        this.transformer = new Transformer();
    }

    @Override
    public IQTree transform(IQTree tree) {
        return tree.acceptVisitor(transformer);
    }


    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        Transformer() {
            super(BooleanExpressionPushDownTransformerImpl.this.iqFactory);
        }

        @Override
        public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {

            PushResult<IQTree> result = pushExpressionDown(
                    transformChild(child),
                    rootNode.getFilterCondition(),
                    this::pushExpressionDown);

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(iqTreeTools.createOptionalFilterNode(result.nonPushedExpression))
                    .build(result.result);
        }

        /**
         * Tries to push the left join condition on the right
         */
        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {

            if (rootNode.getOptionalFilterCondition().isEmpty())
                return super.transformLeftJoin(tree, rootNode, leftChild, rightChild);

            IQTree transformedLeftChild = transformChild(leftChild);
            IQTree transformedRightChild = transformChild(rightChild);

            // Expressions involving variables not on the right are not pushed
            PushResult<IQTree> result = pushExpressionDown(
                    transformedRightChild,
                    rootNode.getOptionalFilterCondition().get(),
                    this::pushExpressionDownIfContainsVariables);

            return iqTreeTools.createLeftJoinTree(result.nonPushedExpression, transformedLeftChild, result.result);
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {

            if (rootNode.getOptionalFilterCondition().isEmpty())
                return super.transformInnerJoin(tree, rootNode, children);

            ImmutableList<IQTree> transformedChildren = NaryIQTreeTools.transformChildren(children, this::transformChild);

            PushResult<ImmutableList<IQTree>> result = pushExpressionDown(
                    transformedChildren,
                    rootNode.getOptionalFilterCondition().get(),
                    this::pushExpressionDownIfContainsVariables);

            return iqTreeTools.createInnerJoinTree(result.nonPushedExpression, result.result);
        }

        private Optional<IQTree> pushExpressionDown(ImmutableExpression expression, IQTree child) {
            return child.acceptVisitor(new BooleanExpressionPusher(expression));
        }

        private Optional<IQTree> pushExpressionDownIfContainsVariables(ImmutableExpression expression, IQTree tree) {
            if (tree.getVariables().containsAll(expression.getVariables()))
                return pushExpressionDown(expression, tree);
            return Optional.empty();
        }

        private Optional<ImmutableList<IQTree>> pushExpressionDownIfContainsVariables(ImmutableExpression expression, ImmutableList<IQTree> list) {
            return Optional.of(NaryIQTreeTools.transformChildren(list,
                            tree -> pushExpressionDownIfContainsVariables(expression, tree)
                                    .orElse(tree)))
                    // If the expression could not be pushed down to any child, keep it in the inner join
                    .filter(r -> !r.equals(list));
        }

        private <T> PushResult<T> pushExpressionDown(T initial, ImmutableExpression expression, BiFunction<ImmutableExpression, T, Optional<T>> function) {
            ImmutableSet.Builder<ImmutableExpression> nonPushedExpressionBuilder = ImmutableSet.builder();
            T result = initial; // non-final
            for (ImmutableExpression exp : expression.flattenAND().collect(ImmutableCollectors.toList())) {
                Optional<T> optionalResult = function.apply(exp, result);
                if (optionalResult.isPresent())
                    result = optionalResult.get();
                else
                    nonPushedExpressionBuilder.add(exp);
            }
            return new PushResult<>(result, termFactory.getConjunction(nonPushedExpressionBuilder.build().stream()));
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final class PushResult<T> {
        private final T result;
        private final Optional<ImmutableExpression>  nonPushedExpression;

        private PushResult(T result, Optional<ImmutableExpression> nonPushedExpression) {
            this.result = result;
            this.nonPushedExpression = nonPushedExpression;
        }
    }


    private class BooleanExpressionPusher extends AbstractIQVisitor<Optional<IQTree>> {

        private final ImmutableExpression expressionToPushDown;

        private BooleanExpressionPusher(ImmutableExpression expressionToPushDown) {
            this.expressionToPushDown = expressionToPushDown;
        }

        @Override
        public Optional<IQTree> transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
            ImmutableExpression newExpression = rootNode.getSubstitution().apply(expressionToPushDown);
            BooleanExpressionPusher newPusher = new BooleanExpressionPusher(newExpression);
            return newPusher.visitPassingUnaryNode(rootNode, child);
        }

        @Override
        public Optional<IQTree> transformAggregation(UnaryIQTree tree, AggregationNode aggregationNode, IQTree child) {
            ImmutableSet<Variable> expressionVariables = expressionToPushDown.getVariables();
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

        private Optional<IQTree> visitPassingUnaryNode(UnaryOperatorNode rootNode, IQTree child) {
            IQTree newChild = transformChild(child)
                    .orElseGet(() -> wrapInFilter(expressionToPushDown, child));

            return Optional.of(iqFactory.createUnaryIQTree(rootNode, newChild));
        }

        /**
         * Only pushes on the left
         *
         * TODO: consider pushing on the right safe expressions
         */
        @Override
        public Optional<IQTree> transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            return leftChild.getVariables().containsAll(expressionToPushDown.getVariables())
                ? transformChild(leftChild)
                        .map(l -> iqFactory.createBinaryNonCommutativeIQTree(rootNode, l, rightChild))
                : Optional.empty();
        }

        @Override
        public Optional<IQTree> transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            ImmutableSet<Variable> expressionVariables = expressionToPushDown.getVariables();

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
}
