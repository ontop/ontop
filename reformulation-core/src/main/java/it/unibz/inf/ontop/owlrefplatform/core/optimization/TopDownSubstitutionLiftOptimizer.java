package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.NextNodeAndQuery;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.SubstitutionPropagationProposalImpl;

import java.util.Optional;

import static it.unibz.inf.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.getDepthFirstNextNode;

/**
 * TODO: explain
 */
public class TopDownSubstitutionLiftOptimizer implements SubstitutionLiftOptimizer {

    private ImmutableSubstitution<ImmutableTerm> substitutionToPropagate;

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

    private NextNodeAndQuery liftBindingsFromCommutativeJoinNode(IntermediateQuery initialQuery,
                                                                 CommutativeJoinNode initialJoinNode)
            throws EmptyQueryException {

        UnionFriendlyBindingExtractor extractor = new UnionFriendlyBindingExtractor();

        // Non-final
        Optional<QueryNode> optionalCurrentChild = initialQuery.getFirstChild(initialJoinNode);
        IntermediateQuery currentQuery = initialQuery;
        QueryNode currentJoinNode = initialJoinNode;


        while (optionalCurrentChild.isPresent()) {
            QueryNode currentChild = optionalCurrentChild.get();

            Optional<ImmutableSubstitution<ImmutableTerm>> optionalSubstitution = extractor.extractInSubTree(
                    currentQuery, currentChild);

            /**
             * Applies the substitution to the child
             */
            if (optionalSubstitution.isPresent()) {
                SubstitutionPropagationProposal<QueryNode> proposal =
                        new SubstitutionPropagationProposalImpl<>(currentChild, optionalSubstitution.get());

                NodeCentricOptimizationResults<QueryNode> results = currentQuery.applyProposal(proposal);
                currentQuery = results.getResultingQuery();
                optionalCurrentChild = results.getOptionalNextSibling();
                currentJoinNode = currentQuery.getParent(
                        results.getNewNodeOrReplacingChild()
                                .orElseThrow(() -> new IllegalStateException(
                                        "The focus was expected to be kept or replaced, not removed")))
                        .orElseThrow(() -> new IllegalStateException(
                                "The focus node should still have a parent (a Join node)"));
            }
            else {
                optionalCurrentChild = currentQuery.getNextSibling(currentChild);
            }
        }
        return new NextNodeAndQuery(getDepthFirstNextNode(currentQuery, currentJoinNode), currentQuery);
    }

    private NextNodeAndQuery liftBindingsFromLeftJoinNode(IntermediateQuery currentQuery, LeftJoinNode currentNode) {
        throw new RuntimeException("TODO: implement LJ handling");
    }

    /**
     *  Applies the substitution
     */
    private static <N extends QueryNode> N propagateSubstitution(IntermediateQuery query,
                                                                 Optional<ImmutableSubstitution<ImmutableTerm>> optionalSubstitution,
                                                                 N topNode) {

        if (optionalSubstitution.isPresent()) {
            SubstitutionPropagationProposal<N> propagationProposal = new SubstitutionPropagationProposalImpl<>(
                    topNode, optionalSubstitution.get());

            try {
                NodeCentricOptimizationResults<N> results = query.applyProposal(propagationProposal, true);

                return results.getOptionalNewNode()
                        .orElseThrow(() -> new IllegalStateException(
                                "No focus node returned after the substitution propagation"));

            } catch (EmptyQueryException e) {
                throw new IllegalStateException("Internal inconsistency error: propagation the substitution " +
                        "leads to an empty query: " + optionalSubstitution.get());
            }
        }

            /**
             * No substitution to propagate
             */
            else {
                return topNode;
            }
    }
}
