package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.impl.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * "Default" implementation for an extensional data node.
 *
 * Most likely (but not necessarily) will be overwritten by native query language specific implementations.
 */
public class ExtensionalDataNodeImpl extends LeafIQTreeImpl implements ExtensionalDataNode {

    private static final String EXTENSIONAL_NODE_STR = "EXTENSIONAL";

    private final RelationDefinition relationDefinition;
    private final ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap;

    // LAZY
    @Nullable
    private ImmutableSet<Variable> variables;

    // LAZY
    @Nullable
    private VariableNonRequirement variableNonRequirement;

    // LAZY
    @Nullable
    private VariableNullability variableNullability;

    //LAZY
    @Nullable
    private ImmutableSet<ImmutableSet<Variable>> uniqueConstraints;

    //LAZY
    @Nullable
    private Boolean isDistinct;

    private final QueryRenamer queryRenamer;

    /**
     * See {@link IntermediateQueryFactory#createExtensionalDataNode(RelationDefinition, ImmutableMap)}
     */
    @SuppressWarnings("unused")
    @AssistedInject
    private ExtensionalDataNodeImpl(@Assisted RelationDefinition relationDefinition,
                                    @Assisted ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap,
                                    IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory,
                                    CoreUtilsFactory coreUtilsFactory, SubstitutionFactory substitutionFactory,
                                    QueryRenamer queryRenamer) {
        this(relationDefinition, argumentMap, null, iqTreeTools, iqFactory, coreUtilsFactory, substitutionFactory, queryRenamer);
    }

    /**
     * See {@link IntermediateQueryFactory#createExtensionalDataNode(RelationDefinition, ImmutableMap, VariableNullability)}
     */
    @AssistedInject
    private ExtensionalDataNodeImpl(@Assisted RelationDefinition relationDefinition,
                                    @Assisted ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap,
                                    @Assisted @Nullable VariableNullability variableNullability,
                                    IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory,
                                    CoreUtilsFactory coreUtilsFactory, SubstitutionFactory substitutionFactory,
                                    QueryRenamer queryRenamer) {
        super(iqTreeTools, iqFactory, substitutionFactory, coreUtilsFactory);
        this.relationDefinition = relationDefinition;
        this.argumentMap = argumentMap;
        this.variableNullability = variableNullability;
        this.queryRenamer = queryRenamer;
    }


    @Override
    public RelationDefinition getRelationDefinition() {
        return relationDefinition;
    }

    @Override
    public ImmutableMap<Integer, ? extends VariableOrGroundTerm> getArgumentMap() {
        return argumentMap;
    }


    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution, VariableGenerator variableGenerator) {
        ImmutableMap<Integer, VariableOrGroundTerm> newArguments = substitutionFactory.onVariableOrGroundTerms().applyToTerms(descendingSubstitution, argumentMap);
        return iqFactory.createExtensionalDataNode(relationDefinition, newArguments);
    }

    @Override
    public synchronized boolean isDistinct() {
        return getCachedValue(() -> isDistinct, this::computeIsDistinct, v -> isDistinct = v);
    }

    private boolean computeIsDistinct() {
        return relationDefinition.getUniqueConstraints().stream()
                .map(UniqueConstraint::getDeterminants)
                .anyMatch(this::areDeterminantsPresentAndNotNull);
    }

    private boolean areDeterminantsPresentAndNotNull(ImmutableSet<Attribute> determinants) {
        VariableNullability variableNullability = getVariableNullability();

        var arguments = getArguments(determinants);
        return arguments.stream().allMatch(Optional::isPresent)
                && getVariableSetFrom(arguments).stream().noneMatch(variableNullability::isPossiblyNullable);
    }

    /**
     * Optimized to re-use the variable nullability.
     * Useful the data node has a lot of columns.
     */
    @Override
    public ExtensionalDataNode applyFreshRenaming(InjectiveSubstitution<Variable> freshRenamingSubstitution) {
        ImmutableMap<Integer, VariableOrGroundTerm> newArgumentMap = substitutionFactory.onVariableOrGroundTerms().applyToTerms(freshRenamingSubstitution, argumentMap);
        return (variableNullability == null)
                ? iqFactory.createExtensionalDataNode(relationDefinition, newArgumentMap)
                : iqFactory.createExtensionalDataNode(relationDefinition, newArgumentMap,
                        variableNullability.applyFreshRenaming(freshRenamingSubstitution));
    }

    @Override
    public synchronized VariableNullability getVariableNullability() {
        return getCachedValue(() -> variableNullability, this::computeVariableNullability, v -> variableNullability = v);
    }

    private VariableNullability computeVariableNullability() {
        ImmutableSet<Variable> singleOccurrenceVariables = NaryIQTreeTools.singleOccurrenceVariables(
                getVariableStreamFrom(argumentMap.values().stream()));

        ImmutableSet<ImmutableSet<Variable>> nullableGroups = getVariableStreamFrom(
                argumentMap.entrySet().stream()
                        // NB: DB column indexes start at 1.
                        .filter(e -> relationDefinition.getAttribute(e.getKey() + 1).isNullable())
                        .map(Map.Entry::getValue))
                // An implicit filter condition makes a variable non-nullable
                .filter(singleOccurrenceVariables::contains)
                .map(ImmutableSet::of)
                .collect(ImmutableCollectors.toSet());

        return coreUtilsFactory.createVariableNullability(nullableGroups, getVariables());
    }

    @Override
    public void validate() throws InvalidIntermediateQueryException {
    }

    @Override
    public synchronized ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints() {
        return getCachedValue(() -> uniqueConstraints, this::computeUniqueConstraints, v -> uniqueConstraints = v);
    }

    private ImmutableSet<ImmutableSet<Variable>> computeUniqueConstraints() {
        return relationDefinition.getUniqueConstraints().stream()
                .map(this::convertUniqueConstraint)
                .flatMap(Optional::stream)
                .collect(ImmutableCollectors.toSet());
    }

    private Optional<ImmutableSet<Variable>> convertUniqueConstraint(UniqueConstraint uniqueConstraint) {
        var arguments = getArguments(uniqueConstraint.getDeterminants());

        if (!arguments.stream().allMatch(Optional::isPresent))
            return Optional.empty();

        return Optional.of(getVariableSetFrom(arguments));
    }

    @Override
    public synchronized FunctionalDependencies inferFunctionalDependencies() {
        return relationDefinition.getOtherFunctionalDependencies().stream()
                .map(this::convertFunctionalDependency)
                .flatMap(Optional::stream)
                .collect(FunctionalDependencies.toFunctionalDependencies())
                .concat(FunctionalDependencies.fromUniqueConstraints(inferUniqueConstraints(), getLocalVariables()));
    }

    private Optional<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>> convertFunctionalDependency(FunctionalDependency functionalDependency) {
        var determinants = getArguments(functionalDependency.getDeterminants());
        var dependents = getArguments(functionalDependency.getDependents());

        if (!determinants.stream().allMatch(Optional::isPresent) || dependents.stream().noneMatch(Optional::isPresent))
            return Optional.empty();

        return Optional.of(Maps.immutableEntry(getVariableSetFrom(determinants), getVariableSetFrom(dependents)));
    }


    private ImmutableList<Optional<? extends VariableOrGroundTerm>> getArguments(ImmutableSet<Attribute> attributes) {
        return attributes.stream()
                .map(a -> Optional.ofNullable(argumentMap.get(a.getIndex() - 1)))
                .collect(ImmutableCollectors.toList());
    }

    private static ImmutableSet<Variable> getVariableSetFrom(ImmutableList<Optional<? extends VariableOrGroundTerm>> list) {
        return getVariableStreamFrom(list.stream().flatMap(Optional::stream))
                .collect(ImmutableCollectors.toSet());
    }

    private static Stream<Variable> getVariableStreamFrom(Stream<? extends VariableOrGroundTerm> stream) {
        return stream
                .filter(t -> t instanceof Variable)
                .map(v -> (Variable)v);
    }


    /**
     * Only co-occuring variables are required.
     */
    @Override
    public synchronized VariableNonRequirement getVariableNonRequirement() {
        return getCachedValue(() -> variableNonRequirement, this::computeVariableNonRequirement, v -> variableNonRequirement = v);
    }

    private VariableNonRequirement computeVariableNonRequirement() {
        return VariableNonRequirement.of(
                NaryIQTreeTools.singleOccurrenceVariables(
                        getVariableStreamFrom(argumentMap.values().stream())));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof ExtensionalDataNodeImpl) {
            ExtensionalDataNodeImpl that = (ExtensionalDataNodeImpl) o;
            return relationDefinition.equals(that.relationDefinition) && argumentMap.equals(that.argumentMap);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(relationDefinition, argumentMap);
    }

    @Override
    public String toString() {
        return String.format("%s %s(%s)",
                EXTENSIONAL_NODE_STR,
                relationDefinition.getAtomPredicate().getName(),
                argumentMap.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(",")));
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return getCachedValue(() -> variables, this::computeVariables, v -> variables = v);
    }

    private ImmutableSet<Variable> computeVariables() {
        return getVariableStreamFrom(argumentMap.values().stream())
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return false;
    }

    @Override
    public ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions() {
        if (relationDefinition instanceof Lens) {
            IQ iq = ((Lens) relationDefinition).getIQ();

            IQTree renamedTree = merge(this, iq, coreUtilsFactory.createVariableGenerator(this.getKnownVariables()),
                    substitutionFactory, queryRenamer, iqFactory);

            return renamedTree.getPossibleVariableDefinitions();
        }
        return ImmutableSet.of(substitutionFactory.getSubstitution());
    }

    public static IQTree merge(ExtensionalDataNode dataNode, IQ definition, VariableGenerator variableGenerator,
                               SubstitutionFactory substitutionFactory, QueryRenamer queryRenamer,
                               IntermediateQueryFactory iqFactory) {
        InjectiveSubstitution<Variable> renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(
                variableGenerator, definition.getTree().getKnownVariables());

        IQ renamedDefinition = queryRenamer.applyInDepthRenaming(renamingSubstitution, definition);

        ImmutableList<Variable> sourceAtomArguments = substitutionFactory.apply(
                renamingSubstitution,
                renamedDefinition.getProjectionAtom().getArguments());

        Substitution<VariableOrGroundTerm> descendingSubstitution = dataNode.getArgumentMap().entrySet().stream()
                .collect(substitutionFactory.toSubstitutionSkippingIdentityEntries(
                        e -> sourceAtomArguments.get(e.getKey()),
                        Map.Entry::getValue));

        try {
            DownPropagation dp = DownPropagation.of(descendingSubstitution, Optional.empty(), renamedDefinition.getTree().getVariables(), variableGenerator, null);
            return iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(dataNode.getVariables()),
                            dp.propagate(renamedDefinition.getTree()))
                    .normalizeForOptimization(variableGenerator);
        }
        catch (DownPropagation.InconsistentDownPropagationException e) {
            throw new MinorOntopInternalBugException("ExtensionalDataNode cannot contain NULLs", e);
        }
    }
}
