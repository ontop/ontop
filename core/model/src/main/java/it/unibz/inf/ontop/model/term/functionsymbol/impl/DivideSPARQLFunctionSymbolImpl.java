package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

public class DivideSPARQLFunctionSymbolImpl extends AbstractNumericBinarySPARQLFunctionSymbol {

    private final RDFDatatype xsdDecimalType;

    protected DivideSPARQLFunctionSymbolImpl(RDFDatatype abstractNumericType, RDFDatatype xsdDecimalType) {
        super("SP_DIVIDE", SPARQL.NUMERIC_DIVIDE, abstractNumericType);
        this.xsdDecimalType = xsdDecimalType;
    }

    @Override
    protected ImmutableTerm computeNumericTerm(ImmutableFunctionalTerm numericTerm1, ImmutableFunctionalTerm numericTerm2,
                                               TermFactory termFactory) {
        throw new RuntimeException("TODO: implement it");
    }

    /**
     * Division by zero returns a NULL (SPARQL error) if the operands are both xsd:integer or xsd:decimal
     * (but not if they are xsd:float and xsd:double)
     */
    @Override
    protected ImmutableExpression.Evaluation evaluateInputTypeError(ImmutableList<ImmutableTerm> typeTerms,
                                                                    TermFactory termFactory,
                                                                    VariableNullability variableNullability) {
        throw new RuntimeException("TODO: implement evaluateInputTypeError for the DIVIDE operator");
    }

    /**
     * Makes sure that it returns a xsd:decimal if both operands are xsd:integer
     */
    @Override
    protected ImmutableTerm extractRDFTermTypeTerm(ImmutableTerm rdfTerm, TermFactory termFactory) {
        return termFactory.getCommonDenominatorFunctionalTerm(ImmutableList.of(
                super.extractRDFTermTypeTerm(rdfTerm, termFactory),
                termFactory.getRDFTermTypeConstant(xsdDecimalType)));
    }
}
