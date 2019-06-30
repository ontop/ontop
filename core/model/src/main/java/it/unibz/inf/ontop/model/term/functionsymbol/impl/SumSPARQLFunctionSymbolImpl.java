package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLAggregationFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

import java.util.Optional;

public class SumSPARQLFunctionSymbolImpl extends SPARQLFunctionSymbolImpl implements SPARQLAggregationFunctionSymbol {

    protected SumSPARQLFunctionSymbolImpl(RDFTermType rootRdfTermType) {
        super("SP_SUM", SPARQL.SUM, ImmutableList.of(rootRdfTermType));
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    /**
     * Too complex to be implemented (for the moment)
     */
    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.empty();
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    /**
     * Nullable due to typing errors
     */
    @Override
    public boolean isNullable(ImmutableSet<Integer> nullableIndexes) {
        return true;
    }

    @Override
    public Optional<ImmutableFunctionalTerm.FunctionalTermDecomposition> decomposeIntoDBAggregation(
            ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory, VariableNullability variableNullability) {
        // TODO: implement it seriously
        return Optional.empty();
    }

    @Override
    public boolean isAggregation() {
        return true;
    }
}
