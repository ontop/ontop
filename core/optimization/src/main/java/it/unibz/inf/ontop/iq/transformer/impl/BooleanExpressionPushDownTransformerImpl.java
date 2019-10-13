package it.unibz.inf.ontop.iq.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transformer.BooleanExpressionPushDownTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.inject.Inject;
import java.util.Optional;

public class BooleanExpressionPushDownTransformerImpl extends DefaultRecursiveIQTreeVisitingTransformer
        implements BooleanExpressionPushDownTransformer {

    private final CoreSingletons coreSingletons;
    private final TermFactory termFactory;

    @Inject
    protected BooleanExpressionPushDownTransformerImpl(CoreSingletons coreSingletons) {
        super(coreSingletons);
        this.coreSingletons = coreSingletons;
        this.termFactory = coreSingletons.getTermFactory();
    }

    @Override
    public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {

        ImmutableList<ImmutableExpression> subExpressions = rootNode.getFilterCondition().flattenAND()
                .collect(ImmutableCollectors.toList());

        ImmutableList.Builder<ImmutableExpression> nonPushedExpressionBuilder = ImmutableList.builder();

        // Recursive. Non-final variable
        IQTree currentChild = child.acceptTransformer(this);

        for (ImmutableExpression subExpression : subExpressions) {
            BooleanExpressionPusher newPusher = new BooleanExpressionPusher(subExpression, coreSingletons);
            Optional<IQTree> optionalChild = currentChild.acceptVisitor(newPusher);
            if (optionalChild.isPresent()) {
                currentChild = optionalChild.get();
            }
            else
                nonPushedExpressionBuilder.add(subExpression);
        }
        IQTree newChild = currentChild;

        return termFactory.getConjunction(nonPushedExpressionBuilder.build().stream())
                .map(iqFactory::createFilterNode)
                .map(n -> (IQTree) iqFactory.createUnaryIQTree(n, newChild))
                .orElse(newChild);
    }

    /**
     * Tries to push the left join condition on the right
     */
    @Override
    public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {

        IQTree newLeftChild = leftChild.acceptTransformer(this);
        IQTree selfTransformedRightChild = rightChild.acceptTransformer(this);

        if (!rootNode.getOptionalFilterCondition().isPresent())
            return leftChild.equals(newLeftChild) && rightChild.equals(selfTransformedRightChild)
                    ? tree
                    : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, selfTransformedRightChild);

        ImmutableList<ImmutableExpression> subExpressions = rootNode.getOptionalFilterCondition().get().flattenAND()
                .collect(ImmutableCollectors.toList());

        ImmutableList.Builder<ImmutableExpression> nonPushedExpressionBuilder = ImmutableList.builder();

        // Recursive. Non-final variable
        IQTree currentRightChild = selfTransformedRightChild;
        ImmutableSet<Variable> rightChildVariables = rightChild.getVariables();

        for (ImmutableExpression subExpression : subExpressions) {
            ImmutableSet<Variable> subExpressionVariables = subExpression.getVariableStream()
                    .collect(ImmutableCollectors.toSet());

            if (rightChildVariables.containsAll(subExpressionVariables)) {

                BooleanExpressionPusher newPusher = new BooleanExpressionPusher(subExpression, coreSingletons);
                Optional<IQTree> optionalChild = currentRightChild.acceptVisitor(newPusher);
                if (optionalChild.isPresent()) {
                    currentRightChild = optionalChild.get();
                } else
                    nonPushedExpressionBuilder.add(subExpression);
            }
            // Expression involving variables not on the right
            else
                nonPushedExpressionBuilder.add(subExpression);
        }

        LeftJoinNode newLeftJoinNode = termFactory.getConjunction(nonPushedExpressionBuilder.build().stream())
                .map(iqFactory::createLeftJoinNode)
                .orElseGet(iqFactory::createLeftJoinNode);

        return iqFactory.createBinaryNonCommutativeIQTree(newLeftJoinNode, newLeftChild, currentRightChild);
    }

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {

        ImmutableList<IQTree> selfTransformedChildren = children.stream()
                .map(this::transform)
                .collect(ImmutableCollectors.toList());

        if (!rootNode.getOptionalFilterCondition().isPresent())
            return selfTransformedChildren.equals(children)
                    ? tree
                    : iqFactory.createNaryIQTree(rootNode, selfTransformedChildren);

        ImmutableList<ImmutableExpression> subExpressions = rootNode.getOptionalFilterCondition().get().flattenAND()
                .collect(ImmutableCollectors.toList());

        ImmutableSet.Builder<ImmutableExpression> nonPushedExpressionBuilder = ImmutableSet.builder();

        // Recursive. Non-final variable
        ImmutableList<IQTree> currentChildren = selfTransformedChildren;

        for (ImmutableExpression subExpression : subExpressions) {
            ImmutableSet<Variable> subExpressionVariables = subExpression.getVariableStream()
                    .collect(ImmutableCollectors.toSet());

            BooleanExpressionPusher newPusher = new BooleanExpressionPusher(subExpression, coreSingletons);

            ImmutableList<IQTree> updatedChildren = currentChildren.stream()
                    .map(c -> c.getVariables().containsAll(subExpressionVariables)
                            ? c.acceptVisitor(newPusher).orElse(c)
                            : c)
                    .collect(ImmutableCollectors.toList());

            /*
             * If the expression could not be pushed down to any child, keep it in the inner join
             */
            if (currentChildren.equals(updatedChildren))
                nonPushedExpressionBuilder.add(subExpression);

            currentChildren = updatedChildren;
        }

        InnerJoinNode newInnerJoinNode = termFactory.getConjunction(nonPushedExpressionBuilder.build().stream())
                .map(iqFactory::createInnerJoinNode)
                .orElseGet(iqFactory::createInnerJoinNode);

        return iqFactory.createNaryIQTree(newInnerJoinNode, currentChildren);

    }
}
