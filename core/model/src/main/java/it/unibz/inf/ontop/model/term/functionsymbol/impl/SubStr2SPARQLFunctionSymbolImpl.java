package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;


public class SubStr2SPARQLFunctionSymbolImpl extends AbstractSubStrSPARQLFunctionSymbol {

    protected SubStr2SPARQLFunctionSymbolImpl(RDFDatatype xsdStringDatatype, RDFDatatype xsdIntegerDatatype) {
        super("SUBSTR2", XPathFunction.SUBSTRING, xsdStringDatatype,
                ImmutableList.of(xsdStringDatatype, xsdIntegerDatatype));
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms, TermFactory termFactory) {
        return termFactory.getDBSubString2(subLexicalTerms.get(0), subLexicalTerms.get(1));
    }
}
