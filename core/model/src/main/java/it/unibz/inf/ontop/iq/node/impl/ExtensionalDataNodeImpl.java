package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private VariableNullability variableNullability;
    //LAZY
    @Nullable
    private ImmutableSet<ImmutableSet<Variable>> uniqueConstraints;

    private final CoreUtilsFactory coreUtilsFactory;

    @AssistedInject
    private ExtensionalDataNodeImpl(@Assisted DataAtom<RelationPredicate> atom,
                                    IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory,
                                    CoreUtilsFactory coreUtilsFactory) {
        super(iqTreeTools, iqFactory);
        this.coreUtilsFactory = coreUtilsFactory;
        this.relationDefinition = atom.getPredicate().getRelationDefinition();
        this.argumentMap = extractArgumentMap(atom);
    }

    @AssistedInject
    private ExtensionalDataNodeImpl(@Assisted RelationDefinition relationDefinition,
                                    @Assisted ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap,
                                    IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory,
                                    CoreUtilsFactory coreUtilsFactory) {
        super(iqTreeTools, iqFactory);
        this.coreUtilsFactory = coreUtilsFactory;
        this.relationDefinition = relationDefinition;
        this.argumentMap = argumentMap;
    }

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

    /**
     * TEMPORARY
     */
    private ImmutableMap<Integer, ? extends VariableOrGroundTerm> extractArgumentMap(DataAtom<RelationPredicate> atom) {
        return IntStream.range(0, atom.getArity())
                .boxed()
                .collect(ImmutableCollectors.toMap(
                        i -> i,
                        atom::getTerm));
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
    public ExtensionalDataNode clone() {
        return iqFactory.createExtensionalDataNode(relationDefinition, argumentMap);
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
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        if (!getVariables().contains(variable))
            throw new IllegalArgumentException("The variable " + variable + " is not projected by " + this);

        // NB: DB column indexes start at 1.
        return argumentMap.entrySet().stream()
                .filter(e -> e.getValue().equals(variable))
                .map(e -> e.getKey() + 1)
                .map(relationDefinition::getAttribute)
                .allMatch(Attribute::canNull);
    }

    @Override
    public IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        return transformer.transformExtensionalData(this);
    }

    /**
     * Is distinct if it has at least one unique constraint
     */
    @Override
    public boolean isDistinct() {
        return !relationDefinition.getUniqueConstraints().isEmpty();
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
    public VariableNullability getVariableNullability() {
        if (variableNullability == null) {

            ImmutableMultiset<? extends VariableOrGroundTerm> argMultiset = ImmutableMultiset.copyOf(argumentMap.values());

            // NB: DB column indexes start at 1.
            ImmutableSet<ImmutableSet<Variable>> nullableGroups = argumentMap.entrySet().stream()
                    .filter(e -> e.getValue() instanceof Variable)
                    .filter(e -> relationDefinition.getAttribute(e.getKey() + 1).canNull())
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
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints() {
        if (uniqueConstraints == null) {

            uniqueConstraints = relationDefinition.getUniqueConstraints().stream()
                    .map(uc -> uc.getAttributes().stream()
                            .map(a ->  argumentMap.get(a.getIndex() -1))
                            .filter(t -> t instanceof Variable)
                            .map(v -> (Variable)v)
                            .collect(ImmutableCollectors.toSet()))
                    .collect(ImmutableCollectors.toSet());
        }
        return uniqueConstraints;
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof ExtensionalDataNode)
                && ((ExtensionalDataNode) node).getRelationDefinition().equals(relationDefinition)
                && ((ExtensionalDataNode) node).getArgumentMap().equals(argumentMap);
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return isSyntacticallyEquivalentTo(queryNode);
    }

    @Override
    public String toString() {
        return String.format("%s %s(%s)",
                EXTENSIONAL_NODE_STR,
                relationDefinition.getID().toString(),
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
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return getLocallyRequiredVariables();
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
