package it.unibz.inf.ontop.owlrefplatform.core.optimization.unfolding.impl;


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryMergingProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.QueryMergingProposalImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap;
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

        query.getIntensionalNodes();
        for( IntermediateQuery mapping : mappingIndex.values()){

            QueryMergingProposal queryMerging = new QueryMergingProposalImpl(mapping);

            query.applyProposal(queryMerging, true);

        }

        return query;
//        throw new RuntimeException("TODO: implement query unfolding");
    }
}
