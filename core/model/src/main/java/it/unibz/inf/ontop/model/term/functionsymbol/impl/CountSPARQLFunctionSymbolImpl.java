package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

import java.util.Optional;

public class CountSPARQLFunctionSymbolImpl extends SPARQLFunctionSymbolImpl{

    private final RDFDatatype xsdIntegerType;
    private final boolean isDistinct;

    protected CountSPARQLFunctionSymbolImpl(RDFTermType rootRdfTermType, RDFDatatype xsdIntegerType, boolean isDistinct) {
        super("SP_COUNT_1", SPARQL.COUNT, ImmutableList.of(rootRdfTermType));
        this.xsdIntegerType = xsdIntegerType;
        this.isDistinct = isDistinct;
    }

    protected CountSPARQLFunctionSymbolImpl(RDFDatatype xsdIntegerType, boolean isDistinct) {
        super("SP_COUNT_0", SPARQL.COUNT, ImmutableList.of());
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

        ImmutableFunctionalTerm dbCountTerm = (getArity() == 0)
                ? termFactory.getDBCount(isDistinct)
                : termFactory.getDBCount(newTerms.get(0), isDistinct);

        return termFactory.getRDFLiteralFunctionalTerm(termFactory.getConversion2RDFLexical(dbCountTerm, xsdIntegerType),
                xsdIntegerType);
    }

}
