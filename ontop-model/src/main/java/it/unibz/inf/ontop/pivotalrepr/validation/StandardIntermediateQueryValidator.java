package it.unibz.inf.ontop.pivotalrepr.validation;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;

import java.util.Optional;

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
            if (query.getChildren(constructionNode).size() > 1) {
                throw new InvalidIntermediateQueryException("CONSTRUCTION node " + constructionNode
                        + " has more than one child.\n" + query);
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
        }

        @Override
        public void visit(LeftJoinNode leftJoinNode) {
            if (query.getChildren(leftJoinNode).size() != 2) {
                throw new InvalidIntermediateQueryException("LEFTJOIN node " + leftJoinNode
                        + " does not have 2 children.\n" + query);
            }
        }

        @Override
        public void visit(FilterNode filterNode) {
            if (query.getChildren(filterNode).size() != 1) {
                throw new InvalidIntermediateQueryException("FILTER node " + filterNode
                        + " does not have single child.\n" + query);
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
    }

    /**
     * To be overloaded
     */
    protected ValidationVisitor createVisitor(IntermediateQuery query) {
        return new ValidationVisitor(query);
    }

    @Override
    public void validate(IntermediateQuery query) throws InvalidIntermediateQueryException {
        QueryNodeVisitor visitor = createVisitor(query);

        /**
         * May throw an InvalidIntermediateQueryException
         */
        query.getNodesInTopDownOrder().stream()
                .forEach(n -> n.acceptVisitor(visitor));
    }
}
