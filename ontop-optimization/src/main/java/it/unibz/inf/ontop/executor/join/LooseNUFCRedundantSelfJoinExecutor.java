package it.unibz.inf.ontop.executor.join;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopOptimizationSettings;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.pivotalrepr.DataNode;
import it.unibz.inf.ontop.sql.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.injection.OntopModelSettings.CardinalityPreservationMode.LOOSE;

/**
 * Uses non-unique functional constraints to detect and remove redundant self inner joins.
 *
 * Does nothing if the CardinalityPreservationMode is not LOOSE (it does not guarantee its preservation).
 *
 */
@Singleton
public class LooseNUFCRedundantSelfJoinExecutor extends RedundantSelfJoinExecutor {

    private final OntopOptimizationSettings settings;

    @Inject
    private LooseNUFCRedundantSelfJoinExecutor(IntermediateQueryFactory iqFactory, OntopOptimizationSettings settings) {
        super(iqFactory);
        this.settings = settings;
    }

    @Override
    protected Optional<PredicateLevelProposal> proposePerPredicate(ImmutableCollection<DataNode> initialNodes,
                                                                   AtomPredicate predicate, DBMetadata dbMetadata,
                                                                   ImmutableList<Variable> priorityVariables)
            throws AtomUnificationException {

        if (initialNodes.size() < 2)
            return Optional.empty();

        RelationID relationId = Relation2DatalogPredicate.createRelationFromPredicateName(
                dbMetadata.getQuotedIDFactory(), predicate);
        DatabaseRelationDefinition databaseRelation = dbMetadata.getDatabaseRelation(relationId);

        /*
         * Does nothing
         */
        if (databaseRelation == null)
            return Optional.empty();

        ImmutableMap<NonUniqueFunctionalConstraint, ImmutableCollection<Collection<DataNode>>> constraintNodeMap =
                databaseRelation.getNonUniqueFunctionalConstraints().stream()
                    .collect(ImmutableCollectors.toMap(
                        c -> c,
                        c -> groupDataNodesPerConstraint(c, initialNodes)));

        ImmutableList<ImmutableSubstitution<VariableOrGroundTerm>> dependentUnifiers =
                constraintNodeMap.entrySet().stream()
                        .flatMap(e -> extractDependentUnifiers(e.getKey(), e.getValue()))
                        .collect(ImmutableCollectors.toList());

        /*
         * Does not look for redundant joins if not in the LOOSE preservation mode
         */
        if (settings.getCardinalityPreservationMode() != LOOSE)
            return Optional.of(new PredicateLevelProposal(dependentUnifiers, ImmutableList.of()));

        throw new RuntimeException("TODO: continue");
    }

    private ImmutableCollection<Collection<DataNode>> groupDataNodesPerConstraint(
            NonUniqueFunctionalConstraint constraint, ImmutableCollection<DataNode> initialNodes) {

        ImmutableList<Integer> constraintDeterminantIndexes = constraint.getDeterminants().stream()
                .map(Attribute::getIndex)
                .collect(ImmutableCollectors.toList());

        ImmutableMultimap<ImmutableList<VariableOrGroundTerm>, DataNode> nodeMultiMap = initialNodes.stream()
                .collect(ImmutableCollectors.toMultimap(
                        n -> extractDeterminantArguments(n.getProjectionAtom(), constraintDeterminantIndexes),
                        n -> n));

        return nodeMultiMap.asMap().values();
    }

    private ImmutableList<VariableOrGroundTerm> extractDeterminantArguments(DataAtom dataAtom,
                                                                            ImmutableList<Integer> determinantIndexes) {
        ImmutableList<? extends VariableOrGroundTerm> arguments = dataAtom.getArguments();
        return determinantIndexes.stream()
                .map(i -> arguments.get(i - 1))
                .collect(ImmutableCollectors.toList());
    }

    private Stream<ImmutableSubstitution<VariableOrGroundTerm>> extractDependentUnifiers(
            NonUniqueFunctionalConstraint constraint, ImmutableCollection<Collection<DataNode>> dataNodeClusters) {
        ImmutableList<Integer> dependentIndexes = constraint.getDependents().stream()
                .map(d -> d.getIndex() - 1)
                .collect(ImmutableCollectors.toList());

        return dataNodeClusters.stream()
                .flatMap(c -> extractDependentUnifiersFromCluster(dependentIndexes, c));

    }

    private Stream<ImmutableSubstitution<VariableOrGroundTerm>> extractDependentUnifiersFromCluster(
            ImmutableList<Integer> dependentIndexes, Collection<DataNode> cluster) {
        if (cluster.size() < 2)
            return Stream.empty();

        Iterator<DataNode> clusterIterator = cluster.iterator();
        DataNode firstDataNode = clusterIterator.next();

        /*
         * Ignores the first element
         *
         * NB: while loop due to the exception
         */
        while (clusterIterator.hasNext()) {
            //unifyRedundantNodes()
            throw new RuntimeException("TODO: continue");
        }
        throw new RuntimeException("TODO: continue");
    }


}
