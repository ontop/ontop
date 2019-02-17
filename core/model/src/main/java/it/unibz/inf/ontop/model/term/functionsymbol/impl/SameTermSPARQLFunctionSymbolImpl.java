package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

import javax.annotation.Nonnull;

public class SameTermSPARQLFunctionSymbolImpl extends AbstractBinaryComparisonSPARQLFunctionSymbol {

    protected SameTermSPARQLFunctionSymbolImpl(@Nonnull RDFTermType rdfRootType, RDFDatatype xsdBooleanType) {

        super("SP_SAME_TERM", SPARQL.SAME_TERM, rdfRootType, xsdBooleanType);
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        return termFactory.getConjunction(
                termFactory.getStrictEquality(subLexicalTerms),
                termFactory.getStrictEquality(typeTerms))
                .simplify();
    }
}
