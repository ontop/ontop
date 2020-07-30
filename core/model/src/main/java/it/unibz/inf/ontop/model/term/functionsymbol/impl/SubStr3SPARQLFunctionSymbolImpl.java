package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;


public class SubStr3SPARQLFunctionSymbolImpl extends AbstractSubStrSPARQLFunctionSymbol {

    protected SubStr3SPARQLFunctionSymbolImpl(RDFDatatype xsdStringDatatype, RDFDatatype xsdIntegerDatatype) {
        super("SP_SUBSTR3", XPathFunction.SUBSTRING, xsdStringDatatype,
                ImmutableList.of(xsdStringDatatype, xsdIntegerDatatype, xsdIntegerDatatype));
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, ImmutableTerm returnedTypeTerm) {
        RDFDatatype xsdInteger = termFactory.getTypeFactory().getXsdIntegerDatatype();
        return termFactory.getDBSubString3(subLexicalTerms.get(0),
                termFactory.getConversionFromRDFLexical2DB(subLexicalTerms.get(1), xsdInteger),
                termFactory.getConversionFromRDFLexical2DB(subLexicalTerms.get(2), xsdInteger));
    }
}
