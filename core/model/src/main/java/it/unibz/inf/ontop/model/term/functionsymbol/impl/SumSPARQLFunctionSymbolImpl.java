package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm.FunctionalTermDecomposition;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLAggregationFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

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
            VariableNullability variableNullability, VariableGenerator variableGenerator, TermFactory termFactory) {
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
                return decomposeUniTyped(subTerm, subTermPossibleTypes.iterator().next(), variableNullability,
                        variableGenerator, termFactory);
            default:
                return decomposeMultiTyped(subTerm, subTermPossibleTypes, termFactory);
        }


    }

    private Optional<AggregationSimplification> decomposeUniTyped(ImmutableTerm subTerm, RDFTermType subTermType,
                                                                  VariableNullability variableNullability,
                                                                  VariableGenerator variableGenerator, TermFactory termFactory) {
        if (!(subTermType instanceof ConcreteNumericRDFDatatype))
            // TODO: return a NULL
            throw new RuntimeException("TODO: return a null");

        ConcreteNumericRDFDatatype numericDatatype = (ConcreteNumericRDFDatatype) subTermType;
        ImmutableTerm subTermLexicalTerm = extractLexicalTerm(subTerm, termFactory);

        TypeFactory typeFactory = termFactory.getTypeFactory();
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();

        ImmutableFunctionalTerm dbSumTerm = termFactory.getNonNullRejectingDBSum(
                termFactory.getConversionFromRDFLexical2DB(subTermLexicalTerm, numericDatatype),
                numericDatatype.getClosestDBType(termFactory.getTypeFactory().getDBTypeFactory()),
                isDistinct);

        RDFTermTypeConstant inferredTypeTermWhenNonEmpty = termFactory.getRDFTermTypeConstant(
                numericDatatype.getCommonPropagatedOrSubstitutedType(numericDatatype));

        // TODO: consider the possibility to disable through the settings
        ImmutableTerm inferredType = subTermLexicalTerm.isNullable(variableNullability.getNullableVariables())
                ? termFactory.getIfThenElse(
                        termFactory.getStrictEquality(
                                termFactory.getDBCount(subTermLexicalTerm, false),
                                termFactory.getDBConstant("0", dbTypeFactory.getDBLargeIntegerType())),
                        termFactory.getRDFTermTypeConstant(typeFactory.getXsdIntegerDatatype()),
                        inferredTypeTermWhenNonEmpty)
                : inferredTypeTermWhenNonEmpty;

        Variable dbAggregationVariable = variableGenerator.generateNewVariable();

        ImmutableFunctionalTerm liftedTerm = termFactory.getRDFFunctionalTerm(
                termFactory.getConversion2RDFLexical(dbAggregationVariable, numericDatatype),
                inferredType);

        FunctionalTermDecomposition decomposition = termFactory.getFunctionalTermDecomposition(liftedTerm,
                ImmutableMap.of(dbAggregationVariable, dbSumTerm));

        return Optional.of(AggregationSimplification.create(decomposition, ImmutableSet.of()));
    }

    /**
     * TODO: support
     */
    private Optional<AggregationSimplification> decomposeMultiTyped(ImmutableTerm subTerm,
                                                                    ImmutableSet<RDFTermType> subTermPossibleTypes,
                                                                    TermFactory termFactory) {
        throw new RuntimeException("TODO: the multityped case for SUM is not yet supported");
    }

    @Override
    public boolean isAggregation() {
        return true;
    }
}
