package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import it.unibz.inf.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.NextNodeAndQuery;
import it.unibz.inf.ontop.pivotalrepr.*;

import static it.unibz.inf.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.getDepthFirstNextNode;

/**
 * TODO: explain
 */
public class TopDownSubstitutionLiftOptimizer implements SubstitutionLiftOptimizer {

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        // Non-final
        NextNodeAndQuery nextNodeAndQuery = new NextNodeAndQuery(
                query.getFirstChild(query.getRootConstructionNode()),
                query);

        while (nextNodeAndQuery.getOptionalNextNode().isPresent()) {
            nextNodeAndQuery = liftBindings(nextNodeAndQuery.getNextQuery(),
                    nextNodeAndQuery.getOptionalNextNode().get());
        }
        return nextNodeAndQuery.getNextQuery();
    }

    private NextNodeAndQuery liftBindings(IntermediateQuery currentQuery, QueryNode currentNode)
            throws EmptyQueryException {

        if (currentNode instanceof ConstructionNode) {
            return liftBindingsFromConstructionNode(currentQuery, (ConstructionNode) currentNode);
        }
        else if (currentNode instanceof CommutativeJoinNode) {
            return liftBindingsFromCommutativeJoinNode(currentQuery, (CommutativeJoinNode) currentNode);

        }
        else if (currentNode instanceof LeftJoinNode) {
            return liftBindingsFromLeftJoinNode(currentQuery, (LeftJoinNode) currentNode);
        }
        /**
         * Other nodes: does nothing
         */
        else {
            return new NextNodeAndQuery(getDepthFirstNextNode(currentQuery, currentNode), currentQuery);
        }
    }

    private NextNodeAndQuery liftBindingsFromConstructionNode(IntermediateQuery currentQuery, ConstructionNode currentNode) {
        throw new RuntimeException("TODO: implement Construction node handling");
    }

    private NextNodeAndQuery liftBindingsFromCommutativeJoinNode(IntermediateQuery currentQuery,
                                                                 CommutativeJoinNode currentNode) {
        throw new RuntimeException("TODO: implement Join handling");
    }

    private NextNodeAndQuery liftBindingsFromLeftJoinNode(IntermediateQuery currentQuery, LeftJoinNode currentNode) {
        throw new RuntimeException("TODO: implement LJ handling");
    }
}
