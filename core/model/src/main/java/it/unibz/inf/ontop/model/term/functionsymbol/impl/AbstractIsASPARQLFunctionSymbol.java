package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.*;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class AbstractIsASPARQLFunctionSymbol extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final RDFTermType baseRDFType;
    private final RDFDatatype xsdBooleanType;

    protected AbstractIsASPARQLFunctionSymbol(@Nonnull String functionSymbolName, @Nonnull String officialName,
                                              RDFTermType baseRDFType, RDFTermType rootRDFType, RDFDatatype xsdBooleanType) {
        super(functionSymbolName, officialName, ImmutableList.of(rootRDFType));
        this.baseRDFType = baseRDFType;
        this.xsdBooleanType = xsdBooleanType;
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, ImmutableTerm returnedTypeTerm) {

        ImmutableExpression expression = termFactory.getIsAExpression(typeTerms.get(0), baseRDFType);
        return termFactory.getConversion2RDFLexical(expression, xsdBooleanType)
                .simplify();
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        return termFactory.getRDFTermTypeConstant(xsdBooleanType);
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(xsdBooleanType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
