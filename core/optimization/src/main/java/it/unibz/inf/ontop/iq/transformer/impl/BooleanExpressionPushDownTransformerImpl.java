package it.unibz.inf.ontop.iq.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transformer.BooleanExpressionPushDownTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.BiFunction;

public class BooleanExpressionPushDownTransformerImpl extends DefaultRecursiveIQTreeVisitingTransformer
        implements BooleanExpressionPushDownTransformer {

    private final CoreSingletons coreSingletons;
    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    protected BooleanExpressionPushDownTransformerImpl(CoreSingletons coreSingletons) {
        super(coreSingletons.getIQFactory());
        this.coreSingletons = coreSingletons;
        this.termFactory = coreSingletons.getTermFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
    }

    @Override
    public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {

        IQTree transformedChild = child.acceptTransformer(this);

        PushResult<IQTree> result = pushExpressionDown(
                transformedChild,
                rootNode.getFilterCondition(),
                this::pushExpressionDown);

        var optionalFilter = iqTreeTools.createOptionalFilterNode(result.nonPushedExpression);
        return iqTreeTools.unaryIQTreeBuilder()
                .append(optionalFilter)
                .build(result.result);
    }

    /**
     * Tries to push the left join condition on the right
     */
    @Override
    public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {

        IQTree transformedLeftChild = leftChild.acceptTransformer(this);
        IQTree transformedRightChild = rightChild.acceptTransformer(this);

        if (rootNode.getOptionalFilterCondition().isEmpty())
            return leftChild.equals(transformedLeftChild) && rightChild.equals(transformedRightChild)
                    ? tree
                    : iqFactory.createBinaryNonCommutativeIQTree(rootNode, transformedLeftChild, transformedRightChild);

        // Expressions involving variables not on the right are not pushed
        PushResult<IQTree> result = pushExpressionDown(
                transformedRightChild,
                rootNode.getOptionalFilterCondition().get(),
                this::pushExpressionDownIfContainsVariables);

        return iqTreeTools.createLeftJoinTree(
                result.nonPushedExpression,
                transformedLeftChild,
                result.result);
    }

    @Override
    public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {

        ImmutableList<IQTree> transformedChildren = NaryIQTreeTools.transformChildren(children,
                t -> t.acceptTransformer(this));

        if (rootNode.getOptionalFilterCondition().isEmpty())
            return transformedChildren.equals(children)
                    ? tree
                    : iqTreeTools.createInnerJoinTree(transformedChildren);

        PushResult<ImmutableList<IQTree>> result = pushExpressionDown(
                transformedChildren,
                rootNode.getOptionalFilterCondition().get(),
                this::pushExpressionDownIfContainsVariables);

        return iqTreeTools.createInnerJoinTree(result.nonPushedExpression, result.result);
    }

    private Optional<IQTree> pushExpressionDown(ImmutableExpression expression, IQTree child) {
        BooleanExpressionPusher newPusher = new BooleanExpressionPusher(expression, coreSingletons);
        return child.acceptVisitor(newPusher);
    }

    private Optional<IQTree> pushExpressionDownIfContainsVariables(ImmutableExpression expression, IQTree tree) {
        if (tree.getVariables().containsAll(expression.getVariables()))
            return pushExpressionDown(expression, tree);
        return Optional.empty();
    }

    private Optional<ImmutableList<IQTree>> pushExpressionDownIfContainsVariables(ImmutableExpression expression, ImmutableList<IQTree> list) {
        return Optional.of(list.stream()
                        .map(tree -> pushExpressionDownIfContainsVariables(expression, tree)
                                .orElse(tree))
                        .collect(ImmutableCollectors.toList()))
                // If the expression could not be pushed down to any child, keep it in the inner join
                .filter(rr -> !rr.equals(list));
    }

    private static final class PushResult<T> {
        final T result;
        final Optional<ImmutableExpression>  nonPushedExpression;

        PushResult(T result, Optional<ImmutableExpression> nonPushedExpression) {
            this.result = result;
            this.nonPushedExpression = nonPushedExpression;
        }
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
