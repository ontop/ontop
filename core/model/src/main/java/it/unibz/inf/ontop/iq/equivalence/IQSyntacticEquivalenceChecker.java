package it.unibz.inf.ontop.iq.equivalence;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.QueryNode;

import java.util.LinkedList;
import java.util.Queue;

/**
 * TODO explain
 */
public class IQSyntacticEquivalenceChecker {

    private static class NodePair {
        public final QueryNode node1;
        public final QueryNode node2;

        private NodePair(QueryNode node1, QueryNode node2) {
            this.node1 = node1;
            this.node2 = node2;
        }
    }

    public static boolean areEquivalent(IntermediateQuery query1, IntermediateQuery query2) {

        if (!query1.getProjectionAtom().equals(query2.getProjectionAtom()))
            return false;

        Queue<NodePair> pairsToVisit = new LinkedList<>();
        pairsToVisit.add(new NodePair(query1.getRootNode(), query2.getRootNode()));

        while(!pairsToVisit.isEmpty()) {
            NodePair pair = pairsToVisit.poll();
            if (!areNodesEqual(pair.node1, pair.node2)) {
                return false;
            }

            ImmutableList<QueryNode> children1 = sort(query1.getChildren(pair.node1));
            ImmutableList<QueryNode> children2 = sort(query2.getChildren(pair.node2));

            /**
             * check sizes
             */
            if(children1.size() != children2.size()) {
                return false;
            }

            /**
             * create pairs
             */
            for(int i = 0; i < children1.size(); i++) {
                pairsToVisit.add(new NodePair(children1.get(i), children2.get(i)));
            }
        }
        return true;
    }

    /**
     * Currently we do not sort
     */
    private static ImmutableList<QueryNode> sort(ImmutableList<QueryNode> children) {
        return children;
    }

    private static boolean areNodesEqual(QueryNode node1, QueryNode node2) {

        return node1.isSyntacticallyEquivalentTo(node2);
    }

}
