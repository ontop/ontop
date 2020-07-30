package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.*;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

public abstract class AbstractUnaryBooleanSPARQLFunctionSymbol extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdBooleanType;

    protected AbstractUnaryBooleanSPARQLFunctionSymbol(String functionSymbolName, String officialName,
                                                       TermType inputBaseType, RDFDatatype xsdBooleanType) {
        super(functionSymbolName, officialName, ImmutableList.of(inputBaseType));
        this.xsdBooleanType = xsdBooleanType;
    }

    protected AbstractUnaryBooleanSPARQLFunctionSymbol(String functionSymbolName, IRI iri,
                                                       TermType inputBaseType, RDFDatatype xsdBooleanType) {
        super(functionSymbolName, iri, ImmutableList.of(inputBaseType));
        this.xsdBooleanType = xsdBooleanType;
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, ImmutableTerm returnedTypeTerm) {
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();

        ImmutableExpression expression = computeExpression(subLexicalTerms, termFactory);

        // TODO: call simplify()
        return termFactory.getConversion2RDFLexical(
                dbTypeFactory.getDBBooleanType(),
                expression,
                xsdBooleanType);
    }

    protected abstract ImmutableExpression computeExpression(ImmutableList<ImmutableTerm> subLexicalTerms,
                                                             TermFactory termFactory);

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms,
                                            ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        return termFactory.getRDFTermTypeConstant(xsdBooleanType);
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
