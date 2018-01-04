package it.unibz.inf.ontop.reformulation.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.optimizer.TrueNodesRemovalOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.TopDownBindingLiftOptimizer;
import it.unibz.inf.ontop.iq.proposal.QueryMergingProposal;
import it.unibz.inf.ontop.iq.proposal.impl.QueryMergingProposalImpl;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.reformulation.RuleUnfolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class RuleUnfolderImpl implements RuleUnfolder {

    private final TrueNodesRemovalOptimizer trueNodesRemovalOptimizer;
    private final TopDownBindingLiftOptimizer topDownBindingLiftOptimizer;
    private static final Logger log = LoggerFactory.getLogger(RuleUnfolder.class);
    private static final int LOOPS = 10;

    @Inject
    public RuleUnfolderImpl(TrueNodesRemovalOptimizer trueNodesRemovalOptimizer, TopDownBindingLiftOptimizer topDownBindingLiftOptimizer) {
        this.trueNodesRemovalOptimizer = trueNodesRemovalOptimizer;
        this.topDownBindingLiftOptimizer = topDownBindingLiftOptimizer;
    }

    //BasicQueryUnfolder.optimize
    // TODO: follow the steps in QuestQueryProcessor by injecting TranslationFactory
    @Override
    public IntermediateQuery unfold(IntermediateQuery query, ImmutableMap<AtomPredicate, IntermediateQuery> mappingMap) throws EmptyQueryException {
        // Non-final
        Optional<IntensionalDataNode> optionalCurrentIntensionalNode = query.getIntensionalNodes().findFirst();


        while (optionalCurrentIntensionalNode.isPresent()) {

            IntensionalDataNode intensionalNode = optionalCurrentIntensionalNode.get();

            Optional<IntermediateQuery> optionalMappingAssertion = Optional.ofNullable(mappingMap.get(
                    intensionalNode.getProjectionAtom().getPredicate()));

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

    //FixedPointBindingLiftOptimizer.optimize
    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {

        int oldVersionNumber;
        int countVersion = 0;

        do {
            oldVersionNumber = query.getVersionNumber();

            query = topDownBindingLiftOptimizer.optimize(query);
            log.trace("New query after substitution lift optimization: \n" + query.toString());
            countVersion++;

            if(countVersion == LOOPS){
                throw new IllegalStateException("Too many substitution lift optimizations are executed");
            }

        } while( oldVersionNumber != query.getVersionNumber() );

        return  trueNodesRemovalOptimizer.optimize(query);
    }
}
