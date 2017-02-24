package it.unibz.inf.ontop.answering.reformulation.unfolding.impl;


import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.TrueNodesRemovalOptimizer;
import it.unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import it.unibz.inf.ontop.pivotalrepr.IntensionalDataNode;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryMergingProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.QueryMergingProposalImpl;
import it.unibz.inf.ontop.answering.reformulation.unfolding.QueryUnfolder;

import java.util.Optional;

public class BasicQueryUnfolder implements QueryUnfolder {

    private final Mapping mapping;

    @AssistedInject
    private BasicQueryUnfolder(@Assisted Mapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {

        // Non-final
        Optional<IntensionalDataNode> optionalCurrentIntensionalNode = query.getIntensionalNodes().findFirst();


        while (optionalCurrentIntensionalNode.isPresent()) {

            IntensionalDataNode intensionalNode = optionalCurrentIntensionalNode.get();

            Optional<IntermediateQuery> optionalMapping = mapping.getDefinition(
                    intensionalNode.getProjectionAtom().getPredicate());

            QueryMergingProposal queryMerging = new QueryMergingProposalImpl(intensionalNode, optionalMapping);
            query.applyProposal(queryMerging);

            /**
             * Next intensional node
             *
             * NB: some intensional nodes may have dropped during the last merge
             */
            optionalCurrentIntensionalNode = query.getIntensionalNodes().findFirst();
        }

        // remove unnecessary TrueNodes, which may have been introduced during substitution lift
        return new TrueNodesRemovalOptimizer().optimize(query);
    }
}
