package it.unibz.inf.ontop.pivotalrepr.validation;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Checks the QueryNode and their children
 */
public class StandardIntermediateQueryValidator implements IntermediateQueryValidator {

    /**
     * TODO: use a visitor
     * TODO: throw InvalidIQException
     */
    @Override
    public void validate(IntermediateQuery query) throws InvalidIntermediateQueryException {
        Queue<QueryNode> queryNodeToVisit = new LinkedList<>();

        queryNodeToVisit.add(query.getRootConstructionNode());

        while (!queryNodeToVisit.isEmpty()) {
            QueryNode node = queryNodeToVisit.poll();

            if (node instanceof LeftJoinNode) {
                if (query.getChildren(node).size() != 2) {
                    throw new IllegalStateException("LeftJoinNode must have 2 children node.");
                }
            }
            if (node instanceof GroupNode) {
                if (query.getChildren(node).size() != 1) {
                    throw new IllegalStateException("GroupNode must have a child.");
                }
            }
            if (node instanceof JoinLikeNode) {
                if (query.getChildren(node).size() < 2) {
                    throw new IllegalStateException("JoinLikeNode must have at least 2 children node.");
                }
            }
            if (node instanceof FilterNode) {
                if (query.getChildren(node).size() != 1) {
                    throw new IllegalStateException("FilterNode must have a child.");
                }
            }
            if (node instanceof  ConstructionNode) {
                if (query.getChildren(node).size() > 1) {
                    throw new IllegalStateException("ConstructionNode can not have more than one child.");
                }
            }
            if (node instanceof EmptyNode) {
                if (query.getChildren(node).size() != 0) {
                    throw new IllegalStateException("EmptyNode can not have a child.");
                }
            }
            if (node instanceof DataNode) {
                if (query.getChildren(node).size() != 0) {
                    throw new IllegalStateException("DataNode can not have a child.");
                }
            }
            if (node instanceof UnionNode) {
                if (query.getChildren(node).size() < 2) {
                    throw new IllegalStateException("UnionNode must have at least 2 children node.");
                }

                ImmutableSet<Variable> unionProjectedVariables = ((UnionNode) node).getProjectedVariables();

                for (QueryNode child : query.getChildren(node)) {
                    ImmutableSet<Variable> childProjectedVariables = query.getProjectedVariables(child);

                    if (!childProjectedVariables.containsAll(unionProjectedVariables)) {
                        throw new IllegalStateException("This child " + child
                                + "does not project all the variables " +
                                "required by the UNION node (" + unionProjectedVariables + ")");
                    }
                }
            }

            queryNodeToVisit.addAll(query.getChildren(node));
        }
    }
}
