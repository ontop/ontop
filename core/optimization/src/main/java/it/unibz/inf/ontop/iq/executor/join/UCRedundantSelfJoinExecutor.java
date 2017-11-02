package it.unibz.inf.ontop.iq.executor.join;


import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.iq.node.DataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;

import java.util.Optional;

/**
 * Uses unique constraints to detect and remove redundant self inner joins
 */
@Singleton
public class UCRedundantSelfJoinExecutor extends RedundantSelfJoinExecutor {

    @Inject
    private UCRedundantSelfJoinExecutor(IntermediateQueryFactory iqFactory,
                                        SubstitutionFactory substitutionFactory,
                                        ImmutableUnificationTools unificationTools) {
        super(iqFactory, substitutionFactory, unificationTools);
    }


    /**
     * TODO: explain
     *
     */
    @Override
    protected Optional<PredicateLevelProposal> proposePerPredicate(InnerJoinNode joinNode, ImmutableCollection<DataNode> initialNodes,
                                                                   AtomPredicate predicate, DBMetadata dbMetadata,
                                                                   ImmutableList<Variable> priorityVariables,
                                                                   IntermediateQuery query)
            throws AtomUnificationException {
        ImmutableMultimap<AtomPredicate, ImmutableList<Integer>> uniqueConstraints = dbMetadata.getUniqueConstraints();

        if (uniqueConstraints.containsKey(predicate)) {
            ImmutableMultimap<ImmutableList<VariableOrGroundTerm>, DataNode> groupingMap = groupByUniqueConstraintArguments(
                    initialNodes, uniqueConstraints.get(predicate));
            return Optional.of(proposeForGroupingMap(groupingMap));
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * dataNodes and UC positions are given for the same predicate
     * TODO: explain
     */
    private static ImmutableMultimap<ImmutableList<VariableOrGroundTerm>, DataNode> groupByUniqueConstraintArguments(
            ImmutableCollection<DataNode> dataNodes,
            ImmutableCollection<ImmutableList<Integer>> collectionOfUCPositions) {
        ImmutableMultimap.Builder<ImmutableList<VariableOrGroundTerm>, DataNode> groupingMapBuilder = ImmutableMultimap.builder();

        for (ImmutableList<Integer> primaryKeyPositions : collectionOfUCPositions) {
            for (DataNode dataNode : dataNodes) {
                groupingMapBuilder.put(extractArguments(dataNode.getProjectionAtom(), primaryKeyPositions), dataNode);
            }
        }
        return groupingMapBuilder.build();
    }
}
