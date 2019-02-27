package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;

public class NotSPARQLFunctionSymbolImpl extends AbstractUnaryBooleanSPARQLFunctionSymbol {

    private final RDFDatatype xsdBooleanType;

    protected NotSPARQLFunctionSymbolImpl(RDFDatatype xsdBooleanType) {
        super("SP_NOT", XPathFunction.NOT, xsdBooleanType, xsdBooleanType);
        this.xsdBooleanType = xsdBooleanType;
    }

    @Override
    protected ImmutableExpression computeExpression(ImmutableList<ImmutableTerm> subLexicalTerms, TermFactory termFactory) {
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();

        return termFactory.getDBNot(
                (ImmutableExpression) termFactory.getConversionFromRDFLexical2DB(
                        dbTypeFactory.getDBBooleanType(),
                        subLexicalTerms.get(0),
                        xsdBooleanType));
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return true;
    }
}
