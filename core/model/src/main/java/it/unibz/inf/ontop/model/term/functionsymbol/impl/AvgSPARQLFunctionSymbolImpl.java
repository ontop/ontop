package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.request.DefinitionPushDownRequest;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.type.ConcreteNumericRDFDatatype;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class AvgSPARQLFunctionSymbolImpl extends UnaryNumericSPARQLAggregationFunctionSymbolImpl {

    private static final String DEFAULT_AGG_VAR_NAME = "avg1";

    protected AvgSPARQLFunctionSymbolImpl(RDFTermType rootRdfTermType, boolean isDistinct) {
        super("SP_AVG"+ (isDistinct ? "_DISTINCT" : ""), SPARQL.AVG, isDistinct, rootRdfTermType, DEFAULT_AGG_VAR_NAME);
    }

    @Override
    protected ImmutableFunctionalTerm createAggregate(ConcreteNumericRDFDatatype rdfType, ImmutableTerm dbTerm,
                                                      TermFactory termFactory) {
        DBTermType dbType = rdfType.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory());
        return termFactory.getDBAvg(dbTerm, dbType, isDistinct());
    }

    @Override
    protected ConcreteNumericRDFDatatype inferTypeWhenNonEmpty(ConcreteNumericRDFDatatype inputNumericDatatype, TypeFactory typeFactory) {
        ConcreteNumericRDFDatatype propagatedOrSubstitutedType = inputNumericDatatype.getCommonPropagatedOrSubstitutedType(
                inputNumericDatatype);

        return propagatedOrSubstitutedType.equals(typeFactory.getXsdIntegerDatatype())
                ? typeFactory.getXsdDecimalDatatype()
                : propagatedOrSubstitutedType;
    }

    /**
     * Here the aggregated value will be a DB decimal
     * (we ignore the distinction between decimal, float and double)
     *
     * 3 possible XSD datatypes are possible for the output: DECIMAL, FLOAT and DOUBLE
     *
     */
    @Override
    protected AggregationSimplification decomposeMultityped(ImmutableTerm subTerm,
                                                            ImmutableSet<RDFTermType> subTermPossibleTypes,
                                                            boolean hasGroupBy, VariableNullability variableNullability,
                                                            VariableGenerator variableGenerator, TermFactory termFactory) {
        ImmutableTerm subTermLexicalTerm = extractLexicalTerm(subTerm, termFactory);
        ImmutableTerm subTermTypeTerm = extractRDFTermTypeTerm(subTerm, termFactory);

        // E.g. IRI, xsd:string, etc.
        ImmutableSet<RDFTermType> nonNumericTypes = extractNonNumericTypes(subTermPossibleTypes);

        // Only the "core" numeric types (integer, decimal, float and double)
        ImmutableSet<ConcreteNumericRDFDatatype> numericTypes = subTermPossibleTypes.stream()
                .filter(t -> t instanceof ConcreteNumericRDFDatatype)
                .map(t -> (ConcreteNumericRDFDatatype) t)
                .map(t -> t.getCommonPropagatedOrSubstitutedType(t))
                .collect(ImmutableCollectors.toSet());

        /*
         * Sub-variables: to be provided by the sub-tree
         */
        Optional<Variable> optionalNumSubVariable = numericTypes.isEmpty()
                ? Optional.empty()
                : Optional.of(variableGenerator.generateNewVariable("num1"));

        // We need to know if there are non-numeric values
        Optional<Variable> optionalIncompatibleSubVariable = nonNumericTypes.isEmpty()
                ? Optional.empty()
                : Optional.of(variableGenerator.generateNewVariable("nonNum1"));

        // We need to know if there are float and double values (for type promotion)
        ImmutableMap<ConcreteNumericRDFDatatype, Variable> floatAndDoubleNumVariableMap = numericTypes.stream()
                .filter(t -> t.getIRI().equals(XSD.FLOAT)
                        || t.getIRI().equals(XSD.DOUBLE))
                .collect(ImmutableCollectors.toMap(
                        t -> t,
                        t -> variableGenerator.generateNewVariable("floatOrDouble1")));

        /*
         * Requests to make sure that the sub-tree with provide these novel numeric and non-numeric variables
         */
        ImmutableSet<DefinitionPushDownRequest> pushDownRequests = computeRequests(subTermLexicalTerm, subTermTypeTerm,
                nonNumericTypes, optionalNumSubVariable, optionalIncompatibleSubVariable, floatAndDoubleNumVariableMap,
                variableNullability, termFactory);

        Optional<Variable> optionalNumAvgVariable = optionalNumSubVariable
                .map(v -> variableGenerator.generateNewVariable(DEFAULT_AGG_VAR_NAME));

        Optional<Variable> optionalIncompatibleCountVariable = optionalIncompatibleSubVariable
                .map(v -> variableGenerator.generateNewVariable("nonNumCount"));

        ImmutableMap<ConcreteNumericRDFDatatype, Variable> floatAndDoubleCountVariableMap = floatAndDoubleNumVariableMap.keySet().stream()
                .collect(ImmutableCollectors.toMap(
                        t -> t,
                        t -> variableGenerator.generateNewVariable("count1")));


        ImmutableMap<Variable, ImmutableFunctionalTerm> substitutionMap = computeSubstitutionMap(
                optionalNumAvgVariable, optionalNumSubVariable,
                optionalIncompatibleCountVariable, optionalIncompatibleSubVariable,
                floatAndDoubleNumVariableMap, floatAndDoubleCountVariableMap,
                termFactory);

        ImmutableFunctionalTerm liftableTerm = computeLiftableTerm(optionalNumAvgVariable, floatAndDoubleCountVariableMap,
                optionalIncompatibleCountVariable, termFactory);

        return AggregationSimplification.create(
                termFactory.getFunctionalTermDecomposition(liftableTerm, substitutionMap),
                pushDownRequests);
    }

    private ImmutableSet<DefinitionPushDownRequest> computeRequests(
            ImmutableTerm subTermLexicalTerm, ImmutableTerm subTermTypeTerm, ImmutableSet<RDFTermType> nonNumericTypes,
            Optional<Variable> optionalNumSubVariable, Optional<Variable> optionalIncompatibleSubVariable,
            ImmutableMap<ConcreteNumericRDFDatatype, Variable> floatAndDoubleNumVariableMap,
            VariableNullability variableNullability, TermFactory termFactory) {

        Optional<DefinitionPushDownRequest> numericRequest = optionalNumSubVariable
                .map(v -> createNumericForFloatingAggregateRequest(v, subTermLexicalTerm, subTermTypeTerm, variableNullability, termFactory));

        Optional<DefinitionPushDownRequest> incompatibleTypeRequest = optionalIncompatibleSubVariable
                .map(v -> createNonNumericRequest(subTermTypeTerm, v, nonNumericTypes, termFactory));

        // NB: to be used for count purposes
        Stream<DefinitionPushDownRequest> floatAndDoubleRequestStream = floatAndDoubleNumVariableMap.entrySet().stream()
                .map(e -> createNumericForCountRequest(e.getValue(), e.getKey(), subTermLexicalTerm, subTermTypeTerm,
                        variableNullability, termFactory));

        return Stream.concat(
                floatAndDoubleRequestStream,
                Stream.of(numericRequest, incompatibleTypeRequest)
                        .filter(Optional::isPresent)
                        .map(Optional::get))
                .collect(ImmutableCollectors.toSet());
    }

    private DefinitionPushDownRequest createNumericForFloatingAggregateRequest(Variable numericVariable,
                                                                               ImmutableTerm subTermLexicalTerm,
                                                                               ImmutableTerm subTermTypeTerm,
                                                                               VariableNullability variableNullability,
                                                                               TermFactory termFactory) {
        TypeFactory typeFactory = termFactory.getTypeFactory();

        ImmutableTerm decimalDefinition = termFactory.getConversionFromRDFLexical2DB(
                    subTermLexicalTerm,
                    typeFactory.getXsdDecimalDatatype())
                .simplify(variableNullability);

        ImmutableExpression condition = termFactory.getIsAExpression(subTermTypeTerm,
                typeFactory.getAbstractOntopNumericDatatype());

        return DefinitionPushDownRequest.create(numericVariable, decimalDefinition, condition);
    }

    private DefinitionPushDownRequest createNumericForCountRequest(Variable numericVariable,
                                                                  ConcreteNumericRDFDatatype numericType,
                                                                  ImmutableTerm subTermLexicalTerm,
                                                                  ImmutableTerm subTermTypeTerm,
                                                                  VariableNullability variableNullability,
                                                                  TermFactory termFactory) {
        ImmutableTerm decimalDefinition = termFactory.getConversionFromRDFLexical2DB(
                subTermLexicalTerm,
                numericType)
                .simplify(variableNullability);

        ImmutableExpression condition = termFactory.getIsAExpression(subTermTypeTerm, numericType);

        return DefinitionPushDownRequest.create(numericVariable, decimalDefinition, condition);
    }

    private ImmutableMap<Variable, ImmutableFunctionalTerm> computeSubstitutionMap(Optional<Variable> optionalNumAvgVariable,
                                                                                   Optional<Variable> optionalNumSubVariable,
                                                                                   Optional<Variable> optionalIncompatibleCountVariable,
                                                                                   Optional<Variable> optionalIncompatibleSubVariable,
                                                                                   ImmutableMap<ConcreteNumericRDFDatatype, Variable> floatAndDoubleNumVariableMap,
                                                                                   ImmutableMap<ConcreteNumericRDFDatatype, Variable> floatAndDoubleCountVariableMap,
                                                                                   TermFactory termFactory) {

        ConcreteNumericRDFDatatype xsdDecimal = termFactory.getTypeFactory().getXsdDecimalDatatype();
        
        Optional<Map.Entry<Variable, ImmutableFunctionalTerm>> avgEntryStream = optionalNumAvgVariable
                .map(v -> Maps.immutableEntry(v, createAggregate(xsdDecimal, optionalNumSubVariable.get(), termFactory)));

        /*
         * Creates a COUNT for the incompatible types
         */
        Optional<Map.Entry<Variable, ImmutableFunctionalTerm>> incompatibleEntry = optionalIncompatibleCountVariable
                .map(v -> Maps.immutableEntry(v, termFactory.getDBCount(optionalIncompatibleSubVariable.get(), false)));

        /*
         * Creates COUNTs for float and doubles
         */
        Stream<Map.Entry<Variable, ImmutableFunctionalTerm>> floatDoubleCountStream = floatAndDoubleCountVariableMap.entrySet().stream()
                .map(e -> Maps.immutableEntry(
                        e.getValue(),
                        termFactory.getDBCount(floatAndDoubleNumVariableMap.get(e.getKey()), false)));

        return Stream.concat(
                floatDoubleCountStream,
                Stream.of(avgEntryStream, incompatibleEntry)
                        .filter(Optional::isPresent)
                        .map(Optional::get))
                .collect(ImmutableCollectors.toMap());
    }

    private ImmutableFunctionalTerm computeLiftableTerm(Optional<Variable> optionalNumAvgVariable,
                                                        ImmutableMap<ConcreteNumericRDFDatatype, Variable> floatAndDoubleCountVariableMap,
                                                        Optional<Variable> optionalIncompatibleCountVariable,
                                                        TermFactory termFactory) {

        Optional<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> incompatibleWhenPair = optionalIncompatibleCountVariable
                .map(v -> termFactory.getDBNumericInequality(InequalityLabel.GT, v, termFactory.getDBIntegerConstant(0)))
                .map(c -> Maps.immutableEntry(c, termFactory.getNullConstant()));

        ImmutableTerm lexicalTerm = computeLexicalTerm(optionalNumAvgVariable, incompatibleWhenPair, termFactory);
        ImmutableTerm typeTerm = computeTypeTerm(optionalNumAvgVariable, floatAndDoubleCountVariableMap,
                incompatibleWhenPair, termFactory);

        return termFactory.getRDFFunctionalTerm(lexicalTerm, typeTerm);

    }

    private ImmutableTerm computeLexicalTerm(Optional<Variable> optionalFloatingAggVariable,
                                             Optional<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> incompatibleWhenPair,
                                             TermFactory termFactory) {
        ConcreteNumericRDFDatatype xsdDecimal = termFactory.getTypeFactory().getXsdDecimalDatatype();

        Stream<? extends Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> whenPairs = Stream.of(
                // Check if there are some non-numeric values and return if it is  the case
                incompatibleWhenPair,
                // Otherwise, if there is a non-null aggregated value, returns its string representation
                optionalFloatingAggVariable
                        .map(v -> Maps.immutableEntry(
                                termFactory.getDBIsNotNull(v),
                                termFactory.getConversion2RDFLexical(v, xsdDecimal))))
                .filter(Optional::isPresent)
                .map(Optional::get);

        return termFactory.getDBCase(whenPairs,
                // If there is no numeric value: returns 0
                termFactory.getDBStringConstant("0"), true);

    }

    private ImmutableTerm computeTypeTerm(Optional<Variable> optionalNumAvgVariable,
                                          ImmutableMap<ConcreteNumericRDFDatatype, Variable> floatAndDoubleCountVariableMap,
                                          Optional<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> incompatibleWhenPair,
                                          TermFactory termFactory) {
        // xsd:double first, xsd:float last
        //noinspection ComparatorMethodParameterNotUsed
        ImmutableList<ConcreteNumericRDFDatatype> orderedTypes = floatAndDoubleCountVariableMap.keySet().stream()
                .sorted((t1, t2) -> t1.getCommonPropagatedOrSubstitutedType(t2).equals(t2) ? 1 : -1)
                .collect(ImmutableCollectors.toList());

        Stream<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> floatDoubleWhenPairs = orderedTypes.stream()
                .map(t -> Maps.immutableEntry(
                        termFactory.getDBNumericInequality(InequalityLabel.GT, floatAndDoubleCountVariableMap.get(t),
                                termFactory.getDBIntegerConstant(0)),
                        termFactory.getRDFTermTypeConstant(t)));

        // XSD.DECIMAL in case of the absence of floats and doubles
        Optional<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> decimalEntry = optionalNumAvgVariable
                .map(v -> Maps.immutableEntry(
                        termFactory.getDBIsNotNull(v),
                        termFactory.getRDFTermTypeConstant(termFactory.getTypeFactory().getXsdDecimalDatatype())));

        Stream<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> whenPairs = Stream.concat(
                Stream.concat(
                        // First: presence of non-numeric values
                        incompatibleWhenPair
                                .map(Stream::of)
                                .orElseGet(Stream::empty),
                        // Second: presence of double
                        // and Third: presence of float
                        floatDoubleWhenPairs),
                // Fourth: presence of decimal or integer
                decimalEntry
                        .map(Stream::of)
                        .orElseGet(Stream::empty));

        return termFactory.getDBCase(
                whenPairs,
                // Default: no value -> XSD.INTEGER
                termFactory.getRDFTermTypeConstant(termFactory.getTypeFactory().getXsdIntegerDatatype()), true);
    }


    @Override
    public Constant evaluateEmptyBag(TermFactory termFactory) {
        return termFactory.getRDFLiteralConstant("0", XSD.INTEGER);
    }
}
