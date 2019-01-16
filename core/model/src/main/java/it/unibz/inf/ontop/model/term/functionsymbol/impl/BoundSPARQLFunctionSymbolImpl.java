package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

public class BoundSPARQLFunctionSymbolImpl extends AbstractUnaryBooleanSPARQLFunctionSymbol {

    protected BoundSPARQLFunctionSymbolImpl(RDFTermType rootRDFType,
                                            RDFDatatype xsdBooleanType) {
        super("SP_BOUND", SPARQL.BOUND, rootRDFType, xsdBooleanType);
    }

    @Override
    protected ImmutableExpression computeExpression(ImmutableList<ImmutableTerm> subLexicalTerms, TermFactory termFactory) {
        return termFactory.getDBIsNotNull(subLexicalTerms.get(0));
    }

    @Override
    protected boolean isAlwaysInjective() {
        return false;
    }
}
