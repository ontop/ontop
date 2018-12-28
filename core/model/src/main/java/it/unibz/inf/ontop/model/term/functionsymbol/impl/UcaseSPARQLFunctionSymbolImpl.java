package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;

public class UcaseSPARQLFunctionSymbolImpl extends AbstractUnaryStringSPARQLFunctionSymbol {
    private final DBFunctionSymbol dbUcaseFunctionSymbol;

    protected UcaseSPARQLFunctionSymbolImpl(RDFDatatype xsdStringDatatype,
                                            DBFunctionSymbolFactory dbFunctionSymbolFactory) {
        super("SP_UCASE", XPathFunction.UPPER_CASE, xsdStringDatatype);
        this.dbUcaseFunctionSymbol = dbFunctionSymbolFactory.getDBUpper();
    }

    @Override
    protected boolean isAlwaysInjective() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms, TermFactory termFactory) {
        return termFactory.getImmutableFunctionalTerm(dbUcaseFunctionSymbol, subLexicalTerms);
    }
}
