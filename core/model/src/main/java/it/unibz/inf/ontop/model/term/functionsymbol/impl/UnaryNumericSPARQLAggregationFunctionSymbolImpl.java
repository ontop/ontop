package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.request.DefinitionPushDownRequest;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLAggregationFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public abstract class UnaryNumericSPARQLAggregationFunctionSymbolImpl extends SPARQLFunctionSymbolImpl
        implements SPARQLAggregationFunctionSymbol {

    private final boolean isDistinct;
    private final String defaultAggVariableName;

    public UnaryNumericSPARQLAggregationFunctionSymbolImpl(String name, String officialName, boolean isDistinct,
                                                           RDFTermType rootRdfTermType, String defaultAggVariableName) {
        super(name, officialName, ImmutableList.of(rootRdfTermType));
        this.isDistinct = isDistinct;
        this.defaultAggVariableName = defaultAggVariableName;
    }

    public boolean isDistinct() {
        return isDistinct;
    }

    @Override
    public boolean isAggregation() {
        return true;
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
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableTerm newTerm = newTerms.get(0);
        return newTerm.isNull()
                ? evaluateEmptyBag(termFactory)
                : super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
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

    protected AggregationSimplification decomposeUnityped(ImmutableTerm subTerm, RDFTermType subTermType,
                                                          boolean hasGroupBy, VariableNullability variableNullability,
                                                          VariableGenerator variableGenerator, TermFactory termFactory) {

        if (!(subTermType instanceof ConcreteNumericRDFDatatype)) {
            // Special case of the multityped case. Does not return a null when the group is empty (use a count)
            // TODO: consider the possibility to disable it through the settings (returning a simple NULL)
            return decomposeMultityped(subTerm, ImmutableSet.of(subTermType), hasGroupBy, variableNullability, variableGenerator, termFactory);
        }

        ConcreteNumericRDFDatatype inputNumericDatatype = (ConcreteNumericRDFDatatype) subTermType;
        ImmutableTerm subTermLexicalTerm = extractLexicalTerm(subTerm, termFactory);

        TypeFactory typeFactory = termFactory.getTypeFactory();
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();

        ImmutableFunctionalTerm dbAggTerm = createAggregate(
                inputNumericDatatype,
                termFactory.getConversionFromRDFLexical2DB(subTermLexicalTerm, inputNumericDatatype),
                termFactory);

        ConcreteNumericRDFDatatype inferredTypeWhenNonEmpty = inferTypeWhenNonEmpty(inputNumericDatatype, typeFactory);
        RDFTermTypeConstant inferredTypeTermWhenNonEmpty = termFactory.getRDFTermTypeConstant(inferredTypeWhenNonEmpty);

        Variable dbAggregationVariable = variableGenerator.generateNewVariable(defaultAggVariableName);

        boolean isSubTermNullable = subTermLexicalTerm.isNullable(variableNullability.getNullableVariables());
        DBConstant zero = termFactory.getDBConstant("0", dbTypeFactory.getDBLargeIntegerType());

        // If DB sum returns a NULL, replaces it by 0
        boolean dbAggMayReturnNull = !(hasGroupBy && (!isSubTermNullable));
        ImmutableTerm nonNullDBAggregate = dbAggMayReturnNull
                ? termFactory.getDBCoalesce(dbAggregationVariable, zero)
                : dbAggregationVariable;

        // TODO: consider the possibility to disable it through the settings
        ImmutableTerm inferredType = dbAggMayReturnNull
                ? termFactory.getIfThenElse(
                termFactory.getDBIsNotNull(dbAggregationVariable),
                inferredTypeTermWhenNonEmpty,
                termFactory.getRDFTermTypeConstant(typeFactory.getXsdIntegerDatatype()))
                : inferredTypeTermWhenNonEmpty;

        ImmutableFunctionalTerm liftedTerm = termFactory.getRDFFunctionalTerm(
                termFactory.getConversion2RDFLexical(nonNullDBAggregate, inferredTypeWhenNonEmpty),
                inferredType);

        ImmutableFunctionalTerm.FunctionalTermDecomposition decomposition = termFactory.getFunctionalTermDecomposition(
                liftedTerm,
                ImmutableMap.of(dbAggregationVariable, dbAggTerm));

        return AggregationSimplification.create(decomposition);
    }

    protected abstract ConcreteNumericRDFDatatype inferTypeWhenNonEmpty(ConcreteNumericRDFDatatype inputNumericDatatype, TypeFactory typeFactory);

    protected abstract AggregationSimplification decomposeMultityped(ImmutableTerm subTerm,
                                                                     ImmutableSet<RDFTermType> subTermPossibleTypes,
                                                                     boolean hasGroupBy, VariableNullability variableNullability,
                                                                     VariableGenerator variableGenerator,
                                                                     TermFactory termFactory);

    protected abstract ImmutableFunctionalTerm createAggregate(ConcreteNumericRDFDatatype rdfType, ImmutableTerm dbTerm,
                                                               TermFactory termFactory);

    protected DefinitionPushDownRequest createNonNumericRequest(ImmutableTerm subTermTypeTerm, Variable nonNumericVariable,
                                                              ImmutableSet<RDFTermType> nonNumericTypes,
                                                              TermFactory termFactory) {
        ImmutableTerm definition = termFactory.getDBBooleanConstant(true);

        ImmutableExpression condition = termFactory.getDisjunction(nonNumericTypes.stream()
                .map(t -> termFactory.getStrictEquality(subTermTypeTerm, termFactory.getRDFTermTypeConstant(t))))
                .orElseThrow(() -> new MinorOntopInternalBugException("At least one type was expected"));

        return DefinitionPushDownRequest.create(nonNumericVariable, definition, condition);
    }

    protected ImmutableSet<RDFTermType> extractNonNumericTypes(ImmutableSet<RDFTermType> subTermPossibleTypes) {
        return subTermPossibleTypes.stream()
                .filter(t -> !(t instanceof ConcreteNumericRDFDatatype))
                .collect(ImmutableCollectors.toSet());
    }
}
