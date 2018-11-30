package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

import javax.annotation.Nonnull;

public class UcaseSPARQLFunctionSymbolImpl extends AbstractUnaryStringSPARQLFunctionSymbol {
    private final DBFunctionSymbol dbUcaseFunctionSymbol;

    protected UcaseSPARQLFunctionSymbolImpl(RDF rdfFactory, RDFDatatype xsdStringDatatype,
                                            BooleanFunctionSymbol isARDFFunctionSymbol,
                                            DBFunctionSymbolFactory dbFunctionSymbolFactory) {
        super("SP_UCASE", rdfFactory.createIRI("http://www.w3.org/2005/xpath-functions#upper-case"),
                xsdStringDatatype, isARDFFunctionSymbol);
        this.dbUcaseFunctionSymbol = dbFunctionSymbolFactory.getDBUCase();
    }

    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return false;
    }

    @Override
    public boolean canBePostProcessed() {
        return false;
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms, TermFactory termFactory) {
        return termFactory.getImmutableFunctionalTerm(dbUcaseFunctionSymbol, subLexicalTerms);
    }
}
