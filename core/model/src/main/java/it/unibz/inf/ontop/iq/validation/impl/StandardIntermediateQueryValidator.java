package it.unibz.inf.ontop.iq.validation.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.validation.IntermediateQueryValidator;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

/**
 * Checks the QueryNode and their children
 */
@Singleton
public class StandardIntermediateQueryValidator implements IntermediateQueryValidator {

    @Inject
    protected StandardIntermediateQueryValidator() {
    }

    protected static class ValidationVisitor implements QueryNodeVisitor {

        private final IntermediateQuery query;

        protected ValidationVisitor(IntermediateQuery query) {
            this.query = query;
        }

        protected IntermediateQuery getQuery() {
            return query;
        }

        @Override
        public void visit(ConstructionNode constructionNode) {
            if (query.getChildren(constructionNode).size() != 1) {
                throw new InvalidIntermediateQueryException("CONSTRUCTION node " + constructionNode
                        + " must have ONE and ONLY ONE child.\n" + query);
            }

            ImmutableSet<Variable> requiredChildVariables = constructionNode.getChildVariables();

            for (QueryNode child : query.getChildren(constructionNode)) {
                ImmutableSet<Variable> childProjectedVariables = query.getVariables(child);

                if (!childProjectedVariables.containsAll(requiredChildVariables)) {
                    throw new InvalidIntermediateQueryException("This child " + child
                            + " does not project all the variables " +
                            "required by the CONSTRUCTION node (" + requiredChildVariables + ")\n" + query);
                }
            }
        }

        @Override
        public void visit(AggregationNode aggregationNode) {
            // TODO: shall we implement it? Only relevant for IntermediateQuery
        }

        @Override
        public void visit(UnionNode unionNode) {
            if (query.getChildren(unionNode).size() < 2) {
                throw new InvalidIntermediateQueryException("UNION node " + unionNode
                        +" does not have at least 2 children node.");
            }

            ImmutableSet<Variable> unionProjectedVariables = unionNode.getVariables();

            for (QueryNode child : query.getChildren(unionNode)) {
                ImmutableSet<Variable> childProjectedVariables = query.getVariables(child);

                if (!childProjectedVariables.containsAll(unionProjectedVariables)) {
                    throw new InvalidIntermediateQueryException("This child " + child
                            + " does not project all the variables " +
                            "required by the UNION node (" + unionProjectedVariables + ")\n" + query);
                }
            }
        }

        @Override
        public void visit(InnerJoinNode innerJoinNode) {
            if (query.getChildren(innerJoinNode).size() < 2) {
                throw new InvalidIntermediateQueryException("JOIN node " + innerJoinNode
                        +" does not have at least 2 children.\n" + query);
            }

            innerJoinNode.getOptionalFilterCondition()
                    .ifPresent(e -> checkExpression(innerJoinNode, e));
        }

        @Override
        public void visit(LeftJoinNode leftJoinNode) {
            if (query.getChildren(leftJoinNode).size() != 2) {
                throw new InvalidIntermediateQueryException("LEFTJOIN node " + leftJoinNode
                        + " does not have 2 children.\n" + query);
            }
            leftJoinNode.getOptionalFilterCondition()
                    .ifPresent(e -> checkExpression(leftJoinNode, e));
        }

        @Override
        public void visit(FilterNode filterNode) {
            if (query.getChildren(filterNode).size() != 1) {
                throw new InvalidIntermediateQueryException("FILTER node " + filterNode
                        + " does not have single child.\n" + query);
            }
            checkExpression(filterNode, filterNode.getFilterCondition());
        }

        private void checkExpression(JoinOrFilterNode node, ImmutableExpression expression) {
            ImmutableSet<Variable> unboundVariables = expression.getVariableStream()
                    .filter(v -> !(query.getChildren(node).stream()
                            .flatMap(c -> query.getVariables(c).stream())
                            .collect(ImmutableCollectors.toSet())
                            .contains(v)))
                    .collect(ImmutableCollectors.toSet());
            if (!unboundVariables.isEmpty()) {
                throw new InvalidIntermediateQueryException("Expression " + expression + " of "
                        + expression + " uses unbound variables (" + unboundVariables +  ").\n" + query);
            }
        }

        @Override
        public void visit(IntensionalDataNode intensionalDataNode) {
            if (query.getChildren(intensionalDataNode).size() != 0) {
                throw new InvalidIntermediateQueryException("DATA node "+ intensionalDataNode
                        + " has a child.\n" + query);
            }
        }

        @Override
        public void visit(ExtensionalDataNode extensionalDataNode) {
            if (query.getChildren(extensionalDataNode).size() != 0) {
                throw new InvalidIntermediateQueryException("DATA node "+ extensionalDataNode
                        + " has a child.\n" + query);
            }
        }

        @Override
        public void visit(EmptyNode emptyNode) {
            if (query.getChildren(emptyNode).size() != 0) {
                throw new InvalidIntermediateQueryException("EMPTY node " + emptyNode
                        + " has a child.\n" + query);
            }
        }

        @Override
        public void visit(TrueNode trueNode) {
            if (query.getChildren(trueNode).size() != 0) {
                throw new InvalidIntermediateQueryException("TRUENODE node " + trueNode
                        + " has a child.\n" + query);
            }
        }

        @Override
        public void visit(DistinctNode distinctNode) {
            if (query.getChildren(distinctNode).size() != 1) {
                throw new InvalidIntermediateQueryException("DISTINCT node " + distinctNode
                        + " must have ONE and ONLY ONE child.\n" + query);
            }
        }

        @Override
        public void visit(SliceNode sliceNode) {
            if (query.getChildren(sliceNode).size() != 1) {
                throw new InvalidIntermediateQueryException("SLICE node " + sliceNode
                        + " must have ONE and ONLY ONE child.\n" + query);
            }
        }

        @Override
        public void visit(OrderByNode orderByNode) {
            if (query.getChildren(orderByNode).size() != 1) {
                throw new InvalidIntermediateQueryException("ORDER BY node " + orderByNode
                        + " must have ONE and ONLY ONE child.\n" + query);
            }

            ImmutableSet<Variable> requiredVariables = orderByNode.getLocallyRequiredVariables();

            for (QueryNode child : query.getChildren(orderByNode)) {
                if (!query.getVariables(child).containsAll(requiredVariables)) {
                    throw new InvalidIntermediateQueryException("Some variables used in the node " + this
                            + " are not provided by its child " + child);
                }
            }
        }

        @Override
        public void visit(FlattenNode flattenNode) {
            ImmutableList<QueryNode> children = query.getChildren(flattenNode);

            if (children.size() != 1)
                throw new InvalidIntermediateQueryException("A flatten node must have exactly one child");

            QueryNode child = children.get(0);
            ImmutableSet<Variable> childVariables = query.getVariables(child);

            if (!childVariables.contains(flattenNode.getFlattenedVariable())) {
                throw new InvalidIntermediateQueryException("The array variable (" + flattenNode.getFlattenedVariable()
                        + ") of a flatten node must be defined by its sub-tree.\n " + query);
            }
        }
    }

    /**
     * To be overloaded
     */
    protected ValidationVisitor createVisitor(IntermediateQuery query) {
        return new ValidationVisitor(query);
    }

    @Override
    public void validate(IntermediateQuery query) throws InvalidIntermediateQueryException {
        validateProjectedVariables(query);


        QueryNodeVisitor visitor = createVisitor(query);

        /*
         * May throw an InvalidIntermediateQueryException
         */
        query.getNodesInTopDownOrder()
                .forEach(n -> n.acceptVisitor(visitor));
    }

    private void validateProjectedVariables(IntermediateQuery query) throws InvalidIntermediateQueryException {
        ImmutableSet<Variable> projectedVariables = query.getVariables(query.getRootNode());
        if (!projectedVariables.equals(query.getProjectionAtom().getVariables())) {
            throw new InvalidIntermediateQueryException("The variables projected by the root node"
                    +  projectedVariables + " do not match the projection atom " + query.getProjectionAtom());
        }
    }
}
