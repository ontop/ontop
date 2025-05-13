package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBStrictEqFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.StringConstantDecomposer;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
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
    private final ImmutableList<ImmutableMap<Variable, Constant>> valueMaps;

    private final CoreUtilsFactory coreUtilsFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    private final OntopModelSettings settings;

    private boolean isNormalized = false;
    // LAZY
    private VariableNullability variableNullability;
    // LAZY
    private Boolean isDistinct;

    // LAZY
    private ImmutableSet<Substitution<NonVariableTerm>> possibleVariableDefinitions;

    // LAZY
    private ImmutableSet<ImmutableSet<Variable>> uniqueConstraints;


    @AssistedInject
    protected ValuesNodeImpl(@Assisted("orderedVariables") ImmutableList<Variable> orderedVariables,
                             @Assisted("values") ImmutableList<ImmutableList<Constant>> values,
                             IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory, CoreUtilsFactory coreUtilsFactory,
                             OntopModelSettings settings, SubstitutionFactory substitutionFactory, TermFactory termFactory) {
        this(ImmutableSet.copyOf(orderedVariables),
                values.stream()
                        .map(tuple -> IntStream.range(0, orderedVariables.size())
                                .mapToObj(i -> Maps.immutableEntry(orderedVariables.get(i), tuple.get(i)))
                                .collect(ImmutableCollectors.toMap()))
                        .collect(ImmutableCollectors.toList()),
                iqTreeTools, iqFactory, coreUtilsFactory, settings, substitutionFactory, termFactory);
    }

    @AssistedInject
    protected ValuesNodeImpl(@Assisted("projectedVariables") ImmutableSet<Variable> projectedVariables,
                             @Assisted("valueMaps") ImmutableList<ImmutableMap<Variable, Constant>> valueMaps,
                             IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory, CoreUtilsFactory coreUtilsFactory,
                             OntopModelSettings settings, SubstitutionFactory substitutionFactory, TermFactory termFactory) {
        this(projectedVariables, valueMaps, null, iqTreeTools, iqFactory, coreUtilsFactory, settings,
                substitutionFactory, termFactory);
    }

    private ValuesNodeImpl(ImmutableSet<Variable> projectedVariables,
                           ImmutableList<ImmutableMap<Variable, Constant>> valueMaps,
                           @Nullable ImmutableSet<ImmutableSet<Variable>> uniqueConstraints,
                           IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory, CoreUtilsFactory coreUtilsFactory,
                           OntopModelSettings settings, SubstitutionFactory substitutionFactory, TermFactory termFactory) {
        super(iqTreeTools, iqFactory);

        this.projectedVariables = projectedVariables;
        this.orderedVariables = ImmutableList.copyOf(projectedVariables);
        this.valueMaps = valueMaps;
        this.coreUtilsFactory = coreUtilsFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.uniqueConstraints = uniqueConstraints;
        this.settings = settings;

        if (settings.isTestModeEnabled())
            validate();
    }

    @Override
    public ImmutableList<ImmutableList<Constant>> getValues() {
        return valueMaps.stream()
                .map(tuple -> orderedVariables.stream()
                        .map(tuple::get)
                        .collect(ImmutableCollectors.toList()))
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public ImmutableList<ImmutableMap<Variable, Constant>> getValueMaps() {
        return valueMaps;
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

        ImmutableSet<Variable> singleValueVariables = projectedVariables.stream()
                .filter(v -> 1 == getValueStream(v)
                        .unordered()
                        .distinct()
                        .count())
                .collect(ImmutableCollectors.toSet());

        if (!singleValueVariables.isEmpty()) {
            // Can be normalized into a construction/child node pair. Start by creating ConstructionNode.
            Substitution<ImmutableTerm> substitutions = singleValueVariables.stream()
                    .collect(substitutionFactory.toSubstitution(
                            v -> v,
                            v -> valueMaps.get(0).get(v)));

            ConstructionNode constructionNode = iqFactory.createConstructionNode(projectedVariables, substitutions);

            // Create the ValueNode
            ImmutableSet<Variable> multiValueVariables = Sets.difference(projectedVariables, singleValueVariables).immutableCopy();

            ImmutableList<ImmutableMap<Variable, Constant>> newValuesNodeValues = valueMaps.stream()
                    .map(tuple -> tuple.entrySet().stream()
                            .filter(e -> multiValueVariables.contains(e.getKey()))
                            .collect(ImmutableCollectors.toMap()))
                    .collect(ImmutableCollectors.toList());

            ValuesNode valuesNode = iqFactory.createValuesNode(multiValueVariables, newValuesNodeValues);

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


    @Override
    public Stream<Constant> getValueStream(Variable variable) {
        if (!projectedVariables.contains(variable))
            return Stream.empty();

        return valueMaps.stream()
                .map(t -> t.get(variable));
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
        if (o instanceof ValuesNodeImpl) {
            ValuesNodeImpl that = (ValuesNodeImpl) o;
            return projectedVariables.equals(that.projectedVariables) && valueMaps.equals(that.valueMaps);
        }
        return false;
    }

    @Override
    public boolean wouldKeepDescendingGroundTermInFilterAbove(Variable variable, boolean isConstant) {
        return projectedVariables.contains(variable) && !isConstant;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectedVariables, valueMaps);
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    @Override
    public ValuesNode applyFreshRenaming(InjectiveSubstitution<Variable> freshRenamingSubstitution) {
        ImmutableSet<Variable> newVariables = substitutionFactory.apply(freshRenamingSubstitution, projectedVariables);

        if (newVariables.equals(projectedVariables))
            return this;

        var newUniqueConstraints = uniqueConstraints == null
                ? null
                : uniqueConstraints.stream()
                .map(s -> substitutionFactory.apply(freshRenamingSubstitution, s))
                .collect(ImmutableCollectors.toSet());

        var newValueMaps =  valueMaps.stream()
                .map(tuple -> tuple.entrySet().stream()
                        .map(e -> Maps.immutableEntry(substitutionFactory.apply(freshRenamingSubstitution, e.getKey()), e.getValue()))
                        .collect(ImmutableCollectors.toMap()))
                .collect(ImmutableCollectors.toList());

        return new ValuesNodeImpl(newVariables, newValueMaps, newUniqueConstraints, iqTreeTools, iqFactory,
                coreUtilsFactory, settings, substitutionFactory, termFactory);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                               VariableGenerator variableGenerator) {
        if (descendingSubstitution.isEmpty())
            return this;

        final ConstructionNode constructionNode;
        final FilterNode filterNode;
        ValuesNode valuesNode = this;

        Substitution<GroundFunctionalTerm> functionalSubstitutionFragment = descendingSubstitution.restrictRangeTo(GroundFunctionalTerm.class);
        if (!functionalSubstitutionFragment.isEmpty()) {
            InjectiveSubstitution<Variable> renaming = Sets.intersection(valuesNode.getVariables(), functionalSubstitutionFragment.getDomain()).stream()
                    .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

            ImmutableExpression filterCondition = termFactory.getConjunction(
                    substitutionFactory.rename(renaming, functionalSubstitutionFragment)
                            .builder()
                            .toStream(termFactory::getStrictEquality))
                    .orElseThrow(() -> new MinorOntopInternalBugException("There must be an exception"));

                constructionNode = iqFactory.createConstructionNode(Sets.difference(valuesNode.getVariables(), descendingSubstitution.getDomain()).immutableCopy());
                filterNode = iqFactory.createFilterNode(filterCondition);
                valuesNode = valuesNode.applyFreshRenaming(renaming);
        }
        else {
            constructionNode = null;
            filterNode = null;
        }

        Substitution<Constant> constantSubstitutionFragment = descendingSubstitution.restrictRangeTo(Constant.class);
        valuesNode = substituteConstants(constantSubstitutionFragment, valuesNode);

        Substitution<Variable> variableSubstitutionFragment = descendingSubstitution.restrictRangeTo(Variable.class);
        valuesNode = substituteVariables(variableSubstitutionFragment, valuesNode);

        if (constructionNode == null) {
            return valuesNode;
        }
        return iqTreeTools.createUnaryIQTree(constructionNode, filterNode, valuesNode);
    }

    private ValuesNode substituteConstants(Substitution<Constant> substitution, ValuesNode valuesNode) {

        ImmutableSet<Variable> variables = valuesNode.getVariables();
        ImmutableSet<Variable> newProjectionVariables = Sets.difference(variables, substitution.getDomain()).immutableCopy();
        if (newProjectionVariables.size() == variables.size())
            return valuesNode;

        ImmutableList<ImmutableMap<Variable, Constant>> newValues = valuesNode.getValueMaps().stream()
                .filter(tuple -> tuple.entrySet().stream()
                        .filter(e -> substitution.isDefining(e.getKey()))
                        .allMatch(e -> e.getValue().equals(substitution.get(e.getKey()))))
                .map(tuple -> tuple.entrySet().stream()
                        .filter(e -> !substitution.isDefining(e.getKey()))
                        .collect(ImmutableCollectors.toMap()))
                .collect(ImmutableCollectors.toList());

        return iqFactory.createValuesNode(newProjectionVariables, newValues);
    }

    private ValuesNode substituteVariables(Substitution<Variable> variableSubstitutionFragment, ValuesNode valuesNode) {

        ImmutableSet<Variable> variables = valuesNode.getVariables();
        ImmutableSet<Variable> newVariables = substitutionFactory.apply(variableSubstitutionFragment, variables);
        if (newVariables.equals(variables))
            return valuesNode;

        ImmutableList<ImmutableMap<Variable, Constant>> newValues;
        if (newVariables.size() == variables.size()) {
            // one-to-one substitution
            newValues = valuesNode.getValueMaps().stream()
                    .map(tuple -> tuple.entrySet().stream()
                            .map(e -> Maps.immutableEntry(substitutionFactory.apply(variableSubstitutionFragment, e.getKey()), e.getValue()))
                            .collect(ImmutableCollectors.toMap()))
                    .collect(ImmutableCollectors.toList());
        }
        else {
            // many-to-one substitution
            ImmutableSet<Variable> firstFoundVariables = newVariables.stream()
                    .map(v -> variables.stream()
                            .filter(u -> substitutionFactory.apply(variableSubstitutionFragment, u).equals(v))
                            .findFirst()
                            .orElseThrow(() -> new MinorOntopInternalBugException("expected a non-empty pre-image")))
                    .collect(ImmutableCollectors.toSet());

            newValues = valuesNode.getValueMaps().stream()
                    .filter(tuple -> tuple.entrySet().stream()
                            .allMatch(e1 -> tuple.entrySet().stream()
                                    .filter(e2 -> variableSubstitutionFragment.apply(e1.getKey()).equals(variableSubstitutionFragment.apply(e2.getKey())))
                                    .allMatch(e2 -> e1.getValue().equals(e2.getValue()))))
                    .map(tuple -> tuple.entrySet().stream()
                            .filter(e -> firstFoundVariables.contains(e.getKey()))
                            .map(e -> Maps.immutableEntry(substitutionFactory.apply(variableSubstitutionFragment, e.getKey()), e.getValue()))
                            .collect(ImmutableCollectors.toMap()))
                    .collect(ImmutableCollectors.toList());
        }

        return iqFactory.createValuesNode(newVariables, newValues);
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

        ImmutableList<ImmutableExpression> strictEqualities = constraintClassification.get(true);
        assert strictEqualities != null;

        ImmutableList<ImmutableExpression> otherConditions = constraintClassification.get(false);
        assert otherConditions != null;

        if (strictEqualities.isEmpty()) {
            return filterValuesNodeEntries(constraint);
        }

        ImmutableExpression firstStrictEquality = strictEqualities.get(0);

        Optional<IQTree> optionalReshapedTree = tryToReshapeValuesNodeToConstructFunctionalTerm(firstStrictEquality, variableGenerator);
        if (optionalReshapedTree.isPresent()) {
            // Propagates down other constraints
            return iqTreeTools.propagateDownOptionalConstraint(
                    optionalReshapedTree.get(),
                    termFactory.getConjunction(constraint.flattenAND()
                            .filter(c -> !c.equals(firstStrictEquality))),
                    variableGenerator);
        }

        IQTree filteredValuesNode = filterValuesNodeEntries(termFactory.getConjunction(
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
            ObjectStringTemplateFunctionSymbol objectStringTemplateFunctionSymbol = ((ObjectStringTemplateFunctionSymbol) functionSymbol);

            VariableNullability simplifiedVariableNullability = coreUtilsFactory.createSimplifiedVariableNullability(functionalTerm);
            return objectStringTemplateFunctionSymbol.getDecomposer(functionalTerm.getTerms(), termFactory, simplifiedVariableNullability)
                    .flatMap(decomposer  -> decompose(decomposer, variable, objectStringTemplateFunctionSymbol, variableGenerator));
        }
        if (functionSymbol instanceof DBTypeConversionFunctionSymbol) {
            DBTypeConversionFunctionSymbol dbTypeConversionFunctionSymbol = (DBTypeConversionFunctionSymbol) functionSymbol;
            return dbTypeConversionFunctionSymbol.getDecomposer(termFactory)
                    .flatMap(decomposer -> decompose(decomposer, variable, functionSymbol, variableGenerator));
        }
        return Optional.empty();
    }

    private Optional<IQTree> decompose(StringConstantDecomposer decomposer,
                                       Variable variableToReplace, FunctionSymbol functionSymbol,
                                       VariableGenerator variableGenerator) {

        ImmutableList<Variable> newVariables = IntStream.range(0, functionSymbol.getArity())
                .mapToObj(i -> variableGenerator.generateNewVariable())
                .collect(ImmutableCollectors.toList());

        ImmutableList<ImmutableMap<Variable, Constant>> newValues = valueMaps.stream()
                .map(tuple -> Optional.of(tuple.get(variableToReplace))
                        .filter(c -> c instanceof DBConstant)
                        .map(c -> (DBConstant) c)
                        .flatMap(c -> decomposer.decompose(c)
                                .map(additionalColumns -> Streams.concat(
                                        tuple.entrySet().stream()
                                                .filter(e -> !e.getKey().equals(variableToReplace)),
                                        IntStream.range(0, newVariables.size())
                                                .mapToObj(i -> Maps.<Variable, Constant>immutableEntry(newVariables.get(i), additionalColumns.get(i))))
                                        .collect(ImmutableCollectors.toMap()))))
                .flatMap(Optional::stream)
                .collect(ImmutableCollectors.toList());

        if (newValues.isEmpty())
            return Optional.of(iqFactory.createEmptyNode(projectedVariables));

        ImmutableSet<Variable> newProjectedVariables = Sets.union(
                Sets.difference(projectedVariables, ImmutableSet.of(variableToReplace)),
                ImmutableSet.copyOf(newVariables)).immutableCopy();

        ValuesNode newValueNode = iqFactory.createValuesNode(newProjectedVariables, newValues);

        ConstructionNode constructionNode = iqFactory.createConstructionNode(
                projectedVariables,
                substitutionFactory.getSubstitution(variableToReplace,
                        termFactory.getImmutableFunctionalTerm(functionSymbol, newVariables)));

        return Optional.of(iqFactory.createUnaryIQTree(constructionNode, newValueNode)
                .normalizeForOptimization(variableGenerator));
    }


    private IQTree filterValuesNodeEntries(ImmutableExpression constraint) {
        var variableNullability = getVariableNullability();
        ImmutableList<ImmutableMap<Variable, Constant>> newValues = valueMaps.stream()
                .filter(tuple -> !(tuple.entrySet().stream().collect(substitutionFactory.toSubstitution())
                        .apply(constraint))
                        .evaluate2VL(variableNullability)
                        .isEffectiveFalse())
                .collect(ImmutableCollectors.toList());

        return iqFactory.createValuesNode(projectedVariables, newValues);
    }

    @Override
    public ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions() {
        if (possibleVariableDefinitions == null) {
            Stream<ImmutableMap<Variable, Constant>> distinctValuesStream = ((isDistinct != null) && isDistinct)
                    ? valueMaps.stream()
                    : valueMaps.stream().distinct();

            possibleVariableDefinitions = distinctValuesStream
                    .map(tuple -> tuple.entrySet().stream().collect(substitutionFactory.<NonVariableTerm>toSubstitution()))
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
            isDistinct = (valueMaps.size() == valueMaps.stream()
                                                .unordered()
                                                .distinct()
                                                .count()); }
        return isDistinct;
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return valueMaps.isEmpty();
    }

    @Override
    public synchronized VariableNullability getVariableNullability() {
        // Implemented by looking through the values, if one of them contains a null
        // the corresponding variable is seen as nullable.
        if (variableNullability == null) {
            ImmutableSet<ImmutableSet<Variable>> nullableGroups = orderedVariables.stream()
                    .filter(v -> getValueStream(v)
                            .anyMatch(ImmutableTerm::isNull))
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
    public synchronized ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints() {
        if (uniqueConstraints == null) {
            uniqueConstraints = computeUniqueConstraints();
        }
        return uniqueConstraints;
    }

    /**
     * Basic implementation: looks first for atomic unique constraints.
     *  If there is no atomic constraints, looks if the values node is distinct
     */
    private ImmutableSet<ImmutableSet<Variable>> computeUniqueConstraints() {
        int count = valueMaps.size();
        var atomicConstraints = getVariables().stream()
                .filter(v -> getValueStream(v).distinct().count() == count)
                .map(ImmutableSet::of)
                .collect(ImmutableCollectors.toSet());

        if (!atomicConstraints.isEmpty())
            return atomicConstraints;
        else if (isDistinct())
            return ImmutableSet.of(getVariables());
        else
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
        String valuesString = valueMaps.stream()
                .map(tuple -> orderedVariables.stream()
                        .map(tuple::get)
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
}
