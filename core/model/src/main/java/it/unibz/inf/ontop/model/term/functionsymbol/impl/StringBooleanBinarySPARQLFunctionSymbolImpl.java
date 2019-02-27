package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
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

    protected StringBooleanBinarySPARQLFunctionSymbolImpl(@Nonnull String functionSymbolName, @Nonnull String officialName,
                                                          RDFDatatype xsdStringType, RDFDatatype xsdBooleanType) {
        super(functionSymbolName, officialName, ImmutableList.of(xsdStringType, xsdStringType));
        this.xsdBooleanType = xsdBooleanType;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(xsdBooleanType));
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, ImmutableTerm returnedTypeTerm) {
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();

        return termFactory.getConversion2RDFLexical(
                dbTypeFactory.getDBBooleanType(),
                computeDBBooleanTerm(subLexicalTerms, typeTerms, termFactory),
                xsdBooleanType);
    }

    protected abstract ImmutableTerm computeDBBooleanTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                                          ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory);

    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, VariableNullability variableNullability) {
        return termFactory.getRDFTermTypeConstant(xsdBooleanType);
    }

    @Override
    protected ImmutableExpression.Evaluation evaluateInputTypeError(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms,
                                                                    TermFactory termFactory, VariableNullability variableNullability) {
        return termFactory.getAreCompatibleRDFStringExpression(typeTerms.get(0), typeTerms.get(1))
                .evaluate(variableNullability);
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }
}
