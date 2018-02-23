package it.unibz.inf.ontop.answering.reformulation.unfolding.impl;


import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.tools.RootConstructionNodeEnforcer;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.iq.optimizer.TrueNodesRemovalOptimizer;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.proposal.QueryMergingProposal;
import it.unibz.inf.ontop.iq.proposal.impl.QueryMergingProposalImpl;
import it.unibz.inf.ontop.answering.reformulation.unfolding.QueryUnfolder;

import java.util.Optional;

public class BasicQueryUnfolder implements QueryUnfolder {

    private final Mapping mapping;
    private final RootConstructionNodeEnforcer rootCnEnforcer;

    @AssistedInject
    private BasicQueryUnfolder(@Assisted Mapping mapping, RootConstructionNodeEnforcer rootCnEnforcer) {
        this.mapping = mapping;
        this.rootCnEnforcer = rootCnEnforcer;
    }

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {

        // Non-final
        Optional<IntensionalDataNode> optionalCurrentIntensionalNode = query.getIntensionalNodes().findFirst();


        while (optionalCurrentIntensionalNode.isPresent()) {

            IntensionalDataNode intensionalNode = optionalCurrentIntensionalNode.get();

            Optional<IntermediateQuery> optionalMappingAssertion = mapping.getDefinition(
                    intensionalNode.getProjectionAtom().getPredicate());

            query = rootCnEnforcer.enforceRootCn(query);
            QueryMergingProposal queryMerging = new QueryMergingProposalImpl(intensionalNode, optionalMappingAssertion);
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
