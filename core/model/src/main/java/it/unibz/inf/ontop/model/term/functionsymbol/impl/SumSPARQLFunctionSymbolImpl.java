package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.request.DefinitionPushDownRequest;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm.FunctionalTermDecomposition;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLAggregationFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SumSPARQLFunctionSymbolImpl extends SPARQLFunctionSymbolImpl implements SPARQLAggregationFunctionSymbol {

    private final boolean isDistinct;

    protected SumSPARQLFunctionSymbolImpl(boolean isDistinct, RDFTermType rootRdfTermType) {
        super("SP_SUM", SPARQL.SUM, ImmutableList.of(rootRdfTermType));
        this.isDistinct = isDistinct;
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    /**
     * Too complex to be implemented (for the moment)
     */
    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.empty();
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    /**
     * Nullable due to typing errors
     */
    @Override
    public boolean isNullable(ImmutableSet<Integer> nullableIndexes) {
        return true;
    }

    @Override
    public Optional<AggregationSimplification> decomposeIntoDBAggregation(
            ImmutableList<? extends ImmutableTerm> subTerms, ImmutableList<ImmutableSet<RDFTermType>> possibleRDFTypes,
            boolean hasGroupBy, VariableNullability variableNullability, VariableGenerator variableGenerator, TermFactory termFactory) {
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
                return Optional.of(decomposeMultityped(subTerm, subTermPossibleTypes, hasGroupBy, variableNullability, variableGenerator, termFactory));
        }


    }

    private AggregationSimplification decomposeUnityped(ImmutableTerm subTerm, RDFTermType subTermType,
                                                        boolean hasGroupBy, VariableNullability variableNullability,
                                                        VariableGenerator variableGenerator, TermFactory termFactory) {
        if (!(subTermType instanceof ConcreteNumericRDFDatatype)) {
            FunctionalTermDecomposition decomposition = termFactory.getFunctionalTermDecomposition(termFactory.getNullConstant());
            return AggregationSimplification.create(decomposition);
        }

        ConcreteNumericRDFDatatype numericDatatype = (ConcreteNumericRDFDatatype) subTermType;
        ImmutableTerm subTermLexicalTerm = extractLexicalTerm(subTerm, termFactory);

        TypeFactory typeFactory = termFactory.getTypeFactory();
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();

        ImmutableFunctionalTerm dbSumTerm = termFactory.getDBSum(
                termFactory.getConversionFromRDFLexical2DB(subTermLexicalTerm, numericDatatype),
                numericDatatype.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory()),
                isDistinct);

        RDFTermTypeConstant inferredTypeTermWhenNonEmpty = termFactory.getRDFTermTypeConstant(
                numericDatatype.getCommonPropagatedOrSubstitutedType(numericDatatype));

        Variable dbAggregationVariable = variableGenerator.generateNewVariable("sum");

        boolean isSubTermNullable = subTermLexicalTerm.isNullable(variableNullability.getNullableVariables());
        DBConstant zero = termFactory.getDBConstant("0", dbTypeFactory.getDBLargeIntegerType());

        // If DB sum returns a NULL, replaces it by 0
        boolean dbSumMayReturnNull = !(hasGroupBy && (!isSubTermNullable));
        ImmutableTerm nonNullDBAggregate = dbSumMayReturnNull
                ? termFactory.getDBCoalesce(dbAggregationVariable, zero)
                : dbAggregationVariable;

        // TODO: consider the possibility to disable it through the settings
        ImmutableTerm inferredType = isSubTermNullable
                ? termFactory.getIfThenElse(
                    termFactory.getDBIsNotNull(dbAggregationVariable),
                    inferredTypeTermWhenNonEmpty,
                    termFactory.getRDFTermTypeConstant(typeFactory.getXsdIntegerDatatype()))
                : inferredTypeTermWhenNonEmpty;

        ImmutableFunctionalTerm liftedTerm = termFactory.getRDFFunctionalTerm(
                termFactory.getConversion2RDFLexical(nonNullDBAggregate, numericDatatype),
                inferredType);

        FunctionalTermDecomposition decomposition = termFactory.getFunctionalTermDecomposition(
                liftedTerm,
                ImmutableMap.of(dbAggregationVariable, dbSumTerm));

        return AggregationSimplification.create(decomposition);
    }

    /**
     * TODO: support
     */
    private AggregationSimplification decomposeMultityped(ImmutableTerm subTerm,
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
        ImmutableSet<RDFTermType> nonNumericTypes = subTermPossibleTypes.stream()
                .filter(t -> !(t instanceof ConcreteNumericRDFDatatype))
                .collect(ImmutableCollectors.toSet());

        /*
         * Sub-term variables: to be provided by the sub-tree
         * TODO: find a better name
         */
        ImmutableMap<ConcreteNumericRDFDatatype, Variable> numericSubTermVarMap = numericTypeMultimap.keySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> e,
                        e -> variableGenerator.generateNewVariable("num")));

        Optional<Variable> optionalIncompatibleSubVariable = nonNumericTypes.isEmpty()
                ? Optional.empty()
                : Optional.of(variableGenerator.generateNewVariable("nonNum"));

        ImmutableSet<DefinitionPushDownRequest> pushDownRequests = computeRequests(subTermLexicalTerm, subTermTypeTerm,
                numericTypeMultimap, nonNumericTypes, numericSubTermVarMap,
                optionalIncompatibleSubVariable, variableNullability, termFactory);

        ImmutableMap<ConcreteNumericRDFDatatype, Variable> numericAggregateVarMap = numericTypeMultimap.keySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> e,
                        e -> variableGenerator.generateNewVariable("agg")));

        Optional<Variable> optionalIncompatibleCountVariable = optionalIncompatibleSubVariable
                .map(v -> variableGenerator.generateNewVariable("nonNumCount"));

        ImmutableMap<Variable, ImmutableFunctionalTerm> substitutionMap = computeSubstitutionMap(
                numericAggregateVarMap, numericSubTermVarMap, optionalIncompatibleCountVariable,
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

    private DefinitionPushDownRequest createNonNumericRequest(ImmutableTerm subTermTypeTerm, Variable nonNumericVariable,
                                                              ImmutableSet<RDFTermType> nonNumericTypes,
                                                              TermFactory termFactory) {
        ImmutableTerm definition = termFactory.getDBBooleanConstant(true);

        ImmutableExpression condition = termFactory.getDisjunction(nonNumericTypes.stream()
                .map(t -> termFactory.getStrictEquality(subTermTypeTerm, termFactory.getRDFTermTypeConstant(t))))
                .orElseThrow(() -> new MinorOntopInternalBugException("At least one type was expected"));

        return DefinitionPushDownRequest.create(nonNumericVariable, definition, condition);
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

    protected ImmutableFunctionalTerm createAggregate(ConcreteNumericRDFDatatype rdfType, Variable subVariable,
                                                    TermFactory termFactory) {

        DBTermType dbType = rdfType.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory());
        return termFactory.getDBSum(subVariable, dbType, isDistinct);
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
                .boxed()
                .map(i -> computeCombiningPair(i, orderedTypes, orderedVariables, termFactory));

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
                zero);

        Stream<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> numericTypeWhenPairs = IntStream.range(0, nbTypes)
                .boxed()
                .map(i -> computeTypingPair(i, orderedTypes, orderedVariables, termFactory));

        ImmutableFunctionalTerm typeTerm = termFactory.getDBCase(
                Stream.concat(
                        // Checks the incompatibility first
                        incompatibleWhenPair
                                .map(Stream::of)
                                .orElseGet(Stream::empty),
                        // Then the numeric values
                        numericTypeWhenPairs),
                termFactory.getRDFTermTypeConstant(termFactory.getTypeFactory().getXsdIntegerDatatype()));

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

    /**
     * Recursive
     */
    private ImmutableTerm combineWithFollowingAggregates(int index, ImmutableList<Variable> orderedVariables,
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

    private ImmutableTerm combineAggregates(ImmutableTerm aggregate1, ImmutableTerm aggregate2, TermFactory termFactory) {
        DBTermType dbDecimalType = termFactory.getTypeFactory().getDBTypeFactory().getDBDecimalType();
        return termFactory.getDBBinaryNumericFunctionalTerm(SPARQL.NUMERIC_ADD, dbDecimalType, aggregate1, aggregate2);
    }

    private ImmutableTerm getNeutralElement(TermFactory termFactory) {
        return termFactory.getDBIntegerConstant(0);
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

    @Override
    public boolean isAggregation() {
        return true;
    }
}
