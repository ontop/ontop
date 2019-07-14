package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLAggregationFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class MinOrMaxSPARQLFunctionSymbolImpl extends SPARQLFunctionSymbolImpl
        implements SPARQLAggregationFunctionSymbol {

    private final TypeFactory typeFactory;
    private final boolean isMax;
    private final RDFDatatype abstractNumericType;
    private final RDFDatatype dateTimeType;
    private final String defaultAggVariableName;

    protected MinOrMaxSPARQLFunctionSymbolImpl(TypeFactory typeFactory, boolean isMax) {
        super(isMax ? "SP_MAX" : "SP_MIN", isMax ? SPARQL.MAX : SPARQL.MIN,
                ImmutableList.of(typeFactory.getAbstractRDFTermType()));
        this.typeFactory = typeFactory;
        this.isMax = isMax;
        this.abstractNumericType = typeFactory.getAbstractOntopNumericDatatype();
        this.dateTimeType = typeFactory.getXsdDatetimeDatatype();
        this.defaultAggVariableName = isMax ? "max1" : "min1";
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
        throw new RuntimeException("TODO: support the multityped case of MIN and MAX");
    }

    enum Sort {
        NUMERIC,
        DATETIME,
        LEXICAL
    }
}
