package org.semanticweb.ontop.executor.join;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator;
import org.semanticweb.ontop.pivotalrepr.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import static org.semanticweb.ontop.model.impl.OBDAVocabulary.*;

/**
 * TODO: describe
 */
public class JoinExtractionUtils {

    /**
     * TODO: explain
     */
    public static class InsatisfiedExpressionException extends Exception  {
    }


    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

    /**
     * TODO: explain
     */
    public static Optional<ImmutableBooleanExpression> extractFoldAndOptimizeBooleanExpressions(
            ImmutableList<JoinOrFilterNode> filterAndJoinNodes) throws InsatisfiedExpressionException {

        ImmutableList<ImmutableBooleanExpression> booleanExpressions = extractBooleanExpressions(
                filterAndJoinNodes);

        Optional<ImmutableBooleanExpression> foldedExpression = foldBooleanExpressions(booleanExpressions);
        if (foldedExpression.isPresent()) {
            ExpressionEvaluator evaluator = new ExpressionEvaluator();

            Optional<ImmutableBooleanExpression> optionalEvaluatedExpression = evaluator.evaluateBooleanExpression(
                    foldedExpression.get());
            if (optionalEvaluatedExpression.isPresent()) {
                return optionalEvaluatedExpression;
            }
            else {
                throw new InsatisfiedExpressionException();
            }
        }
        else {
            return Optional.absent();
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

    public static Optional<ImmutableBooleanExpression> foldBooleanExpressions(
        ImmutableList<ImmutableBooleanExpression> booleanExpressions) {
        switch (booleanExpressions.size()) {
            case 0:
                return Optional.absent();
            case 1:
                return Optional.of(booleanExpressions.get(0));
            default:
                Iterator<ImmutableBooleanExpression> it = booleanExpressions.iterator();

                // Non-final
                ImmutableBooleanExpression currentExpression = DATA_FACTORY.getImmutableBooleanExpression(AND,
                        it.next(), it.next());
                while(it.hasNext()) {
                    currentExpression = DATA_FACTORY.getImmutableBooleanExpression(AND, currentExpression, it.next());
                }

                return Optional.of(currentExpression);
        }
    }

    @Deprecated
    public static ImmutableList<ImmutableBooleanExpression> extractBooleanExpressionsFromJoins(InnerJoinNode topJoinNode,
                                                                                               IntermediateQuery query) {
        Queue<InnerJoinNode> joinNodesToExplore = new LinkedList<>();
        joinNodesToExplore.add(topJoinNode);

        ImmutableList.Builder<ImmutableBooleanExpression> exprListBuilder = ImmutableList.builder();

        while (!joinNodesToExplore.isEmpty()) {
            InnerJoinNode joinNode = joinNodesToExplore.poll();

            Optional<ImmutableBooleanExpression> optionalFilterCondition = joinNode.getOptionalFilterCondition();
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

    private static ImmutableList<ImmutableBooleanExpression> extractBooleanExpressions(
            ImmutableList<JoinOrFilterNode> filterAndJoinNodes) {
        ImmutableList.Builder<ImmutableBooleanExpression> builder = ImmutableList.builder();
        for (JoinOrFilterNode node : filterAndJoinNodes) {
            Optional<ImmutableBooleanExpression> optionalFilterCondition = node.getOptionalFilterCondition();
            if (optionalFilterCondition.isPresent()) {
                builder.add(optionalFilterCondition.get());
            }
        }
        return builder.build();
    }
}
