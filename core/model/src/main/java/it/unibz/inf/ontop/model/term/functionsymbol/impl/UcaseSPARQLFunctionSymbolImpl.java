package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;

import javax.annotation.Nonnull;

public class UcaseSPARQLFunctionSymbolImpl extends AbstractUnaryStringSPARQLFunctionSymbol {
    protected UcaseSPARQLFunctionSymbolImpl(RDF rdfFactory, RDFDatatype xsdStringDatatype) {
        super("SP_UCASE", rdfFactory.createIRI("http://www.w3.org/2005/xpath-functions#upper-case"),
                xsdStringDatatype);
    }

    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return false;
    }

    @Override
    public boolean canBePostProcessed() {
        return false;
    }
}
