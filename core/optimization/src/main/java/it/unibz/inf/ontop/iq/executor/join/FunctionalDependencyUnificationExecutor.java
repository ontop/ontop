package it.unibz.inf.ontop.iq.executor.join;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.impl.GroundTermTools;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;

/**
 * Focuses on functional dependencies that are not unique constraints.
 *
 * Uses them to unify some terms (a functional dependency is generating equalities)
 *
 * Self-join elimination is done LATER by the SelfJoinSameTermIQOptimizer, by taking advantage of the unified variables.
 *
 * NB: Was having before some logic for removing some redundant join, but was not safe to apply when cardinality matters.
 * The code has only been superficially simplified so some logic that is not needed anymore may still remain.
 *
 */
@Singleton
public class FunctionalDependencyUnificationExecutor extends RedundantSelfJoinExecutor {

    private final SubstitutionFactory substitutionFactory;
    private final ImmutableUnificationTools unificationTools;

    @Inject
    private FunctionalDependencyUnificationExecutor(IntermediateQueryFactory iqFactory,
                                                    SubstitutionFactory substitutionFactory,
                                                    ImmutableUnificationTools unificationTools,
                                                    TermFactory termFactory) {
        super(iqFactory,substitutionFactory, unificationTools, termFactory);
        this.substitutionFactory = substitutionFactory;
        this.unificationTools = unificationTools;
    }

    @Override
    protected Optional<PredicateLevelProposal> proposePerPredicate(InnerJoinNode joinNode, ImmutableCollection<ExtensionalDataNode> initialNodes,
                                                                   RelationDefinition relation,
                                                                   ImmutableList<Variable> priorityVariables,
                                                                   IntermediateQuery query)
            throws AtomUnificationException {

        if (initialNodes.size() < 2)
            return Optional.empty();


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

        ImmutableList<ImmutableSubstitution<VariableOrGroundTerm>> dependentUnifiers = extractDependentUnifiers(
                relation, constraintNodeMap);

        // Here no data node will be removed, so this expression can be ignored
        Optional<ImmutableExpression> isNotNullConjunction = Optional.empty();

        return (dependentUnifiers.isEmpty())
                ? Optional.empty()
                : Optional.of(new PredicateLevelProposal(dependentUnifiers, ImmutableSet.of(), isNotNullConjunction));
    }

    /**
     * TODO: explain
     *
     * @throws AtomUnificationException
     */
    private ImmutableList<ImmutableSubstitution<VariableOrGroundTerm>> extractDependentUnifiers(
            RelationDefinition databaseRelation, ImmutableMap<FunctionalDependency,
            ImmutableCollection<Collection<ExtensionalDataNode>>> constraintNodeMap) throws AtomUnificationException {

        ImmutableSet<Integer> nullableIndexes = databaseRelation.getAttributes().stream()
                .filter(Attribute::canNull)
                .map(a -> a.getIndex() - 1)
                .collect(ImmutableCollectors.toSet());

        ImmutableList.Builder<ImmutableSubstitution<VariableOrGroundTerm>> dependentUnifierBuilder = ImmutableList.builder();
        for (Map.Entry<FunctionalDependency, ImmutableCollection<Collection<ExtensionalDataNode>>> constraintEntry : constraintNodeMap.entrySet()) {
            dependentUnifierBuilder.addAll(extractDependentUnifiers(constraintEntry.getKey(), constraintEntry.getValue(),
                    nullableIndexes));
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
                        n -> extractDeterminantArguments(n, constraintDeterminantIndexes),
                        n -> n));

        return nodeMultiMap.asMap().values();
    }

    private ImmutableList<VariableOrGroundTerm> extractDeterminantArguments(ExtensionalDataNode dataNode,
                                                                            ImmutableList<Integer> determinantIndexes) {
        ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap = dataNode.getArgumentMap();
        return determinantIndexes.stream()
                .map(i -> argumentMap.get(i - 1))
                .collect(ImmutableCollectors.toList());
    }

    private ImmutableCollection<ImmutableSubstitution<VariableOrGroundTerm>> extractDependentUnifiers(
            FunctionalDependency constraint, ImmutableCollection<Collection<ExtensionalDataNode>> dataNodeClusters,
            ImmutableSet<Integer> nullableIndexes)
            throws AtomUnificationException {
        ImmutableList<Integer> dependentIndexes = constraint.getDependents().stream()
                .map(d -> d.getIndex() - 1)
                .collect(ImmutableCollectors.toList());

        ImmutableList.Builder<ImmutableSubstitution<VariableOrGroundTerm>> substitutionCollectionBuilder = ImmutableList.builder();
        for (Collection<ExtensionalDataNode> cluster : dataNodeClusters) {
            substitutionCollectionBuilder.addAll(extractDependentUnifiersFromCluster(dependentIndexes, cluster,
                    nullableIndexes));
        }
        return substitutionCollectionBuilder.build();
    }

    private Collection<ImmutableSubstitution<VariableOrGroundTerm>> extractDependentUnifiersFromCluster(
            ImmutableList<Integer> dependentIndexes, Collection<ExtensionalDataNode> cluster,
            ImmutableSet<Integer> nullableIndexes)
            throws AtomUnificationException {
        if (cluster.size() < 2)
            return ImmutableList.of();

        ExtensionalDataNode referenceDataNode = cluster.stream()
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

            unifyDependentTerms(referenceDataNode, currentDataNode,
                    dependentIndexes, nullableIndexes)
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
            ExtensionalDataNode leftNode, ExtensionalDataNode rightNode, ImmutableList<Integer> dependentIndexes,
            ImmutableSet<Integer> nullableIndexes)
            throws AtomUnificationException {

        ImmutableMap<Integer, ? extends VariableOrGroundTerm> leftArgumentMap = leftNode.getArgumentMap();
        ImmutableMap<Integer, ? extends VariableOrGroundTerm> rightArgumentMap = rightNode.getArgumentMap();

        // Non-final
        Optional<ImmutableSubstitution<VariableOrGroundTerm>> currentUnifier = Optional.empty();

        for (Integer dependentIndex : dependentIndexes) {
            VariableOrGroundTerm leftArgument = leftArgumentMap.get(dependentIndex);
            VariableOrGroundTerm rightArgument = rightArgumentMap.get(dependentIndex);

            /*
             * Throws an exception if the unification is not possible
             */
            ImmutableSubstitution<VariableOrGroundTerm> termUnifier = unificationTools.computeDirectedMGU(
                    rightArgument, leftArgument)
                    .map(ImmutableSubstitution::getImmutableMap)
                    .map(map -> map.entrySet().stream()
                            .collect(ImmutableCollectors.toMap(
                                    Map.Entry::getKey,
                                    e -> GroundTermTools.convertIntoVariableOrGroundTerm(e.getValue()))))
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
            currentUnifier = (!nullableIndexes.contains(dependentIndex))
                    ? Optional.of(candidateUnifier)
                    : currentUnifier ;
        }
        return currentUnifier.filter(s -> !s.isEmpty());
    }
}
