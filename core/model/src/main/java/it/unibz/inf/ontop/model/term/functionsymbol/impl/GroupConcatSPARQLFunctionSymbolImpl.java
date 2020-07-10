package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.request.DefinitionPushDownRequest;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLAggregationFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

public class GroupConcatSPARQLFunctionSymbolImpl extends SPARQLFunctionSymbolImpl implements SPARQLAggregationFunctionSymbol {

    private final RDFDatatype xsdStringType;
    private final String separator;
    private final boolean isDistinct;

    protected GroupConcatSPARQLFunctionSymbolImpl(RDFDatatype xsdStringType, String separator, boolean isDistinct) {
        super(
                "SP_GROUP_CONCAT" + (isDistinct ? "_DISTINCT" : "") + (separator.equals(" ") ? "": separator),
                SPARQL.GROUP_CONCAT,
                ImmutableList.of(xsdStringType)
        );
        this.xsdStringType = xsdStringType;
        this.separator = separator;
        this.isDistinct = isDistinct;
    }

    public boolean isDistinct() {
        return isDistinct;
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(xsdStringType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public boolean isNullable(ImmutableSet<Integer> nullableIndexes) {
        return true;
    }

    @Override
    public boolean isAggregation() {
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

        ImmutableTerm firstSubTerm = subTerms.get(0);
        ImmutableSet<RDFTermType> subTermPossibleTypes = possibleRDFTypes.get(0);

        return subTermPossibleTypes.stream().allMatch(t -> t.isA(xsdStringType))
                ? Optional.of(decomposeString(firstSubTerm, hasGroupBy, variableNullability,
                    variableGenerator, termFactory))
                : Optional.of(decomposeWithNonString(firstSubTerm, variableNullability,
                    variableGenerator, termFactory));
    }

    private AggregationSimplification decomposeString(ImmutableTerm firstSubTerm, boolean hasGroupBy,
                                                      VariableNullability variableNullability,
                                                      VariableGenerator variableGenerator, TermFactory termFactory) {
        ImmutableTerm subTermLexicalTerm = extractLexicalTerm(firstSubTerm, termFactory);

        ImmutableFunctionalTerm dbAggTerm = createAggregate(subTermLexicalTerm, separator, termFactory);

        Variable dbAggregationVariable = variableGenerator.generateNewVariable("str");

        boolean isSubTermNullable = subTermLexicalTerm.isNullable(variableNullability.getNullableVariables());
        DBConstant emptyString = termFactory.getDBStringConstant("");

        // If DB group concat returns a NULL, replaces it by ""
        boolean dbAggMayReturnNull = !(hasGroupBy && (!isSubTermNullable));
        ImmutableTerm nonNullDBAggregate = dbAggMayReturnNull
                ? termFactory.getDBCoalesce(dbAggregationVariable, emptyString)
                : dbAggregationVariable;

        ImmutableFunctionalTerm liftedTerm = termFactory.getRDFFunctionalTerm(
                nonNullDBAggregate,
                termFactory.getRDFTermTypeConstant(xsdStringType));

        ImmutableFunctionalTerm.FunctionalTermDecomposition decomposition = termFactory.getFunctionalTermDecomposition(
                liftedTerm,
                ImmutableMap.of(dbAggregationVariable, dbAggTerm));

        return AggregationSimplification.create(decomposition);
    }

    private ImmutableFunctionalTerm createAggregate(ImmutableTerm subTermLexicalTerm, String separator, TermFactory termFactory) {
        return termFactory.getDBGroupConcat(subTermLexicalTerm, separator, isDistinct);
    }

    private AggregationSimplification decomposeWithNonString(ImmutableTerm subTerm, VariableNullability variableNullability,
                                                             VariableGenerator variableGenerator, TermFactory termFactory) {

        ImmutableTerm subTermLexicalTerm = extractLexicalTerm(subTerm, termFactory);
        ImmutableTerm subTermTypeTerm = extractRDFTermTypeTerm(subTerm, termFactory);
        /*
         * String sub-variable
         */
        Variable stringSubVar = variableGenerator.generateNewVariable("s");
        Variable incompatibleSubVariable = variableGenerator.generateNewVariable("nonStr");

        /*
         * Requests to make sure that the sub-tree with provide these novel numeric and non-numeric variables
         */
        ImmutableSet<DefinitionPushDownRequest> pushDownRequests = computeRequests(subTermLexicalTerm, subTermTypeTerm, stringSubVar,
                incompatibleSubVariable, variableNullability, termFactory);

        Variable strAggregateVar = variableGenerator.generateNewVariable("agg");
        Variable incompatibleCountVariable = variableGenerator.generateNewVariable("nonStrCount");

        ImmutableMap<Variable, ImmutableFunctionalTerm> substitutionMap = computeSubstitutionMap(
                strAggregateVar, stringSubVar, incompatibleCountVariable, incompatibleSubVariable, termFactory, separator);

        ImmutableFunctionalTerm liftableTerm = computeLiftableTerm(strAggregateVar, incompatibleCountVariable,
                termFactory);

        return AggregationSimplification.create(
                termFactory.getFunctionalTermDecomposition(liftableTerm, substitutionMap),
                pushDownRequests);
    }

    private ImmutableSet<DefinitionPushDownRequest> computeRequests(
            ImmutableTerm subTermLexicalTerm, ImmutableTerm subTermTypeTerm,
            Variable strSubTermVar,
            Variable incompatibleVariable,
            VariableNullability variableNullability, TermFactory termFactory) {

        return ImmutableSet.of(
                createStrRequest(strSubTermVar, subTermLexicalTerm, subTermTypeTerm, variableNullability, termFactory),
                createNonStrRequest(subTermTypeTerm, incompatibleVariable, termFactory));
    }

    private DefinitionPushDownRequest createStrRequest(Variable strVariable,
                                                       ImmutableTerm subTermLexicalTerm,
                                                       ImmutableTerm subTermTypeTerm,
                                                       VariableNullability variableNullability,
                                                       TermFactory termFactory) {
        ImmutableTerm definition = subTermLexicalTerm.simplify(variableNullability);
        ImmutableExpression condition = termFactory.getIsAExpression(subTermTypeTerm, xsdStringType);

        return DefinitionPushDownRequest.create(strVariable, definition, condition);
    }

    protected DefinitionPushDownRequest createNonStrRequest(ImmutableTerm subTermTypeTerm, Variable nonStrVariable,
                                                            TermFactory termFactory) {
        ImmutableTerm definition = termFactory.getDBBooleanConstant(true);
        ImmutableExpression condition = termFactory.getDBNot(termFactory.getIsAExpression(subTermTypeTerm, xsdStringType));

        return DefinitionPushDownRequest.create(nonStrVariable, definition, condition);
    }

    private ImmutableMap<Variable, ImmutableFunctionalTerm> computeSubstitutionMap(
            Variable strAggregateVar, Variable strSubTermVar, Variable incompatibleCountVariable,
            Variable incompatibleSubVariable, TermFactory termFactory, String separator) {

        return ImmutableMap.of(
                strAggregateVar, createAggregate(strSubTermVar, separator, termFactory),
                incompatibleCountVariable, termFactory.getDBCount(incompatibleSubVariable, false));
    }

    private ImmutableFunctionalTerm computeLiftableTerm(Variable strAggregateVar, Variable incompatibleCountVariable,
                                                        TermFactory termFactory) {
        DBConstant zero = termFactory.getDBIntegerConstant(0);
        ImmutableExpression incompatibleCondition = termFactory.getDBNumericInequality(InequalityLabel.GT,
                incompatibleCountVariable, zero);

        ImmutableFunctionalTerm lexicalTerm = termFactory.getDBCase(
                Stream.of(
                        // Checks the incompatibility first
                        Maps.immutableEntry(incompatibleCondition, termFactory.getNullConstant()),
                        // Then the group_concat value
                        Maps.immutableEntry(termFactory.getDBIsNotNull(strAggregateVar), strAggregateVar)),
                termFactory.getDBStringConstant(""), true);

        ImmutableFunctionalTerm typeTerm = termFactory.getIfElseNull(
                incompatibleCondition.negate(termFactory),
                termFactory.getRDFTermTypeConstant(xsdStringType));

        return termFactory.getRDFFunctionalTerm(lexicalTerm, typeTerm);
    }

    @Override
    public Constant evaluateEmptyBag(TermFactory termFactory) {
        return termFactory.getRDFLiteralConstant("", xsdStringType);
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableTerm newTerm = newTerms.get(0);
        return newTerm.isNull()
                ? evaluateEmptyBag(termFactory)
                : super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }
}
