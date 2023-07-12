package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBStrictEqFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.StringConstantDecomposer;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Objects;
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
    private final TermFactory termFactory;

    private boolean isNormalized = false;
    // LAZY
    private VariableNullability variableNullability;
    // LAZY
    private Boolean isDistinct;

    // LAZY
    private ImmutableSet<Substitution<NonVariableTerm>> possibleVariableDefinitions;


    @AssistedInject
    protected ValuesNodeImpl(@Assisted("orderedVariables") ImmutableList<Variable> orderedVariables,
                             @Assisted("values") ImmutableList<ImmutableList<Constant>> values,
                             IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory, CoreUtilsFactory coreUtilsFactory,
                             OntopModelSettings settings, SubstitutionFactory substitutionFactory, TermFactory termFactory) {
        super(iqTreeTools, iqFactory);
        this.orderedVariables = orderedVariables;
        this.projectedVariables = ImmutableSet.copyOf(orderedVariables);
        this.values = values;
        this.coreUtilsFactory = coreUtilsFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;

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
                    iqFactory.createIQTreeCache(true));
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
            Substitution<ImmutableTerm> substitutions = singleValueVariableIndices.stream()
                    .collect(substitutionFactory.toSubstitution(
                            orderedVariables::get,
                            i -> values.get(0).get(i)));

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
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return projectedVariables;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValuesNodeImpl that = (ValuesNodeImpl) o;
        return projectedVariables.equals(that.projectedVariables) && values.equals(that.values);
    }

    @Override
    public boolean wouldKeepDescendingGroundTermInFilterAbove(Variable variable, boolean isConstant) {
        return projectedVariables.contains(variable) && (!isConstant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectedVariables, values);
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
    public <T> IQTree acceptTransformer(IQTreeExtendedTransformer<T> transformer, T context) {
        return transformer.transformValues(this, context);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor) {
        return visitor.visitValues(this);
    }

    @Override
    public ValuesNode applyFreshRenaming(InjectiveSubstitution<Variable> freshRenamingSubstitution) {
        ImmutableList<Variable> newVariables = substitutionFactory.apply(freshRenamingSubstitution, orderedVariables);

        return newVariables.equals(orderedVariables)
                ? this
                : iqFactory.createValuesNode(newVariables, values);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                               VariableGenerator variableGenerator) {
        if (descendingSubstitution.isEmpty())
            return this;
        ConstructionAndFilterAndValues constructionAndFilterAndValues =
                new ConstructionAndFilterAndValues(null, null, this);

        Substitution<GroundFunctionalTerm> functionalSubstitutionFragment = descendingSubstitution.restrictRangeTo(GroundFunctionalTerm.class);
        if (!functionalSubstitutionFragment.isEmpty()) {
            constructionAndFilterAndValues = addProjectedVariablesToConstruction(descendingSubstitution, constructionAndFilterAndValues);
            constructionAndFilterAndValues = substituteGroundFunctionalTerms(functionalSubstitutionFragment, constructionAndFilterAndValues, variableGenerator);
        }

        Substitution<Constant> constantSubstitutionFragment = descendingSubstitution.restrictRangeTo(Constant.class);
        if (!constantSubstitutionFragment.isEmpty()) {
            constructionAndFilterAndValues = substituteConstants(constantSubstitutionFragment, constructionAndFilterAndValues);
        }

        Substitution<Variable> variableSubstitutionFragment = descendingSubstitution.restrictRangeTo(Variable.class);
        if (!variableSubstitutionFragment.isEmpty()) {
            constructionAndFilterAndValues = substituteVariables(variableSubstitutionFragment, constructionAndFilterAndValues, iqFactory);
        }
        return buildTreeFromCFV(constructionAndFilterAndValues);
    }

    private ConstructionAndFilterAndValues substituteGroundFunctionalTerms(Substitution<? extends GroundFunctionalTerm> substitution,
                                                                           ConstructionAndFilterAndValues constructionAndFilterAndValues,
                                                                           VariableGenerator variableGenerator) {
        ValuesNode valuesNode = constructionAndFilterAndValues.valuesNode;
        InjectiveSubstitution<Variable> renaming = Sets.intersection(valuesNode.getVariables(), substitution.getDomain()).stream()
                .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

        return termFactory.getConjunction(substitutionFactory.rename(renaming, substitution).builder().toStream(termFactory::getStrictEquality))
                .map(filterCondition ->
                        new ConstructionAndFilterAndValues(
                                constructionAndFilterAndValues.constructionNode,
                                iqFactory.createFilterNode(filterCondition),
                                valuesNode.applyFreshRenaming(renaming)))
                .orElseGet(() -> new ConstructionAndFilterAndValues(null, null, valuesNode));
    }

    private ConstructionAndFilterAndValues addProjectedVariablesToConstruction(Substitution<? extends VariableOrGroundTerm> substitution,
                                                                               ConstructionAndFilterAndValues constructionAndFilterAndValues) {
        return new ConstructionAndFilterAndValues(
                iqFactory.createConstructionNode(
                        Sets.difference(constructionAndFilterAndValues.valuesNode.getVariables(), substitution.getDomain()).immutableCopy()),
                constructionAndFilterAndValues.filterNode,
                constructionAndFilterAndValues.valuesNode);
    }

    private ConstructionAndFilterAndValues substituteConstants(Substitution<Constant> substitution,
                                                               ConstructionAndFilterAndValues constructionAndFilterAndValues) {
        ValuesNode formerValuesNode = constructionAndFilterAndValues.valuesNode;
        ImmutableList<Variable> formerOrderedVariables = formerValuesNode.getOrderedVariables();
        ImmutableList<ImmutableList<Constant>> formerValues = formerValuesNode.getValues();
        int formerArity = formerOrderedVariables.size();

        ImmutableSet<Integer> substitutionVariableIndices = IntStream.range(0, formerArity)
                .filter(i -> substitution.isDefining(formerOrderedVariables.get(i)))
                .boxed()
                .collect(ImmutableCollectors.toSet());

        ImmutableList<Variable> newOrderedVariables = IntStream.range(0, formerArity)
                .filter(i -> !substitutionVariableIndices.contains(i))
                .mapToObj(formerOrderedVariables::get)
                .collect(ImmutableCollectors.toList());

        ImmutableList<ImmutableList<Constant>> newValues = formerValues.stream()
                .filter(tuple -> substitutionVariableIndices.stream()
                        .allMatch(i -> tuple.get(i).equals(substitution.get(formerOrderedVariables.get(i)))))
                .map(tuple -> IntStream.range(0, tuple.size())//tuple.stream()
                        .filter(i -> !substitutionVariableIndices.contains(i))
                        .mapToObj(tuple::get)
                        .collect(ImmutableCollectors.toList()))
                .collect(ImmutableCollectors.toList());

        return new ConstructionAndFilterAndValues(constructionAndFilterAndValues.constructionNode,
                constructionAndFilterAndValues.filterNode,
                iqFactory.createValuesNode(newOrderedVariables, newValues));
    }

    private ConstructionAndFilterAndValues substituteVariables(Substitution<Variable> variableSubstitutionFragment,
                                                               ConstructionAndFilterAndValues constructionAndFilterAndValues,
                                                               IntermediateQueryFactory iqFactory) {
        ValuesNode formerValuesNode = constructionAndFilterAndValues.valuesNode;
        ImmutableList<Variable> formerOrderedVariables = formerValuesNode.getOrderedVariables();
        ImmutableList<ImmutableList<Constant>> formerValues = formerValuesNode.getValues();
        int formerArity = formerOrderedVariables.size();

        ImmutableList<Variable> substitutedOrderedVariables = substitutionFactory.apply(variableSubstitutionFragment, formerOrderedVariables);

        if (substitutedOrderedVariables.equals(formerOrderedVariables))
            return constructionAndFilterAndValues;

        ImmutableList<Integer> firstFoundVariableIndices = substitutedOrderedVariables.stream()
                .distinct()
                .map(substitutedOrderedVariables::indexOf)
                // Ascending order
                .sorted()
                .collect(ImmutableCollectors.toList());

        if (firstFoundVariableIndices.size() == formerArity) {
            return new ConstructionAndFilterAndValues(constructionAndFilterAndValues.constructionNode,
                    constructionAndFilterAndValues.filterNode,
                    iqFactory.createValuesNode(substitutedOrderedVariables, formerValues));
        }

        ImmutableList<Variable> newOrderedVariables = firstFoundVariableIndices.stream()
                .map(substitutedOrderedVariables::get)
                .collect(ImmutableCollectors.toList());

        // Stream in Stream in Stream, can be optimized?
        ImmutableList<ImmutableList<Constant>> newValues = formerValues.stream()
                .filter(tuple -> IntStream.range(0, formerArity)
                        .allMatch(i -> IntStream.range(0, formerArity)
                                .filter(j -> j != i)
                                .allMatch(j -> tuple.get(i).equals(tuple.get(j)) ||
                                        !(substitutedOrderedVariables.get(i).equals(substitutedOrderedVariables.get(j))))))
                .map(tuple -> firstFoundVariableIndices.stream()
                        .map(tuple::get)
                        .collect(ImmutableCollectors.toList()))
                .collect(ImmutableCollectors.toList());

        return new ConstructionAndFilterAndValues(constructionAndFilterAndValues.constructionNode,
                constructionAndFilterAndValues.filterNode,
                iqFactory.createValuesNode(newOrderedVariables, newValues));
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

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, VariableGenerator variableGenerator) {
        if (constraint.isGround())
            return this;

        ImmutableMap<Boolean, ImmutableList<ImmutableExpression>> constraintClassification = constraint.flattenAND()
                .collect(ImmutableCollectors.partitioningBy(
                        e -> (e.getFunctionSymbol() instanceof DBStrictEqFunctionSymbol)
                                && e.getArity() == 2
                                && e.getTerms().stream()
                                .filter(t -> t instanceof Variable)
                                .map(t -> (Variable) t)
                                .anyMatch(projectedVariables::contains)));

        ImmutableList<ImmutableExpression> strictEqualities = Optional.ofNullable(constraintClassification.get(true))
                .orElseGet(ImmutableList::of);

        ImmutableList<ImmutableExpression> otherConditions = Optional.ofNullable(constraintClassification.get(false))
                .orElseGet(ImmutableList::of);

        if (strictEqualities.isEmpty()) {
            return filterValuesNodeEntries(constraint);
        }

        ImmutableExpression firstStrictEquality = strictEqualities.get(0);

        Optional<IQTree> optionalReshapedTree = tryToReshapeValuesNodeToConstructFunctionalTerm(firstStrictEquality, variableGenerator);
        if (optionalReshapedTree.isPresent()) {
            IQTree reshapedTree = optionalReshapedTree.get();
            // Propagates down other constraints
            return termFactory.getConjunction(constraint.flattenAND()
                            .filter(c -> !c.equals(firstStrictEquality)))
                    .map(c -> reshapedTree.propagateDownConstraint(c, variableGenerator))
                    .orElse(reshapedTree);
        }

        ValuesNode filteredValuesNode = filterValuesNodeEntries(termFactory.getConjunction(
                        Stream.concat(
                                Stream.of(firstStrictEquality),
                                otherConditions.stream())
                                .collect(ImmutableCollectors.toList())));

        ImmutableList<ImmutableExpression> otherStrictEqualities = strictEqualities.subList(1, strictEqualities.size());
        return otherStrictEqualities.isEmpty()
                ? filteredValuesNode
                : propagateDownConstraint(termFactory.getConjunction(otherStrictEqualities), variableGenerator);
    }

    /**
     * Tries to extract a functional term to match the strict equality and allow further decomposition
     * Tries to return an IQTree constructing the expected functional term and to which rows have been filtered.
     * If not possible, returns empty.
     */
    private Optional<IQTree> tryToReshapeValuesNodeToConstructFunctionalTerm(ImmutableExpression binaryStrictEquality,
                                                                             VariableGenerator variableGenerator) {
        Variable variable = binaryStrictEquality.getTerms().stream()
                .filter(t -> t instanceof Variable)
                .map(v -> (Variable) v)
                .filter(projectedVariables::contains)
                .findAny()
                .orElseThrow(() -> new MinorOntopInternalBugException("A projected variable was expected as argument"));

        Optional<ImmutableFunctionalTerm> optionalFunctionalArgument = binaryStrictEquality.getTerms().stream()
                .filter(t -> !t.equals(variable))
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm) t)
                .findAny();

        if (optionalFunctionalArgument.isEmpty())
            return Optional.empty();

        ImmutableFunctionalTerm functionalTerm = optionalFunctionalArgument.get();
        FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();

        if (functionSymbol instanceof ObjectStringTemplateFunctionSymbol) {
            return decomposeObjectTemplateString(variableGenerator, variable, functionalTerm,
                    (ObjectStringTemplateFunctionSymbol) functionSymbol);
        }
        if (functionSymbol instanceof DBTypeConversionFunctionSymbol) {
            return ((DBTypeConversionFunctionSymbol) functionSymbol).getDecomposer(termFactory)
                    .flatMap(decomposer -> decompose(decomposer, variable, functionSymbol, variableGenerator));
        }
        return Optional.empty();
    }

    private Optional<IQTree> decomposeObjectTemplateString(VariableGenerator variableGenerator, Variable variableToReplace,
                                                           ImmutableFunctionalTerm functionalTerm,
                                                           ObjectStringTemplateFunctionSymbol templateFunctionSymbol) {
        VariableNullability simplifiedVariableNullability = coreUtilsFactory.createSimplifiedVariableNullability(functionalTerm);

        var optionalDecomposer = templateFunctionSymbol.getDecomposer(functionalTerm.getTerms(),
                termFactory, simplifiedVariableNullability);
        if (optionalDecomposer.isEmpty())
            return Optional.empty();

        StringConstantDecomposer decomposer = optionalDecomposer.get();

        return decompose(decomposer, variableToReplace, templateFunctionSymbol, variableGenerator);
    }

    private Optional<IQTree> decompose(StringConstantDecomposer decomposer,
                                       Variable variableToReplace, FunctionSymbol functionSymbol,
                                       VariableGenerator variableGenerator) {
        int variablePosition = orderedVariables.indexOf(variableToReplace);

        ImmutableList<ImmutableList<Constant>> newValues = values.stream()
                .map(row -> Optional.of(row.get(variablePosition))
                        .filter(c -> c instanceof DBConstant)
                        .map(c -> (DBConstant) c)
                        .flatMap(c -> decomposer.decompose(c)
                                .map(additionalColumns -> mergeColumns(row, variablePosition, additionalColumns))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toList());

        return buildNewTreeWithDecomposition(variableGenerator, variableToReplace, functionSymbol, newValues);
    }


    private Optional<IQTree> buildNewTreeWithDecomposition(VariableGenerator variableGenerator, Variable variableToReplace,
                                                           FunctionSymbol functionSymbol,
                                                           ImmutableList<ImmutableList<Constant>> newValues) {
        if (newValues.isEmpty())
            return Optional.of(iqFactory.createEmptyNode(projectedVariables));

        ImmutableList<Variable> newVariables = IntStream.range(0, functionSymbol.getArity())
                .mapToObj(i -> variableGenerator.generateNewVariable())
                .collect(ImmutableCollectors.toList());

        ImmutableList<Variable> newOrderedVariables = Stream.concat(
                orderedVariables.stream()
                        .filter(v -> !v.equals(variableToReplace)),
                newVariables.stream())
                .collect(ImmutableCollectors.toList());

        ValuesNode newValueNode = iqFactory.createValuesNode(newOrderedVariables, newValues);

        ConstructionNode constructionNode = iqFactory.createConstructionNode(
                projectedVariables,
                substitutionFactory.getSubstitution(variableToReplace,
                        termFactory.getImmutableFunctionalTerm(functionSymbol, newVariables)));

        return Optional.of(iqFactory.createUnaryIQTree(constructionNode, newValueNode)
                .normalizeForOptimization(variableGenerator));
    }

    private ImmutableList<Constant> mergeColumns(ImmutableList<Constant> row, int variableToRemovePosition,
                                                 ImmutableList<? extends Constant> additionalColumns) {
        return Stream.concat(
                        IntStream.range(0, row.size())
                                .filter(i -> i != variableToRemovePosition)
                                .mapToObj(row::get),
                        additionalColumns.stream())
                .collect(ImmutableCollectors.toList());
    }


    private ValuesNode filterValuesNodeEntries(ImmutableExpression constraint) {
        var variableNullability = getVariableNullability();
        ImmutableList<ImmutableList<Constant>> newValues = values.stream()
                .filter(constants -> !(substitutionFactory.getSubstitution(orderedVariables, constants)
                        .apply(constraint))
                        .evaluate(variableNullability)
                        .isEffectiveFalse())
                .collect(ImmutableCollectors.toList());
        return iqFactory.createValuesNode(getOrderedVariables(), newValues);
    }

    @Override
    public ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions() {
        if (possibleVariableDefinitions == null) {
            Stream<ImmutableList<Constant>> distinctValuesStream = ((isDistinct != null) && isDistinct)
                    ? values.stream()
                    : values.stream().distinct();

            possibleVariableDefinitions = distinctValuesStream
                    .map(row -> substitutionFactory.<NonVariableTerm>getSubstitution(orderedVariables, row))
                    .collect(ImmutableCollectors.toSet());
        }
        return possibleVariableDefinitions;
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
                    .mapToObj(orderedVariables::get)
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
    public FunctionalDependencies inferFunctionalDependencies() {
        // TODO: Worth implementing?
        return FunctionalDependencies.empty();
    }

    @Override
    public VariableNonRequirement getVariableNonRequirement() {
        return VariableNonRequirement.of(projectedVariables);
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
