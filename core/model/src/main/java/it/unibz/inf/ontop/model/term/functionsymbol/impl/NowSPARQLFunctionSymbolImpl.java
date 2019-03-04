package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

import java.util.Optional;

public class NowSPARQLFunctionSymbolImpl extends SPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdDatetimeType;

    protected NowSPARQLFunctionSymbolImpl(RDFDatatype xsdDatetimeType) {
        super("SP_NOW", SPARQL.NOW, ImmutableList.of());
        this.xsdDatetimeType = xsdDatetimeType;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return true;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(xsdDatetimeType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {
        return termFactory.getRDFFunctionalTerm(
                termFactory.getConversion2RDFLexical(
                        termFactory.getDBNow(),
                        xsdDatetimeType),
                termFactory.getRDFTermTypeConstant(xsdDatetimeType));
    }
}
