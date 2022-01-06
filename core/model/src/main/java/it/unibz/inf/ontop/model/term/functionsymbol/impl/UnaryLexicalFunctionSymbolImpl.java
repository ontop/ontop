package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;

import java.util.Optional;
import java.util.function.Function;


/**
 * Takes a lexical term and a meta RDF term type term as input.
 * Returns another lexical term.
 *
 */
public class UnaryLexicalFunctionSymbolImpl extends UnaryLatelyTypedFunctionSymbolImpl {

    protected UnaryLexicalFunctionSymbolImpl(DBTermType dbStringType, MetaRDFTermType metaRDFTermType,
                                             Function<DBTermType, Optional<DBFunctionSymbol>> dbFunctionSymbolFct) {
        super(dbStringType, metaRDFTermType, dbStringType, dbFunctionSymbolFct);
    }

    /**
     * Converts back the natural DB term into a lexical term.
     */
    @Override
    protected ImmutableTerm transformNaturalDBTerm(ImmutableFunctionalTerm dbTerm, DBTermType inputDBType, RDFTermType rdfType,
                                                   TermFactory termFactory) {
        return termFactory.getConversion2RDFLexical(inputDBType, dbTerm, rdfType);
    }
}
