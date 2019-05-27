package it.unibz.inf.ontop.iq.executor.join;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopOptimizationSettings;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.injection.OntopModelSettings.CardinalityPreservationMode.LOOSE;

/**
 * Focuses on functional dependencies that are not unique constraints.
 *
 * Uses them to:
 *   (1) unify some terms (a functional dependency is generating equalities)
 *   (2) detect and remove redundant self inner joins.
 *
 *
 * Does not remove any self-join if the CardinalityPreservationMode is not LOOSE (it does not guarantee its preservation).
 *
 */
@Singleton
public class LooseFDRedundantSelfJoinExecutor extends RedundantSelfJoinExecutor {

    private final OntopOptimizationSettings settings;
    private final SubstitutionFactory substitutionFactory;
    private final ImmutableUnificationTools unificationTools;

    @Inject
    private LooseFDRedundantSelfJoinExecutor(IntermediateQueryFactory iqFactory, OntopOptimizationSettings settings,
                                             SubstitutionFactory substitutionFactory,
                                             ImmutableUnificationTools unificationTools) {
        super(iqFactory,substitutionFactory, unificationTools);
        this.settings = settings;
        this.substitutionFactory = substitutionFactory;
        this.unificationTools = unificationTools;
    }

    @Override
    protected Optional<PredicateLevelProposal> proposePerPredicate(InnerJoinNode joinNode, ImmutableCollection<ExtensionalDataNode> initialNodes,
                                                                   RelationPredicate predicate,
                                                                   ImmutableList<Variable> priorityVariables,
                                                                   IntermediateQuery query)
            throws AtomUnificationException {

        if (initialNodes.size() < 2)
            return Optional.empty();

        RelationDefinition relation = predicate.getRelationDefinition();

        /*
         * Does nothing
         */
        if (relation == null)
            return Optional.empty();

        ImmutableMap<FunctionalDependency, ImmutableCollection<Collection<ExtensionalDataNode>>> constraintNodeMap =
                relation.getOtherFunctionalDependencies().stream()
                    .collect(ImmutableCollectors.toMap(
                        c -> c,
                        c -> groupDataNodesPerConstraint(c, initialNodes)));

        ImmutableSet<Variable> requiredAndCooccuringVariables = extractRequiredAndCooccuringVariables(query, joinNode);

        ImmutableSet<ExtensionalDataNode> nodesToRemove = selectNodesToRemove(requiredAndCooccuringVariables,
                constraintNodeMap, predicate);

        ImmutableList<ImmutableSubstitution<VariableOrGroundTerm>> dependentUnifiers = extractDependentUnifiers(
                relation, constraintNodeMap, nodesToRemove);

        return (dependentUnifiers.isEmpty() && nodesToRemove.isEmpty())
                ? Optional.empty()
                : Optional.of(new PredicateLevelProposal(dependentUnifiers, nodesToRemove));
    }

    /**
     * TODO: explain
     *
     * @throws AtomUnificationException
     */
    private ImmutableList<ImmutableSubstitution<VariableOrGroundTerm>> extractDependentUnifiers(
            RelationDefinition databaseRelation, ImmutableMap<FunctionalDependency,
            ImmutableCollection<Collection<ExtensionalDataNode>>> constraintNodeMap,
            ImmutableSet<ExtensionalDataNode> nodesToRemove) throws AtomUnificationException {

        ImmutableSet<Integer> nullableIndexes = databaseRelation.getAttributes().stream()
                .filter(Attribute::canNull)
                .map(a -> a.getIndex() - 1)
                .collect(ImmutableCollectors.toSet());

        ImmutableList.Builder<ImmutableSubstitution<VariableOrGroundTerm>> dependentUnifierBuilder = ImmutableList.builder();
        for (Map.Entry<FunctionalDependency, ImmutableCollection<Collection<ExtensionalDataNode>>> constraintEntry : constraintNodeMap.entrySet()) {
            dependentUnifierBuilder.addAll(extractDependentUnifiers(constraintEntry.getKey(), constraintEntry.getValue(),
                    nodesToRemove, nullableIndexes));
        }

        return dependentUnifierBuilder.build();
    }

    private ImmutableCollection<Collection<ExtensionalDataNode>> groupDataNodesPerConstraint(
            FunctionalDependency constraint, ImmutableCollection<ExtensionalDataNode> initialNodes) {

        ImmutableList<Integer> constraintDeterminantIndexes = constraint.getDeterminants().stream()
                .map(Attribute::getIndex)
                .collect(ImmutableCollectors.toList());

        ImmutableMultimap<ImmutableList<VariableOrGroundTerm>, ExtensionalDataNode> nodeMultiMap = initialNodes.stream()
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
            FunctionalDependency constraint, ImmutableCollection<Collection<ExtensionalDataNode>> dataNodeClusters,
            ImmutableSet<ExtensionalDataNode> nodesToRemove, ImmutableSet<Integer> nullableIndexes)
            throws AtomUnificationException {
        ImmutableList<Integer> dependentIndexes = constraint.getDependents().stream()
                .map(d -> d.getIndex() - 1)
                .collect(ImmutableCollectors.toList());

        ImmutableList.Builder<ImmutableSubstitution<VariableOrGroundTerm>> substitutionCollectionBuilder = ImmutableList.builder();
        for (Collection<ExtensionalDataNode> cluster : dataNodeClusters) {
            substitutionCollectionBuilder.addAll(extractDependentUnifiersFromCluster(dependentIndexes, cluster,
                    nodesToRemove, nullableIndexes));
        }
        return substitutionCollectionBuilder.build();
    }

    private Collection<ImmutableSubstitution<VariableOrGroundTerm>> extractDependentUnifiersFromCluster(
            ImmutableList<Integer> dependentIndexes, Collection<ExtensionalDataNode> cluster,
            ImmutableSet<ExtensionalDataNode> nodesToRemove, ImmutableSet<Integer> nullableIndexes)
            throws AtomUnificationException {
        if (cluster.size() < 2)
            return ImmutableList.of();

        ExtensionalDataNode referenceDataNode = cluster.stream()
                // Try to get the first kept data node
                .filter(n -> !nodesToRemove.contains(n))
                .findFirst()
                // Otherwise if all the nodes will be removed, take the first one
                .orElseGet(() -> cluster.iterator().next());

        /*
         * Ignores the reference data node
         *
         * NB: while loop due to the exception
         */
        Collection<ImmutableSubstitution<VariableOrGroundTerm>> substitutionCollection = new ArrayList<>();
        for (ExtensionalDataNode currentDataNode : cluster) {
            if (currentDataNode == referenceDataNode)
                continue;

            boolean willBeRemoved = nodesToRemove.contains(currentDataNode);

            unifyDependentTerms(referenceDataNode.getProjectionAtom(), currentDataNode.getProjectionAtom(),
                    dependentIndexes, willBeRemoved, nullableIndexes)
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
            DataAtom leftAtom, DataAtom rightAtom, ImmutableList<Integer> dependentIndexes,
            boolean willRightAtomBeRemoved, ImmutableSet<Integer> nullableIndexes)
            throws AtomUnificationException {

        // Non-final
        Optional<ImmutableSubstitution<VariableOrGroundTerm>> currentUnifier = Optional.empty();

        for (Integer dependentIndex : dependentIndexes) {
            VariableOrGroundTerm leftArgument = leftAtom.getTerm(dependentIndex);
            VariableOrGroundTerm rightArgument = rightAtom.getTerm(dependentIndex);

            /*
             * Throws an exception if the unification is not possible
             */
            ImmutableSubstitution<VariableOrGroundTerm> termUnifier = unificationTools.computeDirectedMGU(
                    rightArgument, leftArgument)
                    .map(ImmutableSubstitution::getImmutableMap)
                    .map(map -> map.entrySet().stream()
                            .collect(ImmutableCollectors.toMap(
                                    Map.Entry::getKey,
                                    e -> ImmutabilityTools.convertIntoVariableOrGroundTerm(e.getValue()))))
                    .map(substitutionFactory::getSubstitution)
                    .orElseThrow(AtomUnificationException::new);

            ImmutableSubstitution<VariableOrGroundTerm> candidateUnifier = currentUnifier.isPresent()
                    ? unificationTools.computeAtomMGUS(currentUnifier.get(), termUnifier)
                            .orElseThrow(AtomUnificationException::new)
                    : termUnifier;

            /*
             * Only includes this substitution if it is SAFE to do it.
             *
             * Safety is guaranteed if:
             *   - The right node will be removed (NB: the substitution is also required in that case)
             *   - The column is not nullable
             *   TODO: consider the case where nullable variables will be FILTERED OUT (improvement)
             *
             */
            currentUnifier = (willRightAtomBeRemoved || (!nullableIndexes.contains(dependentIndex)))
                    ? Optional.of(candidateUnifier)
                    : currentUnifier ;
        }
        return currentUnifier.filter(s -> !s.isEmpty());
    }

    /**
     * Does not look for redundant joins if not in the LOOSE preservation mode
     *
     * TODO: consider the case of predicates with multiple non-unique functional dependencies
     */
    private ImmutableSet<ExtensionalDataNode> selectNodesToRemove(

            ImmutableSet<Variable> requiredAndCooccuringVariables, ImmutableMap<FunctionalDependency,
            ImmutableCollection<Collection<ExtensionalDataNode>>> constraintNodeMap, AtomPredicate predicate) {

        if (settings.getCardinalityPreservationMode() != LOOSE) {
            return ImmutableSet.of();
        }

        /*
         * Expects that different unique constraints can only remove independent data nodes (-> no conflict)
         *
         * TODO: show why this works
         */
        return constraintNodeMap.entrySet().stream()
                .flatMap(e -> selectNodesToRemovePerConstraint(requiredAndCooccuringVariables, e.getKey(), e.getValue(),
                        predicate))
                .collect(ImmutableCollectors.toSet());
    }

    private ImmutableSet<Variable> extractRequiredAndCooccuringVariables(IntermediateQuery query, InnerJoinNode joinNode) {
        Stream<Variable> requiredVariablesByAncestorStream = Stream.concat(
                query.getVariablesRequiredByAncestors(joinNode).stream(),
                joinNode.getRequiredVariables(query).stream());

        /*
         * NB: looks fro into multiple occurrences of a variable within the same data node
         */
        Stream<Variable> innerCooccuringVariableStream = query.getChildren(joinNode).stream()
                .filter(c -> c instanceof ExtensionalDataNode)
                .map(c -> (ExtensionalDataNode) c)
                .flatMap(c ->
                        // Multiset
                        c.getProjectionAtom().getArguments().stream()
                        .filter(t -> t instanceof Variable)
                        .map(v -> (Variable) v)
                        .collect(ImmutableCollectors.toMultiset())
                        .entrySet().stream()
                                .filter(e -> e.getCount() > 1)
                                .map(Multiset.Entry::getElement));

        return Stream.concat(requiredVariablesByAncestorStream, innerCooccuringVariableStream)
                .collect(ImmutableCollectors.toSet());
    }

    private Stream<ExtensionalDataNode> selectNodesToRemovePerConstraint(ImmutableSet<Variable> requiredAndCooccuringVariables,
                                                              FunctionalDependency constraint,
                                                              ImmutableCollection<Collection<ExtensionalDataNode>> clusters,
                                                              AtomPredicate predicate) {
        ImmutableList<Integer> determinantIndexes = constraint.getDeterminants().stream()
                .map(Attribute::getIndex)
                .collect(ImmutableCollectors.toList());

        ImmutableList<Integer> dependentIndexes = constraint.getDependents().stream()
                .map(Attribute::getIndex)
                .collect(ImmutableCollectors.toList());


        ImmutableSet<Integer> independentIndexes = IntStream.range(1, predicate.getArity() + 1)
                .filter(i -> !dependentIndexes.contains(i))
                .filter(i -> !determinantIndexes.contains(i))
                .boxed()
                .collect(ImmutableCollectors.toSet());

        return clusters.stream()
                .flatMap(cluster -> selectNodesToRemovePerCluster(cluster, requiredAndCooccuringVariables,
                        independentIndexes));

    }

    private Stream<ExtensionalDataNode> selectNodesToRemovePerCluster(Collection<ExtensionalDataNode> cluster,
                                                           ImmutableSet<Variable> requiredAndCooccuringVariables,
                                                           ImmutableSet<Integer> independentIndexes) {
        int clusterSize = cluster.size();

        if (clusterSize < 2)
            return Stream.empty();

        ImmutableSet<ExtensionalDataNode> removableDataNodes = cluster.stream()
                .filter(n -> isRemovable(n, independentIndexes, requiredAndCooccuringVariables))
                .collect(ImmutableCollectors.toSet());

        /*
         * One node must be kept
         */
        return removableDataNodes.size() < clusterSize
                ? removableDataNodes.stream()
                : removableDataNodes.stream().skip(1);
    }

    /**
     * TODO: explain
     */
    private boolean isRemovable(ExtensionalDataNode node, ImmutableSet<Integer> independentIndexes,
                                ImmutableSet<Variable> requiredAndCooccuringVariables) {
        ImmutableList<? extends VariableOrGroundTerm> arguments = node.getProjectionAtom().getArguments();

        return independentIndexes.stream()
                .map(i -> arguments.get(i - 1))
                .allMatch(t -> (t instanceof Variable) && (!requiredAndCooccuringVariables.contains(t)));
    }


}
