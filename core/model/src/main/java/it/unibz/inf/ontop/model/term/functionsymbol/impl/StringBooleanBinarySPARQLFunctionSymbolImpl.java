package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class StringBooleanBinarySPARQLFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdBooleanType;

    protected StringBooleanBinarySPARQLFunctionSymbolImpl(@Nonnull String functionSymbolName, @Nonnull IRI functionIRI,
                                                          RDFDatatype xsdStringType, RDFDatatype xsdBooleanType) {
        super(functionSymbolName, functionIRI, ImmutableList.of(xsdStringType, xsdStringType));
        this.xsdBooleanType = xsdBooleanType;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(xsdBooleanType));
    }

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        return termFactory.getRDFTermTypeConstant(xsdBooleanType);
    }

    @Override
    protected ImmutableExpression.Evaluation evaluateInputTypeError(ImmutableList<ImmutableTerm> typeTerms,
                                                                    TermFactory termFactory) {
        return termFactory.getAreCompatibleRDFStringExpression(typeTerms.get(0), typeTerms.get(1))
                .evaluate(termFactory);
    }

    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return false;
    }
}
