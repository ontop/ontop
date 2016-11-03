package it.unibz.inf.ontop.executor.join;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.ExpressionOperation;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator;
import it.unibz.inf.ontop.pivotalrepr.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * TODO: describe
 */
public class JoinExtractionUtils {

    /**
     * TODO: explain
     */
    public static class UnsatisfiableExpressionException extends Exception  {
    }


    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

    /**
     * TODO: explain
     */
    public static Optional<ImmutableExpression> extractFoldAndOptimizeBooleanExpressions(
            ImmutableList<JoinOrFilterNode> filterAndJoinNodes, MetadataForQueryOptimization metadata)
            throws UnsatisfiableExpressionException {

        ImmutableList<ImmutableExpression> booleanExpressions = extractBooleanExpressions(
                filterAndJoinNodes);

        Optional<ImmutableExpression> foldedExpression = foldBooleanExpressions(booleanExpressions);
        if (foldedExpression.isPresent()) {
            ExpressionEvaluator evaluator = new ExpressionEvaluator(metadata.getUriTemplateMatcher());

            ExpressionEvaluator.Evaluation evaluation = evaluator.evaluateExpression(foldedExpression.get());
            if (evaluation.isFalse()) {
                throw new UnsatisfiableExpressionException();
            }
            else {
                return evaluation.getOptionalExpression();
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

    public static Optional<ImmutableExpression> foldBooleanExpressions(
        ImmutableList<ImmutableExpression> booleanExpressions) {
        switch (booleanExpressions.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(booleanExpressions.get(0));
            default:
                Iterator<ImmutableExpression> it = booleanExpressions.iterator();

                // Non-final
                ImmutableExpression currentExpression = DATA_FACTORY.getImmutableExpression(ExpressionOperation.AND,
                        it.next(), it.next());
                while(it.hasNext()) {
                    currentExpression = DATA_FACTORY.getImmutableExpression(ExpressionOperation.AND, currentExpression, it.next());
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
}
