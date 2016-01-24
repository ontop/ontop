package org.semanticweb.ontop.owlrefplatform.core.optimization;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.DataNode;
import org.semanticweb.ontop.pivotalrepr.EmptyQueryException;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.GroundTermRemovalFromDataNodeProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.ProposalResults;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.GroundTermRemovalFromDataNodeProposalImpl;

/**
 * Collects all the data nodes that contains some ground terms
 * and proposes to move them of the data nodes (in filter or join-like nodes)
 */
public class GroundTermRemovalFromDataNodeReshaper implements IntermediateQueryOptimizer {

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) {
        return reshape(query);
    }

    private IntermediateQuery reshape(IntermediateQuery query) {
        Optional<GroundTermRemovalFromDataNodeProposal> optionalProposal = makeProposal(query);
        if (optionalProposal.isPresent()) {
            GroundTermRemovalFromDataNodeProposal proposal = optionalProposal.get();
            try {
                ProposalResults results = query.applyProposal(proposal);
                return results.getResultingQuery();

            } catch (EmptyQueryException e) {
                throw new IllegalStateException("Inconsistency: GroundTermRemovalFromDataNodeReshaper should empty the query ");
            }
        }
        else {
            return query;
        }
    }

    private Optional<GroundTermRemovalFromDataNodeProposal> makeProposal(IntermediateQuery query) {
        ImmutableList.Builder<DataNode> dataNodesToSimplifyBuilder = ImmutableList.builder();
        for (QueryNode node : query.getNodesInTopDownOrder()) {
            if (node instanceof DataNode) {
                DataNode dataNode = (DataNode) node;
                if (dataNode.getProjectionAtom().containsGroundTerms()) {
                    dataNodesToSimplifyBuilder.add(dataNode);
                }
            }
        }

        ImmutableList<DataNode> dataNodesToSimplify = dataNodesToSimplifyBuilder.build();
        if (dataNodesToSimplify.isEmpty()) {
            return Optional.empty();
        }
        else {
            GroundTermRemovalFromDataNodeProposal proposal = new GroundTermRemovalFromDataNodeProposalImpl(
                    dataNodesToSimplify);
            return Optional.of(proposal);
        }
    }

}
