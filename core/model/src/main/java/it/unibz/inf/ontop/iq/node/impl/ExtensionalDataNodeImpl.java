package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private ImmutableSet<Variable> notInternallyRequiredVariables;

    // LAZY
    @Nullable
    private VariableNullability variableNullability;

    //LAZY
    @Nullable
    private ImmutableSet<ImmutableSet<Variable>> uniqueConstraints;

    //LAZY
    @Nullable
    private Boolean isDistinct;

    private final CoreUtilsFactory coreUtilsFactory;

    /**
     * See {@link IntermediateQueryFactory#createExtensionalDataNode(RelationDefinition, ImmutableMap)}
     */
    @SuppressWarnings("unused")
    @AssistedInject
    private ExtensionalDataNodeImpl(@Assisted RelationDefinition relationDefinition,
                                    @Assisted ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap,
                                    IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory,
                                    CoreUtilsFactory coreUtilsFactory) {
        this(relationDefinition, argumentMap, null, iqTreeTools, iqFactory, coreUtilsFactory);
    }

    /**
     * See {@link IntermediateQueryFactory#createExtensionalDataNode(RelationDefinition, ImmutableMap, VariableNullability)}
     */
    @AssistedInject
    private ExtensionalDataNodeImpl(@Assisted RelationDefinition relationDefinition,
                                    @Assisted ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap,
                                    @Assisted VariableNullability variableNullability,
                                    IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory,
                                    CoreUtilsFactory coreUtilsFactory) {
        super(iqTreeTools, iqFactory);
        this.coreUtilsFactory = coreUtilsFactory;
        this.relationDefinition = relationDefinition;
        this.argumentMap = argumentMap;
        this.variableNullability = variableNullability;
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
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExtensionalDataNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        ImmutableMap<Integer, ? extends VariableOrGroundTerm> newArguments = descendingSubstitution.applyToArgumentMap(argumentMap);
        return iqFactory.createExtensionalDataNode(relationDefinition, newArguments);
    }

    @Override
    public IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        return transformer.transformExtensionalData(this);
    }

    @Override
    public <T> IQTree acceptTransformer(IQTreeExtendedTransformer<T> transformer, T context) {
        return transformer.transformExtensionalData(this, context);
    }

    @Override
    public synchronized boolean isDistinct() {
        if (isDistinct == null)
        isDistinct = relationDefinition.getUniqueConstraints().stream()
                .map(UniqueConstraint::getDeterminants)
                .anyMatch(this::areDeterminantsPresentAndNotNull);
        return isDistinct;
    }

    private boolean areDeterminantsPresentAndNotNull(ImmutableSet<Attribute> determinants) {
        ImmutableList<Optional<? extends VariableOrGroundTerm>> arguments = determinants.stream()
                .map(a -> Optional.ofNullable(argumentMap.get(a.getIndex() - 1)))
                .collect(ImmutableCollectors.toList());

        VariableNullability variableNullability = getVariableNullability();

        return arguments.stream().allMatch(Optional::isPresent)
                && arguments.stream()
                .map(Optional::get)
                .filter(t -> t instanceof Variable)
                .map(v -> (Variable) v)
                .noneMatch(variableNullability::isPossiblyNullable);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor) {
        return visitor.visitExtensionalData(this);
    }

    /**
     * Optimized to re-use the variable nullability.
     * Useful the data node has a lot of columns.
     */
    @Override
    public IQTree applyFreshRenaming(InjectiveVar2VarSubstitution freshRenamingSubstitution) {
        ImmutableMap<Integer, ? extends VariableOrGroundTerm> newArgumentMap = freshRenamingSubstitution.applyToArgumentMap(argumentMap);
        return (variableNullability == null)
                ? iqFactory.createExtensionalDataNode(relationDefinition, newArgumentMap)
                : iqFactory.createExtensionalDataNode(relationDefinition, newArgumentMap,
                variableNullability.applyFreshRenaming(freshRenamingSubstitution));
    }

    @Override
    public IQTree applyFreshRenamingToAllVariables(InjectiveVar2VarSubstitution freshRenamingSubstitution) {
        return applyFreshRenaming(freshRenamingSubstitution);
    }

    @Override
    public synchronized VariableNullability getVariableNullability() {
        if (variableNullability == null) {

            ImmutableMultiset<? extends VariableOrGroundTerm> argMultiset = ImmutableMultiset.copyOf(argumentMap.values());

            // NB: DB column indexes start at 1.
            ImmutableSet<ImmutableSet<Variable>> nullableGroups = argumentMap.entrySet().stream()
                    .filter(e -> e.getValue() instanceof Variable)
                    .filter(e -> relationDefinition.getAttribute(e.getKey() + 1).isNullable())
                    .map(Map.Entry::getValue)
                    .map(a -> (Variable) a)
                    // An implicit filter condition makes them non-nullable
                    .filter(a -> argMultiset.count(a) < 2)
                    .map(ImmutableSet::of)
                    .collect(ImmutableCollectors.toSet());

            variableNullability = coreUtilsFactory.createVariableNullability(nullableGroups, getVariables());
        }

        return variableNullability;
    }

    @Override
    public void validate() throws InvalidIntermediateQueryException {
    }

    @Override
    public synchronized ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints() {
        if (uniqueConstraints == null) {

            uniqueConstraints = relationDefinition.getUniqueConstraints().stream()
                    .map(this::convertUniqueConstraint)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(ImmutableCollectors.toSet());
        }
        return uniqueConstraints;
    }

    private Optional<ImmutableSet<Variable>> convertUniqueConstraint(UniqueConstraint uniqueConstraint) {
        ImmutableList<Optional<? extends VariableOrGroundTerm>> arguments = uniqueConstraint.getDeterminants().stream()
                .map(a -> Optional.ofNullable(argumentMap.get(a.getIndex() - 1)))
                .collect(ImmutableCollectors.toList());

        if (!arguments.stream().allMatch(Optional::isPresent))
            return Optional.empty();

        return Optional.of(arguments.stream()
                .map(Optional::get)
                .filter(t -> t instanceof Variable)
                .map(v -> (Variable)v)
                .collect(ImmutableCollectors.toSet()));
    }

    /**
     * Only co-occuring variables are required.
     */
    @Override
    public synchronized ImmutableSet<Variable> getNotInternallyRequiredVariables() {
        if (notInternallyRequiredVariables == null) {
            ImmutableMultiset<Variable> multiset = argumentMap.values().stream()
                    .filter(t -> t instanceof Variable)
                    .map(t -> (Variable)t)
                    .collect(ImmutableCollectors.toMultiset());

            notInternallyRequiredVariables = multiset.entrySet().stream()
                    .filter(e -> e.getCount() == 1)
                    .map(Multiset.Entry::getElement)
                    .collect(ImmutableCollectors.toSet());
        }
        return notInternallyRequiredVariables;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtensionalDataNodeImpl that = (ExtensionalDataNodeImpl) o;
        return relationDefinition.equals(that.relationDefinition) && argumentMap.equals(that.argumentMap);
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
        return getLocalVariables();
    }

    @Override
    public synchronized ImmutableSet<Variable> getLocalVariables() {
        if (variables == null) {
            variables = argumentMap.values()
                    .stream()
                    .filter(Variable.class::isInstance)
                    .map(Variable.class::cast)
                    .collect(ImmutableCollectors.toSet());
        }
        return variables;
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return getLocalVariables();
    }

    @Override
    public ImmutableSet<Variable> getKnownVariables() {
        return getLocalVariables();
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return false;
    }
}
