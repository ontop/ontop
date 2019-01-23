package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

public class Md5SPARQLFunctionSymbolImpl extends AbstractHashSPARQLFunctionSymbol {

    protected Md5SPARQLFunctionSymbolImpl(RDFDatatype xsdStringType) {
        super(SPARQL.MD5, xsdStringType);
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, ImmutableTerm returnedTypeTerm) {
        return termFactory.getDBMd5(subLexicalTerms.get(0));
    }
}
