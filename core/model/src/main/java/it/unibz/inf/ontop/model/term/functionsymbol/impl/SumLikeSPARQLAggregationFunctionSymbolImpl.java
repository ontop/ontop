package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.request.DefinitionPushDownRequest;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.type.ConcreteNumericRDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * TODO: find a better name
 *
 * For aggregates like SUM where AGG(xsd:integer) returns an xsd:integer.
 *
 * Therefore the aggregation of integers must be done separately from the aggregation of floating numbers.
 *
 */
public abstract class SumLikeSPARQLAggregationFunctionSymbolImpl extends UnaryNumericSPARQLAggregationFunctionSymbolImpl {

    public SumLikeSPARQLAggregationFunctionSymbolImpl(String name, String officialName, boolean isDistinct,
                                                      RDFTermType rootRdfTermType, String defaultAggVariableName) {
        super(name, officialName, isDistinct, rootRdfTermType, defaultAggVariableName);
    }

    @Override
    protected AggregationSimplification decomposeMultityped(ImmutableTerm subTerm,
                                                            ImmutableSet<RDFTermType> subTermPossibleTypes,
                                                            boolean hasGroupBy, VariableNullability variableNullability,
                                                            VariableGenerator variableGenerator,
                                                            TermFactory termFactory) {
        ImmutableTerm subTermLexicalTerm = extractLexicalTerm(subTerm, termFactory);
        ImmutableTerm subTermTypeTerm = extractRDFTermTypeTerm(subTerm, termFactory);

        // "Main" type (e.g. xsd:integer, xsd:double) -> { concrete types used (e.g. xsd:positiveInteger, xsd:double) }
        ImmutableMultimap<ConcreteNumericRDFDatatype, ConcreteNumericRDFDatatype> numericTypeMultimap = subTermPossibleTypes.stream()
                .filter(t -> t instanceof ConcreteNumericRDFDatatype)
                .map(t -> (ConcreteNumericRDFDatatype) t)
                .collect(ImmutableCollectors.toMultimap(
                        t -> t.getCommonPropagatedOrSubstitutedType(t),
                        t -> t
                ));

        // E.g. IRI, xsd:string, etc.
        ImmutableSet<RDFTermType> nonNumericTypes = extractNonNumericTypes(subTermPossibleTypes);

        /*
         * Sub-variables: to be provided by the sub-tree
         */
        ImmutableMap<ConcreteNumericRDFDatatype, Variable> numericSubVarMap = numericTypeMultimap.keySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> e,
                        e -> variableGenerator.generateNewVariable("num")));

        Optional<Variable> optionalIncompatibleSubVariable = nonNumericTypes.isEmpty()
                ? Optional.empty()
                : Optional.of(variableGenerator.generateNewVariable("nonNum"));

        /*
         * Requests to make sure that the sub-tree with provide these novel numeric and non-numeric variables
         */
        ImmutableSet<DefinitionPushDownRequest> pushDownRequests = computeRequests(subTermLexicalTerm, subTermTypeTerm,
                numericTypeMultimap, nonNumericTypes, numericSubVarMap,
                optionalIncompatibleSubVariable, variableNullability, termFactory);

        ImmutableMap<ConcreteNumericRDFDatatype, Variable> numericAggregateVarMap = numericTypeMultimap.keySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> e,
                        e -> variableGenerator.generateNewVariable("agg")));

        Optional<Variable> optionalIncompatibleCountVariable = optionalIncompatibleSubVariable
                .map(v -> variableGenerator.generateNewVariable("nonNumCount"));

        ImmutableMap<Variable, ImmutableFunctionalTerm> substitutionMap = computeSubstitutionMap(
                numericAggregateVarMap, numericSubVarMap, optionalIncompatibleCountVariable,
                optionalIncompatibleSubVariable, termFactory);

        ImmutableFunctionalTerm liftableTerm = computeLiftableTerm(numericAggregateVarMap, optionalIncompatibleCountVariable,
                termFactory);

        return AggregationSimplification.create(
                termFactory.getFunctionalTermDecomposition(liftableTerm, substitutionMap),
                pushDownRequests);
    }


    private ImmutableSet<DefinitionPushDownRequest> computeRequests(
            ImmutableTerm subTermLexicalTerm, ImmutableTerm subTermTypeTerm,
            ImmutableMultimap<ConcreteNumericRDFDatatype, ConcreteNumericRDFDatatype> numericTypeMultimap,
            ImmutableSet<RDFTermType> nonNumericTypes,
            ImmutableMap<ConcreteNumericRDFDatatype, Variable> numericSubTermVarMap,
            Optional<Variable> optionalIncompatibleVariable,
            VariableNullability variableNullability, TermFactory termFactory) {

        Stream<DefinitionPushDownRequest> numericPushDownRequestStream = numericTypeMultimap.asMap().entrySet().stream()
                .map(e -> createNumericRequest(e.getKey(), e.getValue(), numericSubTermVarMap.get(e.getKey()),
                        subTermLexicalTerm, subTermTypeTerm, variableNullability, termFactory));

        return Stream.concat(numericPushDownRequestStream,
                optionalIncompatibleVariable
                        .map(v -> createNonNumericRequest(subTermTypeTerm, v, nonNumericTypes, termFactory))
                        .map(Stream::of)
                        .orElseGet(Stream::empty))
                .collect(ImmutableCollectors.toSet());
    }

    private DefinitionPushDownRequest createNumericRequest(ConcreteNumericRDFDatatype mainNumericType,
                                                           Collection<ConcreteNumericRDFDatatype> usedTypes,
                                                           Variable numericVariable,
                                                           ImmutableTerm subTermLexicalTerm,
                                                           ImmutableTerm subTermTypeTerm,
                                                           VariableNullability variableNullability,
                                                           TermFactory termFactory) {
        ImmutableTerm definition = termFactory.getConversionFromRDFLexical2DB(subTermLexicalTerm, mainNumericType)
                .simplify(variableNullability);

        ImmutableExpression condition = termFactory.getDisjunction(usedTypes.stream()
                .map(t -> termFactory.getStrictEquality(subTermTypeTerm, termFactory.getRDFTermTypeConstant(t))))
                .orElseThrow(() -> new MinorOntopInternalBugException("At least one type was expected"));

        return DefinitionPushDownRequest.create(numericVariable, definition, condition);
    }

    private ImmutableMap<Variable, ImmutableFunctionalTerm> computeSubstitutionMap(
            ImmutableMap<ConcreteNumericRDFDatatype, Variable> numericAggregateVarMap,
            ImmutableMap<ConcreteNumericRDFDatatype, Variable> numericSubTermVarMap, Optional<Variable> optionalIncompatibleCountVariable,
            Optional<Variable> optionalIncompatibleSubVariable, TermFactory termFactory) {

        Stream<Map.Entry<Variable, ImmutableFunctionalTerm>> numericAggregationStream = numericAggregateVarMap.entrySet().stream()
                .map(e -> Maps.immutableEntry(
                        e.getValue(),
                        createAggregate(e.getKey(), numericSubTermVarMap.get(e.getKey()), termFactory)));

        /*
         * Creates a COUNT for the incompatible types
         */
        Stream<Map.Entry<Variable, ImmutableFunctionalTerm>> incompatibleEntryStream = optionalIncompatibleCountVariable
                .map(v -> Maps.immutableEntry(v, termFactory.getDBCount(optionalIncompatibleSubVariable.get(), false)))
                .map(Stream::of)
                .orElseGet(Stream::empty);

        return Stream.concat(numericAggregationStream, incompatibleEntryStream)
                .collect(ImmutableCollectors.toMap());
    }

    private ImmutableFunctionalTerm computeLiftableTerm(ImmutableMap<ConcreteNumericRDFDatatype, Variable> numericAggregateVarMap,
                                                        Optional<Variable> optionalIncompatibleCountVariable,
                                                        TermFactory termFactory) {
        // xsd:double first, xsd:integer last
        //noinspection ComparatorMethodParameterNotUsed
        ImmutableList<ConcreteNumericRDFDatatype> orderedTypes = numericAggregateVarMap.keySet().stream()
                .sorted((t1, t2) -> t1.getCommonPropagatedOrSubstitutedType(t2).equals(t2) ? 1 : -1)
                .collect(ImmutableCollectors.toList());

        ImmutableList<Variable> orderedVariables = orderedTypes.stream()
                .map(numericAggregateVarMap::get)
                .collect(ImmutableCollectors.toList());

        int nbTypes = numericAggregateVarMap.size();

        Stream<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> numericLexicalWhenPairs = IntStream.range(0, nbTypes)
                .mapToObj(i -> computeCombiningPair(i, orderedTypes, orderedVariables, termFactory));

        DBConstant zero = termFactory.getDBIntegerConstant(0);

        Optional<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> incompatibleWhenPair = optionalIncompatibleCountVariable
                .map(v -> termFactory.getDBNumericInequality(InequalityLabel.GT, v, zero))
                .map(c -> Maps.immutableEntry(c, termFactory.getNullConstant()));

        ImmutableFunctionalTerm lexicalTerm = termFactory.getDBCase(
                Stream.concat(
                        // Checks the incompatibility first
                        incompatibleWhenPair
                                .map(Stream::of)
                                .orElseGet(Stream::empty),
                        // Then the numeric values
                        numericLexicalWhenPairs),
                termFactory.getDBStringConstant("0"), true);

        Stream<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> numericTypeWhenPairs = IntStream.range(0, nbTypes)
                .mapToObj(i -> computeTypingPair(i, orderedTypes, orderedVariables, termFactory));

        ImmutableFunctionalTerm typeTerm = termFactory.getDBCase(
                Stream.concat(
                        // Checks the incompatibility first
                        incompatibleWhenPair
                                .map(Stream::of)
                                .orElseGet(Stream::empty),
                        // Then the numeric values
                        numericTypeWhenPairs),
                termFactory.getRDFTermTypeConstant(termFactory.getTypeFactory().getXsdIntegerDatatype()),
                true);

        return termFactory.getRDFFunctionalTerm(lexicalTerm, typeTerm);
    }

    /**
     * Condition and a DB string (lexical) thenValue
     */
    private Map.Entry<ImmutableExpression, ? extends ImmutableTerm> computeCombiningPair(
            int index, ImmutableList<ConcreteNumericRDFDatatype> orderedTypes,
            ImmutableList<Variable> orderedVariables, TermFactory termFactory) {
        ConcreteNumericRDFDatatype currentType = orderedTypes.get(index);
        Variable currentVariable = orderedVariables.get(index);

        ImmutableExpression condition = termFactory.getDBIsNotNull(currentVariable);

        ImmutableFunctionalTerm thenValue = termFactory.getConversion2RDFLexical(
                combineWithFollowingAggregates(index, orderedVariables, termFactory, true), currentType);

        return Maps.immutableEntry(condition, thenValue);
    }

    private Map.Entry<ImmutableExpression, ? extends ImmutableTerm> computeTypingPair(
            int index, ImmutableList<ConcreteNumericRDFDatatype> orderedTypes,
            ImmutableList<Variable> orderedVariables, TermFactory termFactory) {
        ConcreteNumericRDFDatatype currentType = orderedTypes.get(index);
        Variable currentVariable = orderedVariables.get(index);

        ImmutableExpression condition = termFactory.getDBIsNotNull(currentVariable);
        RDFTermTypeConstant thenValue = termFactory.getRDFTermTypeConstant(currentType);

        return Maps.immutableEntry(condition, thenValue);
    }

    /**
     * Recursive
     */
    protected ImmutableTerm combineWithFollowingAggregates(int index, ImmutableList<Variable> orderedVariables,
                                                           TermFactory termFactory, boolean isTop) {
        Variable currentVariable = orderedVariables.get(index);
        // If top, is guaranteed not to be null
        ImmutableTerm currentTerm = isTop
                ? currentVariable
                : termFactory.getDBCoalesce(currentVariable, getNeutralElement(termFactory));

        // Stopping condition
        if (index == (orderedVariables.size() - 1)) {
            return currentTerm;
        }

        // (Non-tail recursive)
        return combineAggregates(
                currentTerm,
                combineWithFollowingAggregates(index + 1, orderedVariables, termFactory, false),
                termFactory);
    }

    protected abstract ImmutableTerm combineAggregates(ImmutableTerm aggregate1, ImmutableTerm aggregate2, TermFactory termFactory);

    protected abstract ImmutableTerm getNeutralElement(TermFactory termFactory);
}
