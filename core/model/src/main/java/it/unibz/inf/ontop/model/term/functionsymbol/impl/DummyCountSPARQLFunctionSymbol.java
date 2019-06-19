package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

import java.util.Optional;

public class DummyCountSPARQLFunctionSymbol extends SPARQLFunctionSymbolImpl{

    public DummyCountSPARQLFunctionSymbol(RDFTermType rootRdfTermType) {
        super("SP_COUNT_1", SPARQL.COUNT, ImmutableList.of(rootRdfTermType));
    }

    public DummyCountSPARQLFunctionSymbol() {
        super("SP_COUNT_0", SPARQL.COUNT, ImmutableList.of());
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.empty();
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public boolean isNullable(ImmutableSet<Integer> nullableIndexes) {
        return false;
    }
}
