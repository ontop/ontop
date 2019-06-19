package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.IntStream;

public class DummyGroupConcatSPARQLFunctionSymbol extends SPARQLFunctionSymbolImpl {

    protected DummyGroupConcatSPARQLFunctionSymbol(RDFDatatype xsdStringType, int arity) {
        super(
                "SP_GROUP_CONCAT",
                SPARQL.GROUP_CONCAT,
                IntStream.range(0, arity).boxed()
                        .map(i -> xsdStringType)
                        .collect(ImmutableCollectors.toList())
        );
        if(arity < 1 || arity > 2){
            throw new RuntimeException("Arity 1 or 2 expected");
        }
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
        return true;
    }
}
