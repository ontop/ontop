package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.*;

import java.util.Optional;

public class BinaryArithmeticSPARQLFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {
    private final TypeFactory typeFactory;
    private final MetaRDFTermType metaRDFTermType;
    private final DBTermType dbStringType;

    protected BinaryArithmeticSPARQLFunctionSymbolImpl(String functionSymbolName, String officialName, RDFDatatype abstractType, TypeFactory typeFactory) {
        super(functionSymbolName, officialName, ImmutableList.of(abstractType, abstractType));

        this.dbStringType = typeFactory.getDBTypeFactory().getDBStringType();
        this.metaRDFTermType = typeFactory.getMetaRDFTermType();
        this.typeFactory = typeFactory;
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms,
                                            ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        return termFactory.getImmutableFunctionalTerm(
                new BinaryArithmeticTermTypeFunctionSymbolImpl(getOfficialName(), dbStringType, metaRDFTermType, typeFactory),
                subLexicalTerms.get(0), subLexicalTerms.get(1), typeTerms.get(0), typeTerms.get(1));
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                               ImmutableTerm returnedRDFTypeTerm) {
        return termFactory.getImmutableFunctionalTerm(
                new BinaryArithmeticLexicalFunctionSymbolImpl(getOfficialName(), dbStringType, metaRDFTermType, typeFactory),
                subLexicalTerms.get(0), subLexicalTerms.get(1), typeTerms.get(0), typeTerms.get(1), returnedRDFTypeTerm);
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
