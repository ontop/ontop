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
import it.unibz.inf.ontop.model.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.pivotalrepr.DataNode;
import it.unibz.inf.ontop.sql.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.injection.OntopModelSettings.CardinalityPreservationMode.LOOSE;
import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

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

        ImmutableList.Builder<ImmutableSubstitution<VariableOrGroundTerm>> dependentUnifierBuilder = ImmutableList.builder();
        for (Map.Entry<NonUniqueFunctionalConstraint, ImmutableCollection<Collection<DataNode>>> constraintEntry : constraintNodeMap.entrySet()) {
            dependentUnifierBuilder.addAll(extractDependentUnifiers(constraintEntry.getKey(), constraintEntry.getValue()));
        }
        ImmutableList<ImmutableSubstitution<VariableOrGroundTerm>> dependentUnifiers = dependentUnifierBuilder.build();

        /*
         * Does not look for redundant joins if not in the LOOSE preservation mode
         */
        if (settings.getCardinalityPreservationMode() != LOOSE)
            return Optional.of(new PredicateLevelProposal(dependentUnifiers, ImmutableList.of()));

        /*
         * TODO: continue (remove this)
         */
        return Optional.of(new PredicateLevelProposal(dependentUnifiers, ImmutableList.of()));
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

    private ImmutableCollection<ImmutableSubstitution<VariableOrGroundTerm>> extractDependentUnifiers(
            NonUniqueFunctionalConstraint constraint, ImmutableCollection<Collection<DataNode>> dataNodeClusters)
            throws AtomUnificationException {
        ImmutableList<Integer> dependentIndexes = constraint.getDependents().stream()
                .map(d -> d.getIndex() - 1)
                .collect(ImmutableCollectors.toList());

        ImmutableList.Builder<ImmutableSubstitution<VariableOrGroundTerm>> substitutionCollectionBuilder = ImmutableList.builder();
        for (Collection<DataNode> cluster : dataNodeClusters) {
            substitutionCollectionBuilder.addAll(extractDependentUnifiersFromCluster(dependentIndexes, cluster));
        }
        return substitutionCollectionBuilder.build();
    }

    private Collection<ImmutableSubstitution<VariableOrGroundTerm>> extractDependentUnifiersFromCluster(
            ImmutableList<Integer> dependentIndexes, Collection<DataNode> cluster) throws AtomUnificationException {
        if (cluster.size() < 2)
            return ImmutableList.of();

        Iterator<DataNode> clusterIterator = cluster.iterator();
        DataNode firstDataNode = clusterIterator.next();

        /*
         * Ignores the first element
         *
         * NB: while loop due to the exception
         */
        Collection<ImmutableSubstitution<VariableOrGroundTerm>> substitutionCollection = new ArrayList<>();
        while (clusterIterator.hasNext()) {
            DataNode currentDataNode = clusterIterator.next();
            unifyDependentTerms(firstDataNode.getProjectionAtom(), currentDataNode.getProjectionAtom(), dependentIndexes)
                    .ifPresent(substitutionCollection::add);
        }

        return substitutionCollection;
    }

    /**
     *
     * Gives a preference to the variables of the left atom
     *
     * Throws an AtomUnificationException if unification is impossible
     */
    private Optional<ImmutableSubstitution<VariableOrGroundTerm>> unifyDependentTerms(
            DataAtom leftAtom, DataAtom rightAtom, ImmutableList<Integer> dependentIndexes)
            throws AtomUnificationException {

        // Non-final
        Optional<ImmutableSubstitution<VariableOrGroundTerm>> currentUnifier = Optional.empty();

        for (Integer dependentIndex : dependentIndexes) {
            VariableOrGroundTerm leftArgument = leftAtom.getTerm(dependentIndex);
            VariableOrGroundTerm rightArgument = rightAtom.getTerm(dependentIndex);

            /*
             * Throws an exception if the unification is not possible
             */
            ImmutableSubstitution<VariableOrGroundTerm> termUnifier = ImmutableUnificationTools.computeDirectedMGU(
                    rightArgument, leftArgument)
                    .map(ImmutableSubstitution::getImmutableMap)
                    .map(map -> map.entrySet().stream()
                            .collect(ImmutableCollectors.toMap(
                                    Map.Entry::getKey,
                                    e -> ImmutabilityTools.convertIntoVariableOrGroundTerm(e.getValue()))))
                    .map(DATA_FACTORY::getSubstitution)
                    .orElseThrow(AtomUnificationException::new);

            ImmutableSubstitution<VariableOrGroundTerm> newUnifier = currentUnifier.isPresent()
                    ? ImmutableUnificationTools.computeAtomMGUS(currentUnifier.get(), termUnifier)
                            .orElseThrow(AtomUnificationException::new)
                    : termUnifier;

            currentUnifier = Optional.of(newUnifier);

        }
        return currentUnifier.filter(s -> !s.isEmpty());
    }


}
