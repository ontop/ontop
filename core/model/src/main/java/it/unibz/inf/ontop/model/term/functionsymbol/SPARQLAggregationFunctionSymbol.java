package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;

import java.util.Optional;

public interface SPARQLAggregationFunctionSymbol extends SPARQLFunctionSymbol {

    /**
     * TODO: add additional arguments
     */
    Optional<ImmutableFunctionalTerm.FunctionalTermDecomposition> decomposeIntoDBAggregation(
            ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory, VariableNullability variableNullability);
}
