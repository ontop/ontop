package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

public class Sha256SPARQLFunctionSymbolImpl extends AbstractHashSPARQLFunctionSymbol {

    protected Sha256SPARQLFunctionSymbolImpl(RDFDatatype xsdStringType) {
        super(SPARQL.SHA256, xsdStringType);
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, ImmutableTerm returnedTypeTerm) {
        return termFactory.getDBSha256(subLexicalTerms.get(0));
    }
}
