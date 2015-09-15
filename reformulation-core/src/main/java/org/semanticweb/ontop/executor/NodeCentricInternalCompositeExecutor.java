package org.semanticweb.ontop.executor;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import org.semanticweb.ontop.pivotalrepr.proposal.ProposalResults;

/**
 * TODO: explain
 */
public abstract class NodeCentricInternalCompositeExecutor<P extends NodeCentricOptimizationProposal, N extends QueryNode>
        extends InternalCompositeExecutor<P> {
    @Override
    protected Optional<P> createNewProposal(ProposalResults results) {
        if (!(results instanceof NodeCentricOptimizationResults)) {
            throw new IllegalArgumentException("Unexpected type of results: " + results.getClass());
        }

        NodeCentricOptimizationResults nodeCentricResults = (NodeCentricOptimizationResults) results;

        Optional<QueryNode> optionalNewNode = nodeCentricResults.getOptionalNewNode();
        if (optionalNewNode.isPresent()) {
            try {
                N newFocusNode = (N) optionalNewNode.get();
                return createNewProposalFromFocusNode(newFocusNode);
            }
            /**
             * Deals with the limits of Java generics...
             */
            catch (ClassCastException e) {
               return Optional.absent();
            }
        }
        else {
            return Optional.absent();
        }

    }

    protected abstract Optional<P> createNewProposalFromFocusNode(N focusNode);
}
