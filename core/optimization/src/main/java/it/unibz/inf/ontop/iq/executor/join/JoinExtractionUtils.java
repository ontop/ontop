package it.unibz.inf.ontop.iq.executor.join;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.JoinOrFilterNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.iq.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * TODO: describe
 */
public class JoinExtractionUtils {

    private final TermFactory termFactory;
    private final ExpressionEvaluator defaultExpressionEvaluator;

    @Inject
    private JoinExtractionUtils(TermFactory termFactory,
                                ExpressionEvaluator defaultExpressionEvaluator) {
        this.termFactory = termFactory;
        this.defaultExpressionEvaluator = defaultExpressionEvaluator;
    }

    /**
     * TODO: explain
     */
    public Optional<ImmutableExpression> extractFoldAndOptimizeBooleanExpressions(
            ImmutableList<JoinOrFilterNode> filterAndJoinNodes)
            throws UnsatisfiableExpressionException {

        ImmutableList<ImmutableExpression> booleanExpressions = extractBooleanExpressions(
                filterAndJoinNodes);

        Optional<ImmutableExpression> foldedExpression = foldBooleanExpressions(booleanExpressions);
        if (foldedExpression.isPresent()) {
            ExpressionEvaluator evaluator = defaultExpressionEvaluator.clone();

            ExpressionEvaluator.EvaluationResult evaluationResult = evaluator.evaluateExpression(foldedExpression.get());
            if (evaluationResult.isEffectiveFalse()) {
                throw new UnsatisfiableExpressionException();
            }
            else {
                return evaluationResult.getOptionalExpression();
            }
        }
        else {
            return Optional.empty();
        }
    }


    /**
     * TODO: find a better name
     * TODO: explain
     */
    public static ImmutableList<JoinOrFilterNode> extractFilterAndInnerJoinNodes(InnerJoinNode topJoinNode,
                                                                                 IntermediateQuery query) {

        ImmutableList.Builder<JoinOrFilterNode> joinAndFilterNodeBuilder = ImmutableList.builder();
        Queue<JoinOrFilterNode> nodesToExplore = new LinkedList<>();

        nodesToExplore.add(topJoinNode);
        joinAndFilterNodeBuilder.add(topJoinNode);

        while (!nodesToExplore.isEmpty()) {
            JoinOrFilterNode joinNode = nodesToExplore.poll();

            /**
             * Children: only considers the inner joins and the filter nodes.
             */
            for (QueryNode child : query.getChildren(joinNode)) {
                if ((child instanceof InnerJoinNode)
                        || (child instanceof FilterNode)) {

                    JoinOrFilterNode joinOrFilterChild = (JoinOrFilterNode) child;

                    // Continues exploring
                    nodesToExplore.add(joinOrFilterChild);
                    joinAndFilterNodeBuilder.add(joinOrFilterChild);
                }
            }
        }
        return joinAndFilterNodeBuilder.build();
    }

    public Optional<ImmutableExpression> foldBooleanExpressions(
        ImmutableList<ImmutableExpression> booleanExpressions) {
        switch (booleanExpressions.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(booleanExpressions.get(0));
            default:
                Iterator<ImmutableExpression> it = booleanExpressions.iterator();

                // Non-final
                ImmutableExpression currentExpression = termFactory.getImmutableExpression(ExpressionOperation.AND,
                        it.next(), it.next());
                while(it.hasNext()) {
                    currentExpression = termFactory.getImmutableExpression(ExpressionOperation.AND, currentExpression, it.next());
                }

                return Optional.of(currentExpression);
        }
    }

    @Deprecated
    public static ImmutableList<ImmutableExpression> extractBooleanExpressionsFromJoins(InnerJoinNode topJoinNode,
                                                                                        IntermediateQuery query) {
        Queue<InnerJoinNode> joinNodesToExplore = new LinkedList<>();
        joinNodesToExplore.add(topJoinNode);

        ImmutableList.Builder<ImmutableExpression> exprListBuilder = ImmutableList.builder();

        while (!joinNodesToExplore.isEmpty()) {
            InnerJoinNode joinNode = joinNodesToExplore.poll();

            Optional<ImmutableExpression> optionalFilterCondition = joinNode.getOptionalFilterCondition();
            if (optionalFilterCondition.isPresent()) {
                exprListBuilder.add(optionalFilterCondition.get());
            }

            /**
             * Children: only considers the inner joins and the filter nodes.
             */
            for (QueryNode child : query.getChildren(joinNode)) {
                if (child instanceof InnerJoinNode) {
                    // Continues exploring
                    joinNodesToExplore.add((InnerJoinNode)child);
                }
                else if (child instanceof FilterNode) {
                    exprListBuilder.add(((FilterNode)child).getFilterCondition());
                }
            }
        }
        return exprListBuilder.build();
    }


    @Deprecated
    public static ImmutableList<QueryNode> extractNonInnerJoinOrFilterNodesFromJoins(InnerJoinNode topJoinNode,
                                                                              IntermediateQuery query) {

        /**
         * Only inner joins and filter nodes, NOT LEFT-JOINS
         */
        Queue<JoinOrFilterNode> filterOrJoinNodesToExplore = new LinkedList<>();
        filterOrJoinNodesToExplore.add(topJoinNode);

        ImmutableList.Builder<QueryNode> otherNodeListBuilder = ImmutableList.builder();

        while (!filterOrJoinNodesToExplore.isEmpty()) {
            JoinOrFilterNode node = filterOrJoinNodesToExplore.poll();

            /**
             * Children:
             *  - Continues exploring the inner joins and the filter nodes.
             *  - Adds the others in the list
             *
             */
            for (QueryNode child : query.getChildren(node)) {
                if ((child instanceof InnerJoinNode)
                        || (child instanceof FilterNode)) {
                    filterOrJoinNodesToExplore.add((JoinOrFilterNode)child);
                }
                else {
                    otherNodeListBuilder.add(child);
                }
            }
        }
        return otherNodeListBuilder.build();
    }

    private static ImmutableList<ImmutableExpression> extractBooleanExpressions(
            ImmutableList<JoinOrFilterNode> filterAndJoinNodes) {
        ImmutableList.Builder<ImmutableExpression> builder = ImmutableList.builder();
        for (JoinOrFilterNode node : filterAndJoinNodes) {
            Optional<ImmutableExpression> optionalFilterCondition = node.getOptionalFilterCondition();
            if (optionalFilterCondition.isPresent()) {
                builder.add(optionalFilterCondition.get());
            }
        }
        return builder.build();
    }

    /**
     * TODO: explain
     */
    public static class UnsatisfiableExpressionException extends Exception  {
    }
}
