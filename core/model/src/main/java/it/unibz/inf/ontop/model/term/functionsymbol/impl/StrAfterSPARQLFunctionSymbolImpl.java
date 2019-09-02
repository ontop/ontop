package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;


public class StrAfterSPARQLFunctionSymbolImpl extends AbstractStrBeforeOrAfterSPARQLFunctionSymbol {
    protected StrAfterSPARQLFunctionSymbolImpl(RDFDatatype xsdStringType) {
        super("SP_STRAFTER", XPathFunction.SUBSTRING_AFTER, xsdStringType);
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, ImmutableTerm returnedTypeTerm) {
        return computeLexicalTermWhenSecondArgIsNotEmpty(subLexicalTerms, termFactory);
    }

    @Override
    protected ImmutableTerm computeLexicalTermWhenSecondArgIsNotEmpty(ImmutableList<ImmutableTerm> subLexicalTerms,
                                                                      TermFactory termFactory) {
        return termFactory.getDBStrAfter(subLexicalTerms.get(0), subLexicalTerms.get(1));
    }
}
