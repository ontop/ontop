package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFTermType;

import java.util.UUID;

public class ZeroAryBnodeSPARQLFunctionSymbolImpl extends AbstractBnodeSPARQLFunctionSymbol {

    private final UUID uuid;

    protected ZeroAryBnodeSPARQLFunctionSymbolImpl(UUID uuid, RDFTermType bnodeType) {
        super("SP_BNODE" + uuid, ImmutableList.of(), bnodeType);
        this.uuid = uuid;
    }

    @Override
    protected ImmutableTerm buildLexicalTerm(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory) {
        return termFactory.getNullRejectingDBConcatFunctionalTerm(
                ImmutableList.of(
                        termFactory.getDBStringConstant(uuid.toString()),
                        termFactory.getDBRowUniqueStr()));
    }
}
