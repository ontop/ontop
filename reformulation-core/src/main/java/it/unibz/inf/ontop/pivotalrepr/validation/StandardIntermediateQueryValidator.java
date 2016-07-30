package it.unibz.inf.ontop.pivotalrepr.validation;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;

/**
 * Checks the QueryNode and their children
 */
public class StandardIntermediateQueryValidator implements IntermediateQueryValidator {

    protected static class ValidationVisitor implements QueryNodeVisitor {

        private final IntermediateQuery query;

        protected ValidationVisitor(IntermediateQuery query) {
            this.query = query;
        }

        @Override
        public void visit(ConstructionNode constructionNode) {
            if (query.getChildren(constructionNode).size() > 1) {
                throw new InvalidIntermediateQueryException("CONSTRUCTION node " + constructionNode
                        + " has more than one child.\n" + query);
            }
        }

        @Override
        public void visit(UnionNode unionNode) {
            if (query.getChildren(unionNode).size() < 2) {
                throw new InvalidIntermediateQueryException("UNION node " + unionNode
                        +" does not have at least 2 children node.");
            }

            ImmutableSet<Variable> unionProjectedVariables = unionNode.getProjectedVariables();

            for (QueryNode child : query.getChildren(unionNode)) {
                ImmutableSet<Variable> childProjectedVariables = query.getProjectedVariables(child);

                if (!childProjectedVariables.containsAll(unionProjectedVariables)) {
                    throw new InvalidIntermediateQueryException("This child " + child
                            + "does not project all the variables " +
                            "required by the UNION node (" + unionProjectedVariables + ")");
                }
            }
        }

        @Override
        public void visit(InnerJoinNode innerJoinNode) {
            if (query.getChildren(innerJoinNode).size() < 2) {
                throw new InvalidIntermediateQueryException("JOIN node " + innerJoinNode
                        +" does not have at least 2 children.");
            }
        }

        @Override
        public void visit(LeftJoinNode leftJoinNode) {
            if (query.getChildren(leftJoinNode).size() != 2) {
                throw new InvalidIntermediateQueryException("LEFTJOIN node " + leftJoinNode
                        + " does not have 2 children.");
            }
        }

        @Override
        public void visit(FilterNode filterNode) {
            if (query.getChildren(filterNode).size() != 1) {
                throw new InvalidIntermediateQueryException("FILTER node " + filterNode
                        + " does not have single child.");
            }
        }

        @Override
        public void visit(IntensionalDataNode intensionalDataNode) {
            if (query.getChildren(intensionalDataNode).size() != 0) {
                throw new InvalidIntermediateQueryException("DATA node "+ intensionalDataNode
                        + " has a child.");
            }
        }

        @Override
        public void visit(ExtensionalDataNode extensionalDataNode) {
            if (query.getChildren(extensionalDataNode).size() != 0) {
                throw new InvalidIntermediateQueryException("DATA node "+ extensionalDataNode
                        + " has a child.");
            }
        }

        @Override
        public void visit(GroupNode groupNode) {
            if (query.getChildren(groupNode).size() != 1) {
                throw new InvalidIntermediateQueryException("GROUP node " + groupNode
                        + " does not have a child.");
            }
        }

        @Override
        public void visit(EmptyNode emptyNode) {
            if (query.getChildren(emptyNode).size() != 0) {
                throw new InvalidIntermediateQueryException("EMPTY node " + emptyNode
                        + " has a child.");
            }
        }
    }

    @Override
    public void validate(IntermediateQuery query) throws InvalidIntermediateQueryException {

        QueryNodeVisitor visitor = new ValidationVisitor(query);

        /**
         * May throw an InvalidIntermediateQueryException
         */
        query.getNodesInTopDownOrder().stream()
                .forEach(n -> n.acceptVisitor(visitor));
    }
}
