package it.unibz.inf.ontop.owlrefplatform.core.optimization.unfolding.impl;


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.InjectiveVar2VarSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.TrueNodesRemovalOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import it.unibz.inf.ontop.pivotalrepr.IntensionalDataNode;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryRenamer;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryMergingProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.QueryMergingProposalImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

public class QueryUnfolderImpl implements QueryUnfolder {

    private final ImmutableMap<AtomPredicate, IntermediateQuery> mappingIndex;

    public  QueryUnfolderImpl(Stream<IntermediateQuery> mappingStream) {
        AtomicInteger i = new AtomicInteger(0);
        mappingIndex = mappingStream.sequential()
              .map(m -> appendSuffixToVariableNames(m, i.incrementAndGet()))
              .map(m -> new AbstractMap.SimpleEntry<>(m.getProjectionAtom().getPredicate(), m))
              .collect(ImmutableCollectors.toMap());
    }

    @Override
    public ImmutableMap<AtomPredicate, IntermediateQuery> getMappingIndex() {
        return mappingIndex;
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

    private IntermediateQuery appendSuffixToVariableNames(IntermediateQuery query, int suffix) {
        Map<Variable, Variable> substitutionMap =
                query.getKnownVariables().stream()
                        .collect(Collectors.toMap(v -> v, v -> DATA_FACTORY.getVariable(v.getName()+"m"+suffix)));
        QueryRenamer queryRenamer = new QueryRenamer(new InjectiveVar2VarSubstitutionImpl(substitutionMap));
        return queryRenamer.transform(query);
    }
}
