package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLAggregationFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.IntStream;

public class GroupConcatSPARQLFunctionSymbolImpl extends SPARQLFunctionSymbolImpl implements SPARQLAggregationFunctionSymbol {

    private final RDFDatatype xsdStringType;
    private final boolean isDistinct;

    protected GroupConcatSPARQLFunctionSymbolImpl(RDFDatatype xsdStringType, int arity, boolean isDistinct) {
        super(
                "SP_GROUP_CONCAT"+arity,
                SPARQL.GROUP_CONCAT,
                IntStream.range(0, arity).boxed()
                        .map(i -> xsdStringType)
                        .collect(ImmutableCollectors.toList())
        );
        this.xsdStringType = xsdStringType;
        this.isDistinct = isDistinct;
        if(arity < 1 || arity > 2){
            throw new RuntimeException("Arity 1 or 2 expected");
        }
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
    public Optional<AggregationSimplification> decomposeIntoDBAggregation(ImmutableList<? extends ImmutableTerm> subTerms,
                                                                          ImmutableList<ImmutableSet<RDFTermType>> possibleRDFTypes,
                                                                          boolean hasGroupBy, VariableNullability variableNullability,
                                                                          VariableGenerator variableGenerator, TermFactory termFactory) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public Constant evaluateEmptyBag(TermFactory termFactory) {
        return termFactory.getRDFLiteralConstant("", xsdStringType);
    }
}
