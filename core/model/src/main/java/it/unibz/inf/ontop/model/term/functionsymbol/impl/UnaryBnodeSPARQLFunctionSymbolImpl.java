package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;


public class UnaryBnodeSPARQLFunctionSymbolImpl extends AbstractBnodeSPARQLFunctionSymbol {

    protected UnaryBnodeSPARQLFunctionSymbolImpl(RDFDatatype xsdString, RDFTermType bnodeType) {
        super("SP_" + SPARQL.BNODE + "_1", ImmutableList.of(xsdString), bnodeType);
    }

    @Override
    protected ImmutableTerm buildLexicalTerm(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory) {
        ImmutableTerm subLexicalTerm = extractLexicalTerm(newTerms.get(0), termFactory);

        return termFactory.getImmutableFunctionalTerm(
                termFactory.getDBFunctionSymbolFactory().getBnodeStringTemplateFunctionSymbol(
                        Template.builder().addColumn().addSeparator("/").addColumn().build()),
                subLexicalTerm, termFactory.getDBRowUniqueStr());
    }
}
