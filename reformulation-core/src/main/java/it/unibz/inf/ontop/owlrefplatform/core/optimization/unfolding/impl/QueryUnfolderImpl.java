package it.unibz.inf.ontop.owlrefplatform.core.optimization.unfolding.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import it.unibz.inf.ontop.pivotalrepr.IntensionalDataNode;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryMergingProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.QueryMergingProposalImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap;
import java.util.Optional;
import java.util.stream.Stream;

public class QueryUnfolderImpl implements QueryUnfolder {

    private final ImmutableMap<AtomPredicate, IntermediateQuery> mappingIndex;

    public  QueryUnfolderImpl(Stream<IntermediateQuery> mappingStream) {
        mappingIndex = mappingStream
                .map(m -> new AbstractMap.SimpleEntry<>(m.getProjectionAtom().getPredicate(), m))
                .collect(ImmutableCollectors.toMap());
    }


    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {

        // Non-final
        Optional<IntensionalDataNode> optionalCurrentIntensionalNode = query.getIntensionalNodes().findFirst();


        while (optionalCurrentIntensionalNode.isPresent()) {

            IntensionalDataNode intensionalNode = optionalCurrentIntensionalNode.get();

            Optional<IntermediateQuery> optionalMapping = Optional.ofNullable(
                    mappingIndex.get(intensionalNode.getProjectionAtom().getPredicate()));

            QueryMergingProposal queryMerging = new QueryMergingProposalImpl(intensionalNode, optionalMapping);
            query.applyProposal(queryMerging, true);

            /**
             * Next intensional node
             *
             * NB: some intensional nodes may have dropped during the last merge
             */
            optionalCurrentIntensionalNode = query.getIntensionalNodes().findFirst();
        }
        return query;
    }
}
