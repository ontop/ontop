package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.request.DefinitionPushDownRequest;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLAggregationFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MinOrMaxSPARQLFunctionSymbolImpl extends SPARQLFunctionSymbolImpl
        implements SPARQLAggregationFunctionSymbol {

    private final TypeFactory typeFactory;
    private final boolean isMax;
    private final RDFDatatype abstractNumericType;
    private final RDFDatatype dateTimeType;
    private final ObjectRDFType bnodeType;
    private final ObjectRDFType iriType;
    private final String defaultAggVariableName;
    private final InequalityLabel inequalityLabel;

    protected MinOrMaxSPARQLFunctionSymbolImpl(TypeFactory typeFactory, boolean isMax) {
        this(isMax ? "SP_MAX" : "SP_MIN", isMax ? SPARQL.MAX : SPARQL.MIN, typeFactory, isMax);
    }
    protected MinOrMaxSPARQLFunctionSymbolImpl(String name, String officialName, TypeFactory typeFactory, boolean isMax) {
        super(name, officialName, ImmutableList.of(typeFactory.getAbstractRDFTermType()));
        this.typeFactory = typeFactory;
        this.isMax = isMax;
        this.abstractNumericType = typeFactory.getAbstractOntopNumericDatatype();
        this.dateTimeType = typeFactory.getXsdDatetimeDatatype();
        this.bnodeType = typeFactory.getBlankNodeType();
        this.iriType = typeFactory.getIRITermType();
        this.defaultAggVariableName = isMax ? "max1" : "min1";
        this.inequalityLabel = isMax ? InequalityLabel.GT : InequalityLabel.LT;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean isAggregation() {
        return true;
    }

    /**
     * Too complex to be implemented for the moment
     */
    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.empty();
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public boolean isNullable(ImmutableSet<Integer> nullableIndexes) {
        return true;
    }

    /**
     * TODO: put it into common in an abstract class
     */
    @Override
    public Optional<AggregationSimplification> decomposeIntoDBAggregation(ImmutableList<? extends ImmutableTerm> subTerms,
                                                                          ImmutableList<ImmutableSet<RDFTermType>> possibleRDFTypes,
                                                                          boolean hasGroupBy, VariableNullability variableNullability,
                                                                          VariableGenerator variableGenerator, TermFactory termFactory) {
        if (possibleRDFTypes.size() != getArity()) {
            throw new IllegalArgumentException("The size of possibleRDFTypes is expected to match the arity of " +
                    "the function symbol");
        }
        ImmutableTerm subTerm = subTerms.get(0);
        ImmutableSet<RDFTermType> subTermPossibleTypes = possibleRDFTypes.get(0);

        switch (subTermPossibleTypes.size()) {
            case 0:
                throw new MinorOntopInternalBugException("At least one RDF type was expected to be inferred for the first sub-term");
            case 1:
                return Optional.of(decomposeUnityped(subTerm, subTermPossibleTypes.iterator().next(), hasGroupBy, variableNullability,
                        variableGenerator, termFactory));
            default:
                return Optional.of(decomposeMultityped(subTerm, subTermPossibleTypes, hasGroupBy, variableNullability,
                        variableGenerator, termFactory));
        }
    }

    private AggregationSimplification decomposeUnityped(ImmutableTerm subTerm, RDFTermType subTermType, boolean hasGroupBy,
                                                        VariableNullability variableNullability, VariableGenerator variableGenerator,
                                                        TermFactory termFactory) {

        ImmutableTerm subTermLexicalTerm = extractLexicalTerm(subTerm, termFactory);

        ImmutableFunctionalTerm dbAggTerm = createAggregate(
                subTermType,
                termFactory.getConversionFromRDFLexical2DB(subTermLexicalTerm, subTermType),
                termFactory);

        RDFTermTypeConstant subTermTypeConstant = termFactory.getRDFTermTypeConstant(subTermType);

        Variable dbAggregationVariable = variableGenerator.generateNewVariable(defaultAggVariableName);

        boolean isSubTermNullable = subTermLexicalTerm.isNullable(variableNullability.getNullableVariables());
        boolean dbAggMayReturnNull = !(hasGroupBy && (!isSubTermNullable));

        ImmutableTerm typeTerm = dbAggMayReturnNull
                ? termFactory.getIfElseNull(
                        termFactory.getDBIsNotNull(dbAggregationVariable), subTermTypeConstant)
                : subTermTypeConstant;

        ImmutableFunctionalTerm liftedTerm = termFactory.getRDFFunctionalTerm(
                termFactory.getConversion2RDFLexical(dbAggregationVariable, subTermType),
                typeTerm);

        ImmutableFunctionalTerm.FunctionalTermDecomposition decomposition = termFactory.getFunctionalTermDecomposition(
                liftedTerm,
                ImmutableMap.of(dbAggregationVariable, dbAggTerm));

        return AggregationSimplification.create(decomposition);
    }

    private ImmutableFunctionalTerm createAggregate(RDFTermType rdfType, ImmutableTerm dbSubTerm, TermFactory termFactory) {
        DBTermType dbType = rdfType.getClosestDBType(typeFactory.getDBTypeFactory());
        return isMax
                ? termFactory.getDBMax(dbSubTerm, dbType)
                : termFactory.getDBMin(dbSubTerm, dbType);
    }

    private AggregationSimplification decomposeMultityped(ImmutableTerm subTerm, ImmutableSet<RDFTermType> subTermPossibleTypes,
                                                          boolean hasGroupBy, VariableNullability variableNullability,
                                                          VariableGenerator variableGenerator, TermFactory termFactory) {

        ImmutableTerm subTermLexicalTerm = extractLexicalTerm(subTerm, termFactory);
        ImmutableTerm subTermTypeTerm = extractRDFTermTypeTerm(subTerm, termFactory);

        // One fresh variable per type
        ImmutableMap<RDFTermType, Variable> subVariableMap = subTermPossibleTypes.stream()
                .collect(ImmutableCollectors.toMap(
                        t -> t,
                        t -> variableGenerator.generateNewVariable()));

        // Each variable of the subVariableMap will have the closest DB type to its associated RDF type
        ImmutableSet<DefinitionPushDownRequest> pushDownRequests = computeRequests(subTermLexicalTerm, subTermTypeTerm,
                subVariableMap, termFactory, variableNullability);

        // One fresh variable per type
        ImmutableMap<RDFTermType, Variable> aggregateMap = subTermPossibleTypes.stream()
                .collect(ImmutableCollectors.toMap(
                        t -> t,
                        t -> variableGenerator.generateNewVariable()));

        ImmutableMap<Variable, ImmutableFunctionalTerm> substitutionMap = computeSubstitution(subVariableMap, aggregateMap,
                termFactory);

        ImmutableFunctionalTerm liftableTerm = computeLiftableTerm(aggregateMap, termFactory);

        return AggregationSimplification.create(
                termFactory.getFunctionalTermDecomposition(liftableTerm, substitutionMap),
                pushDownRequests);
    }

    private ImmutableSet<DefinitionPushDownRequest> computeRequests(ImmutableTerm subTermLexicalTerm, ImmutableTerm subTermTypeTerm,
                                                                    ImmutableMap<RDFTermType, Variable> subVariableMap,
                                                                    TermFactory termFactory, VariableNullability variableNullability) {
        return subVariableMap.entrySet().stream()
                .map(e -> computeRequest(subTermLexicalTerm, subTermTypeTerm, e.getKey(), e.getValue(), termFactory,
                        variableNullability))
                .collect(ImmutableCollectors.toSet());
    }

    private DefinitionPushDownRequest computeRequest(ImmutableTerm subTermLexicalTerm, ImmutableTerm subTermTypeTerm,
                                                     RDFTermType rdfType, Variable newVariable, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableTerm definition = termFactory.getConversionFromRDFLexical2DB(subTermLexicalTerm, rdfType)
                .simplify(variableNullability);

        ImmutableExpression condition = termFactory.getStrictEquality(subTermTypeTerm, termFactory.getRDFTermTypeConstant(rdfType));

        return DefinitionPushDownRequest.create(newVariable, definition, condition);
    }

    private ImmutableMap<Variable, ImmutableFunctionalTerm> computeSubstitution(ImmutableMap<RDFTermType, Variable> subVariableMap,
                                                                                ImmutableMap<RDFTermType, Variable> aggregateMap,
                                                                                TermFactory termFactory) {
        return aggregateMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getValue,
                        e -> createAggregate(e.getKey(), subVariableMap.get(e.getKey()), termFactory)));
    }

    private ImmutableFunctionalTerm computeLiftableTerm(ImmutableMap<RDFTermType, Variable> aggregateMap,
                                                        TermFactory termFactory) {

        ImmutableList<Map.Entry<ImmutableExpression, RDFTermTypeConstant>> typeTermWhenPairs = computeTypeTermWhenPairs(
                aggregateMap, termFactory);

        ImmutableFunctionalTerm typeTerm = termFactory.getDBCase(typeTermWhenPairs.stream(), termFactory.getNullConstant(), true);
        ImmutableFunctionalTerm lexicalTerm = computeLexicalTerm(typeTermWhenPairs, aggregateMap, termFactory);

        return termFactory.getRDFFunctionalTerm(lexicalTerm, typeTerm);
    }

    private ImmutableList<Map.Entry<ImmutableExpression, RDFTermTypeConstant>> computeTypeTermWhenPairs(
            ImmutableMap<RDFTermType, Variable> aggregateMap,
            TermFactory termFactory) {

        Optional<Map.Entry<ImmutableExpression, RDFTermTypeConstant>> bnodeEntry =
                Optional.ofNullable(aggregateMap.get(bnodeType))
                        .map(v -> createRegularTypeWhenPair(v, bnodeType, termFactory));

        Optional<Map.Entry<ImmutableExpression, RDFTermTypeConstant>> iriEntry =
                Optional.ofNullable(aggregateMap.get(iriType))
                        .map(v -> createRegularTypeWhenPair(v, iriType, termFactory));

        Stream<Map.Entry<ImmutableExpression, RDFTermTypeConstant>> objectEntries = Stream.of(bnodeEntry, iriEntry)
                .filter(Optional::isPresent)
                .map(Optional::get);

        Stream<Map.Entry<ImmutableExpression, RDFTermTypeConstant>> numericEntries = computeNumericOrDatetimeTypePairs(
                abstractNumericType, aggregateMap,
                (agg1, agg2) -> termFactory.getDBNumericInequality(inequalityLabel, agg1, agg2),
                termFactory);

        Stream<Map.Entry<ImmutableExpression, RDFTermTypeConstant>> datetimeEntries = computeNumericOrDatetimeTypePairs(
                dateTimeType, aggregateMap,
                (agg1, agg2) -> termFactory.getDBDatetimeInequality(inequalityLabel, agg1, agg2),
                termFactory);

        Stream<Map.Entry<ImmutableExpression, RDFTermTypeConstant>> otherEntries = aggregateMap.entrySet().stream()
                .filter(e -> e.getKey() instanceof RDFDatatype)
                .filter(e -> (!e.getKey().isA(abstractNumericType))
                        && (!e.getKey().isA(dateTimeType)))
                .map(e -> createRegularTypeWhenPair(e.getValue(), e.getKey(), termFactory));

        return Stream.concat(
                    Stream.concat(objectEntries, numericEntries),
                    Stream.concat(datetimeEntries, otherEntries))
                .collect(ImmutableCollectors.toList());
    }

    private BiFunction<Variable, Variable, ImmutableExpression> orOnlyFirstArgumentIsNotNull(
            BiFunction<Variable, Variable, ImmutableExpression> fct, TermFactory termFactory) {
        return (agg1, agg2) -> termFactory.getDisjunction(
                fct.apply(agg1, agg2),
                termFactory.getConjunction(
                        termFactory.getDBIsNotNull(agg1),
                        termFactory.getDBIsNull(agg2)));
    }

    private Map.Entry<ImmutableExpression, RDFTermTypeConstant> createRegularTypeWhenPair(Variable aggregateVariable,
                                                                                              RDFTermType rdfType,
                                                                                              TermFactory termFactory) {
        return Maps.immutableEntry(
                termFactory.getDBIsNotNull(aggregateVariable),
                termFactory.getRDFTermTypeConstant(rdfType));
    }

    private Stream<Map.Entry<ImmutableExpression, RDFTermTypeConstant>> computeNumericOrDatetimeTypePairs(
            RDFDatatype baseDatatype, ImmutableMap<RDFTermType, Variable> aggregateMap,
            BiFunction<Variable, Variable, ImmutableExpression> partialComparisonFct,
            TermFactory termFactory) {

        BiFunction<Variable, Variable, ImmutableExpression> comparisonFct = orOnlyFirstArgumentIsNotNull(
                partialComparisonFct, termFactory);

        ImmutableList<RDFTermType> matchingTypes = aggregateMap.keySet().stream()
                .filter(t -> t.isA(baseDatatype))
                .collect(ImmutableCollectors.toList());

        if (matchingTypes.isEmpty())
            return Stream.empty();

        Stream<Map.Entry<ImmutableExpression, RDFTermTypeConstant>> firstPairStream = IntStream.range(0, matchingTypes.size() - 1)
                .mapToObj(i -> createNumericOrDatetimeTypePair(matchingTypes.get(i),
                        matchingTypes.subList(i + 1, matchingTypes.size()), aggregateMap,
                        comparisonFct, termFactory));

        RDFTermType lastType = matchingTypes.get(matchingTypes.size() - 1);
        Map.Entry<ImmutableExpression, RDFTermTypeConstant> lastPair = createRegularTypeWhenPair(
                aggregateMap.get(lastType), lastType, termFactory);

        return Stream.concat(firstPairStream, Stream.of(lastPair));
    }

    private Map.Entry<ImmutableExpression, RDFTermTypeConstant> createNumericOrDatetimeTypePair(
            RDFTermType rdfType, ImmutableList<RDFTermType> typesToCompareWith,
            ImmutableMap<RDFTermType, Variable> aggregateMap, BiFunction<Variable, Variable, ImmutableExpression> comparisonFct,
            TermFactory termFactory) {
        Variable aggregateVariable = aggregateMap.get(rdfType);

        ImmutableExpression expression = termFactory.getConjunction(
                typesToCompareWith.stream()
                        .map(aggregateMap::get)
                        .map(agg2 -> comparisonFct.apply(aggregateVariable, agg2)))
                .orElseThrow(() -> new MinorOntopInternalBugException("At least one type to compare with was expected"));

        return Maps.immutableEntry(expression, termFactory.getRDFTermTypeConstant(rdfType));
    }

    /**
     * Replaces the "then values" of typeTermWhenPairs by the lexical values of the corresponding
     * aggregation variables.
     */
    private ImmutableFunctionalTerm computeLexicalTerm(
            ImmutableList<Map.Entry<ImmutableExpression, RDFTermTypeConstant>> typeTermWhenPairs,
            ImmutableMap<RDFTermType, Variable> aggregateMap, TermFactory termFactory) {
        Stream<Map.Entry<ImmutableExpression, ImmutableFunctionalTerm>> lexicalWhenPairs = typeTermWhenPairs.stream()
                .map(e -> {
                            RDFTermType rdfType = e.getValue().getRDFTermType();
                            ImmutableFunctionalTerm thenLexicalTerm = termFactory.getConversion2RDFLexical(
                                    aggregateMap.get(rdfType),
                                    rdfType);
                            return Maps.immutableEntry(e.getKey(), thenLexicalTerm);
                        });

        return termFactory.getDBCase(lexicalWhenPairs, termFactory.getNullConstant(),
                // TODO: double-check
                true);
    }


    @Override
    public Constant evaluateEmptyBag(TermFactory termFactory) {
        return termFactory.getNullConstant();
    }
}
