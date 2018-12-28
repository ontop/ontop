package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;

public class StartsWithSPARQLFunctionSymbolImpl extends StringBooleanBinarySPARQLFunctionSymbolImpl {

    protected StartsWithSPARQLFunctionSymbolImpl(RDFDatatype xsdStringType, RDFDatatype xsdBooleanType) {
        super("SP_STARTS_WITH", XPathFunction.STARTS_WITH, xsdStringType, xsdBooleanType);
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms, TermFactory termFactory) {
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();

        return termFactory.getDBCastFunctionalTerm(
                dbTypeFactory.getDBBooleanType(),
                dbTypeFactory.getDBStringType(),
                termFactory.getDBStartsWithFunctionalTerm(subLexicalTerms));
    }

    /**
     * Could be allowed in the future
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
