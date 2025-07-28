package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;

public class BinaryArithmeticSPARQLFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {
    protected BinaryArithmeticSPARQLFunctionSymbolImpl(String functionSymbolName, String officialName, RDFDatatype abstractTemporalOrNumericType) {
        super(functionSymbolName, officialName,
                ImmutableList.of(abstractTemporalOrNumericType, abstractTemporalOrNumericType));
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms,
                                            ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        return termFactory.getBinaryArithmeticTypeFunctionalTerm(getOfficialName(),
                subLexicalTerms.get(0), subLexicalTerms.get(1),
                typeTerms.get(0), typeTerms.get(1));
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                               ImmutableTerm returnedRDFTypeTerm) {
        return termFactory.getBinaryArithmeticLexicalFunctionalTerm(getOfficialName(),
                subLexicalTerms.get(0), subLexicalTerms.get(1),
                typeTerms.get(0), typeTerms.get(1), returnedRDFTypeTerm);
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    /**
     * Too complex logic so not infer at this level (but after simplification into DB functional terms)
     */
    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.empty();
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
