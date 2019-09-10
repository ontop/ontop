package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLAggregationFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class CountSPARQLFunctionSymbolImpl extends SPARQLFunctionSymbolImpl implements SPARQLAggregationFunctionSymbol {

    private final RDFDatatype xsdIntegerType;
    private final boolean isDistinct;

    protected CountSPARQLFunctionSymbolImpl(RDFTermType rootRdfTermType, RDFDatatype xsdIntegerType, boolean isDistinct) {
        super("SP_COUNT_1" + (isDistinct ? "_DISTINCT" : ""), SPARQL.COUNT, ImmutableList.of(rootRdfTermType));
        this.xsdIntegerType = xsdIntegerType;
        this.isDistinct = isDistinct;
    }

    protected CountSPARQLFunctionSymbolImpl(RDFDatatype xsdIntegerType, boolean isDistinct) {
        super("SP_COUNT_0" + (isDistinct ? "_DISTINCT" : ""), SPARQL.COUNT, ImmutableList.of());
        this.xsdIntegerType = xsdIntegerType;
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

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(xsdIntegerType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public boolean isNullable(ImmutableSet<Integer> nullableIndexes) {
        return false;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        if (getArity() == 0)
            return termFactory.getRDFLiteralFunctionalTerm(
                    termFactory.getConversion2RDFLexical(
                            termFactory.getDBCount(isDistinct),
                            xsdIntegerType),
                    xsdIntegerType);

        ImmutableTerm newTerm = newTerms.get(0);
        if (isRDFFunctionalTerm(newTerm) || (newTerm instanceof RDFConstant)) {
            ImmutableTerm lexicalTerm = extractLexicalTerm(newTerm, termFactory);
            ImmutableFunctionalTerm dbCountTerm = termFactory.getDBCount(lexicalTerm, isDistinct);

            return termFactory.getRDFLiteralFunctionalTerm(
                    termFactory.getConversion2RDFLexical(dbCountTerm, xsdIntegerType), xsdIntegerType)
                    .simplify(variableNullability);
        }
        else if (newTerm.isNull()) {
            return termFactory.getRDFLiteralConstant("0", XSD.INTEGER);
        }

        return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    @Override
    public boolean isAggregation() {
        return true;
    }

    /**
     * Simplifies itself without needing the call of this method.
     */
    @Override
    public Optional<AggregationSimplification> decomposeIntoDBAggregation(
            ImmutableList<? extends ImmutableTerm> subTerms, ImmutableList<ImmutableSet<RDFTermType>> possibleRDFTypes,
            boolean hasGroupBy, VariableNullability variableNullability, VariableGenerator variableGenerator, TermFactory termFactory) {
        return Optional.empty();
    }

    @Override
    public Constant evaluateEmptyBag(TermFactory termFactory) {
        return termFactory.getRDFLiteralConstant("0", XSD.INTEGER);
    }
}
