package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.Var2VarSubstitution;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ValuesNodeImpl extends LeafIQTreeImpl implements ValuesNode {


    private static final String VALUES_NODE_STR = "VALUES";
    // The variables as used in this node, we need to keep order.
    private final ImmutableList<Variable> orderedVariables;
    // The variables consistent with all interfaces, as unordered set.
    private final ImmutableSet<Variable> projectedVariables;
    private final ImmutableList<ImmutableList<Constant>> values;
    private final CoreUtilsFactory coreUtilsFactory;
    private final SubstitutionFactory substitutionFactory;
    private boolean isNormalized = false;
    // LAZY
    private VariableNullability variableNullability;
    // LAZY
    private Boolean isDistinct;


    @AssistedInject
    protected ValuesNodeImpl(@Assisted("orderedVariables") ImmutableList<Variable> orderedVariables,
                             @Assisted("values") ImmutableList<ImmutableList<Constant>> values,
                             IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory, CoreUtilsFactory coreUtilsFactory,
                             OntopModelSettings settings, SubstitutionFactory substitutionFactory) {
        super(iqTreeTools, iqFactory);
        this.orderedVariables = orderedVariables;
        this.projectedVariables = ImmutableSet.copyOf(orderedVariables);
        this.values = values;
        this.coreUtilsFactory = coreUtilsFactory;
        this.substitutionFactory = substitutionFactory;

        if (settings.isTestModeEnabled())
            validate();
    }

    @Override
    public ImmutableList<ImmutableList<Constant>> getValues() {
        return values;
    }

    @Override
    public IQTree normalizeForOptimization(VariableGenerator variableGenerator) {
        if (isNormalized)
            return this;
        Optional<ConstructionAndValues> lift = liftSingleValueVariables();
        if (lift.isPresent()) {
            LeafIQTree normalizedLeaf = furtherNormalize(lift.get().valuesNode);
            return iqFactory.createUnaryIQTree(lift.get().constructionNode, normalizedLeaf,
                    iqFactory.createIQProperties().declareNormalizedForOptimization());
        }
        return furtherNormalize(this);
    }

    private Optional<ConstructionAndValues> liftSingleValueVariables() {
        ImmutableSet<Integer> singleValueVariableIndices = IntStream.range(0, orderedVariables.size())
                .filter(i -> 1 == getValueStream(orderedVariables.get(i))
                        .unordered()
                        .distinct()
                        .count())
                .boxed()
                .collect(ImmutableCollectors.toSet());

        if (!singleValueVariableIndices.isEmpty()) {
            // Can be normalized into a construction/child node pair. Start by creating ConstructionNode.
            ImmutableSubstitution<ImmutableTerm> substitutions = substitutionFactory.getSubstitution(
                    singleValueVariableIndices.stream()
                            .collect(ImmutableCollectors.toMap(
                                    orderedVariables::get,
                                    i -> values.get(0).get(i))));
            ConstructionNode constructionNode = iqFactory.createConstructionNode(projectedVariables, substitutions);

            // Create the ValueNode
            ImmutableSet<Integer> multiValueVariableIndices = IntStream.range(0, orderedVariables.size())
                    .filter(i -> !singleValueVariableIndices.contains(i))
                    .boxed()
                    .collect(ImmutableCollectors.toSet());
            ImmutableList<Variable> newValuesNodeVariables = multiValueVariableIndices.stream()
                    .map(orderedVariables::get)
                    .collect(ImmutableCollectors.toList());
            ImmutableList<ImmutableList<Constant>> newValuesNodeValues = values.stream()
                    .map(constants -> multiValueVariableIndices.stream()
                            .map(constants::get)
                            .collect(ImmutableCollectors.toList()))
                    .collect(ImmutableCollectors.toList());
            ValuesNode valuesNode = iqFactory.createValuesNode(newValuesNodeVariables, newValuesNodeValues);

            return Optional.of(new ConstructionAndValues(constructionNode, valuesNode));
        }
        return Optional.empty();
    }

    private LeafIQTree furtherNormalize(ValuesNode valuesNode) {
        if (valuesNode.getValues().isEmpty()) {
            return iqFactory.createEmptyNode(valuesNode.getVariables());
        }
        if ((valuesNode.getVariables().isEmpty()) && (valuesNode.getValues().size() == 1)) {
            return iqFactory.createTrueNode();
        }
        if (valuesNode == this) {
            isNormalized = true;
        }
        return valuesNode;
    }


    public Stream<Constant> getValueStream(Variable variable) {
        int index = orderedVariables.indexOf(variable);
        if (index < 0)
            return Stream.empty();

        return values.stream()
                .map(t -> t.get(index));
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ValuesNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return projectedVariables;
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        return getVariableNullability().isPossiblyNullable(variable);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof ValuesNode)
                && ((ValuesNode) node).getVariables().equals(projectedVariables)
                && ((ValuesNode) node).getValues().equals(values);
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return projectedVariables;
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return (queryNode instanceof ValuesNode) && queryNode.isSyntacticallyEquivalentTo(this);
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    @Override
    public IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        return transformer.transformValues(this);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor) {
        return visitor.visitValues(this);
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public ValuesNode applyFreshRenaming(InjectiveVar2VarSubstitution freshRenamingSubstitution) {
        ImmutableList<Variable> newVariables = orderedVariables.stream()
                .map(freshRenamingSubstitution::applyToVariable)
                .collect(ImmutableCollectors.toList());

        return newVariables.equals(orderedVariables)
                ? this
                : iqFactory.createValuesNode(newVariables, values);
    }

    @Override
    public IQTree applyFreshRenamingToAllVariables(InjectiveVar2VarSubstitution freshRenamingSubstitution) {
        return applyFreshRenaming(freshRenamingSubstitution);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        if (descendingSubstitution.isEmpty())
            return this;
        ConstructionAndFilterAndValues constructionAndFilterAndValues =
                new ConstructionAndFilterAndValues(null, null, this);
        // Split up the substitution in three groups
        ImmutableSubstitution<GroundFunctionalTerm> functionalSubstitutionFragment = descendingSubstitution.getGroundFunctionalTermFragment();
        ImmutableSubstitution<Constant> constantSubstitutionFragment = descendingSubstitution.getConstantFragment();
        Var2VarSubstitution variableSubstitutionFragment = descendingSubstitution.getVar2VarFragment();

        if (!functionalSubstitutionFragment.isEmpty()) {
            constructionAndFilterAndValues = addProjectedVariablesToConstruction(descendingSubstitution, constructionAndFilterAndValues);
            constructionAndFilterAndValues = substituteGroundFunctionalTerms(functionalSubstitutionFragment, constructionAndFilterAndValues);
        }
        if (!constantSubstitutionFragment.isEmpty()) {
            constructionAndFilterAndValues = substituteConstants(constantSubstitutionFragment, constructionAndFilterAndValues);
        }
        if (!variableSubstitutionFragment.isEmpty()) {
            constructionAndFilterAndValues = substituteVariables(variableSubstitutionFragment, constructionAndFilterAndValues);
        }
        return buildTreeFromCFV(constructionAndFilterAndValues);
    }

    private ConstructionAndFilterAndValues substituteGroundFunctionalTerms(ImmutableSubstitution<? extends GroundFunctionalTerm> substitution,
                                                                           ConstructionAndFilterAndValues constructionAndFilterAndValues) {
        ValuesNode valuesNode = constructionAndFilterAndValues.valuesNode;
        InjectiveVar2VarSubstitution renaming = computeVariableRenaming(valuesNode.getVariables(),
                coreUtilsFactory.createVariableGenerator(valuesNode.getKnownVariables()))
                .reduceDomainToIntersectionWith(substitution.getDomain());

        return renaming.applyRenaming(substitution).convertIntoBooleanExpression().map(filterCondition ->
                new ConstructionAndFilterAndValues(
                constructionAndFilterAndValues.constructionNode,
                iqFactory.createFilterNode(filterCondition),
                (ValuesNode) valuesNode.applyFreshRenaming(renaming)))
                .orElseGet(() -> new ConstructionAndFilterAndValues(null, null, valuesNode));
    }

    private ConstructionAndFilterAndValues addProjectedVariablesToConstruction(ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
                                                ConstructionAndFilterAndValues constructionAndFilterAndValues) {
        return new ConstructionAndFilterAndValues(
                iqFactory.createConstructionNode(constructionAndFilterAndValues.valuesNode.getVariables().stream()
                        .filter(variable -> !substitution.getDomain().contains(variable))
                        .collect(ImmutableCollectors.toSet())),
                constructionAndFilterAndValues.filterNode,
                constructionAndFilterAndValues.valuesNode);
    }

    private ConstructionAndFilterAndValues substituteConstants(ImmutableSubstitution<? extends Constant> substitution,
                                           ConstructionAndFilterAndValues constructionAndFilterAndValues) {
        ValuesNode valuesNode = constructionAndFilterAndValues.valuesNode;

        ImmutableSet<Integer> substitutionVariableIndices = IntStream.range(0, valuesNode.getOrderedVariables().size())
                .filter(i -> substitution.getImmutableMap().containsKey(valuesNode.getOrderedVariables().get(i)))
                .boxed()
                .collect(ImmutableCollectors.toSet());

        ImmutableList<Variable> newVariables = IntStream.range(0, valuesNode.getOrderedVariables().size())
                .filter(i -> !substitutionVariableIndices.contains(i))
                .boxed()
                .map(i -> valuesNode.getOrderedVariables().get(i))
                .collect(ImmutableCollectors.toList());

        ImmutableList<ImmutableList<Constant>> newValues = valuesNode.getValues().stream()
                .filter(constants -> substitutionVariableIndices.stream()
                        .allMatch(i -> constants.get(i).equals(substitution.get(valuesNode.getOrderedVariables().get(i)))))
                .map(constants -> constants.stream()
                        .filter(constant -> !substitutionVariableIndices.contains(constants.indexOf(constant)))
                        .collect(ImmutableCollectors.toList()))
                .collect(ImmutableCollectors.toList());

        return new ConstructionAndFilterAndValues(constructionAndFilterAndValues.constructionNode,
                constructionAndFilterAndValues.filterNode,
                iqFactory.createValuesNode(newVariables, newValues));
    }

    private ConstructionAndFilterAndValues substituteVariables(Var2VarSubstitution variableSubstitutionFragment,
                                                               ConstructionAndFilterAndValues constructionAndFilterAndValues) {
        ImmutableList<ImmutableList<Constant>> oldValues = constructionAndFilterAndValues.valuesNode.getValues();

        ImmutableList<ImmutableTerm> renamedVariables = substitutionFactory.getInjectiveVar2VarSubstitution(
                variableSubstitutionFragment.getImmutableMap())
                .apply(constructionAndFilterAndValues.valuesNode.getOrderedVariables());

        if (renamedVariables.size() != ImmutableSet.of(renamedVariables).size()) {

            ImmutableSet<Integer> newVariableIndices = IntStream.range(0, renamedVariables.size())
                    .filter(i -> !renamedVariables.subList(0, i).contains(renamedVariables.get(i)))
                    .boxed()
                    .collect(ImmutableCollectors.toSet());

            ImmutableList<Variable> newVariables = IntStream.range(0, renamedVariables.size())
                    .filter(newVariableIndices::contains)
                    .boxed()
                    .map(i -> (Variable) renamedVariables.get(i))
                    .collect(ImmutableCollectors.toList());

            // Stream in Stream in Stream, can be optimized?
            ImmutableList<ImmutableList<Constant>> newValues = oldValues.stream()
                    .filter(constants -> IntStream.range(0, renamedVariables.size())
                            .allMatch(i -> IntStream.range(0, oldValues.size())
                                    .allMatch(j -> constants.get(i).equals(constants.get(j)) ||
                                            !(renamedVariables.get(i).equals(renamedVariables.get(j))))))
                    .map(constants -> IntStream.range(0, constants.size())
                        .filter(newVariableIndices::contains)
                        .boxed()
                        .map(constants::get)
                        .collect(ImmutableCollectors.toList()))
                    .collect(ImmutableCollectors.toList());

            return new ConstructionAndFilterAndValues(constructionAndFilterAndValues.constructionNode,
                    constructionAndFilterAndValues.filterNode,
                    iqFactory.createValuesNode(newVariables, newValues));
        }
        return constructionAndFilterAndValues;
    }

    private IQTree buildTreeFromCFV(ConstructionAndFilterAndValues constructionAndFilterAndValues) {
        if (constructionAndFilterAndValues.constructionNode == null) {
            return constructionAndFilterAndValues.valuesNode;
        }
        return iqFactory.createUnaryIQTree(constructionAndFilterAndValues
                .constructionNode, iqFactory.createUnaryIQTree(constructionAndFilterAndValues
                    .filterNode, constructionAndFilterAndValues
                        .valuesNode));
    }

    private InjectiveVar2VarSubstitution computeVariableRenaming(ImmutableSet<Variable> variables, VariableGenerator variableGenerator) {
        ImmutableMap<Variable, Variable> substitutionMap = variables.stream().
                collect(ImmutableCollectors.toMap(
                        v -> v,
                        variableGenerator::generateNewVariableFromVar));
        return substitutionFactory.getInjectiveVar2VarSubstitution(substitutionMap);
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint) {
        if (constraint.isGround())
            return this;
        getVariableNullability();
        ImmutableList<ImmutableList<Constant>> newValues = values.stream()
                .filter(constants -> !((ImmutableExpression) substitutionFactory.getSubstitution(
                        IntStream.range(0, orderedVariables.size())
                                .boxed()
                                .collect(ImmutableCollectors.toMap(
                                        orderedVariables::get,
                                        constants::get)))
                        .apply(constraint))
                        .evaluate(variableNullability)
                        .isEffectiveFalse())
                .collect(ImmutableCollectors.toList());
        return iqFactory.createValuesNode(getOrderedVariables(), newValues);
    }

    @Override
    public ImmutableSet<Variable> getKnownVariables() {
        return projectedVariables;
    }

    @Override
    public boolean isDistinct() {
        if (isDistinct == null) {
            isDistinct = (values.size() == values.stream()
                                                .unordered()
                                                .distinct()
                                                .count()); }
        return isDistinct;
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return values.isEmpty();
    }

    @Override
    public synchronized VariableNullability getVariableNullability() {
        // Implemented by looking through the values, if one of them contains a null
        // the corresponding variable is seen as nullable.
        if (variableNullability == null) {
            ImmutableSet<ImmutableSet<Variable>> nullableGroups = IntStream.range(0, orderedVariables.size())
                    .filter(i -> values.stream()
                            .map(t -> t.get(i))
                            .anyMatch(ImmutableTerm::isNull))
                    .boxed()
                    .map(orderedVariables::get)
                    .map(ImmutableSet::of)
                    .collect(ImmutableCollectors.toSet());
            variableNullability = coreUtilsFactory.createVariableNullability(nullableGroups, projectedVariables);
        }
        return variableNullability;
    }

    @Override
    public void validate() throws InvalidIntermediateQueryException {
        // TODO: Lukas, Add type checking of value/variable
        if (orderedVariables.size() != projectedVariables.size()) {
            throw new InvalidIntermediateQueryException("Variables must be unique");
        }
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints() {
        // TODO: Worth implementing?
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getNotInternallyRequiredVariables() {
        return projectedVariables;
    }

    @Override
    public ValuesNode clone() {
        return iqFactory.createValuesNode(orderedVariables, values);
    }

    @Override
    public ImmutableList<Variable> getOrderedVariables() {
        return orderedVariables;
    }

    @Override
    public String toString() {
        String valuesString = values.stream().map(tuple -> tuple.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","," (",")")))
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        return VALUES_NODE_STR + " " + orderedVariables + valuesString;
    }

    private static class ConstructionAndValues {
        public final ConstructionNode constructionNode;
        public final ValuesNode valuesNode;

        private ConstructionAndValues(ConstructionNode constructionNode, ValuesNode valuesNode) {
            this.constructionNode = constructionNode;
            this.valuesNode = valuesNode;
        }
    }

    private static class ConstructionAndFilterAndValues {
        public final ConstructionNode constructionNode;
        public final FilterNode filterNode;
        public final ValuesNode valuesNode;

        private ConstructionAndFilterAndValues(ConstructionNode constructionNode,
                                      FilterNode filterNode, ValuesNode valuesNode) {
            this.constructionNode = constructionNode;
            this.filterNode = filterNode;
            this.valuesNode = valuesNode;
        }
    }
}
