package it.unibz.inf.ontop.model.term.functionsymbol;

import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.TermFactory;

public interface AggregationFunctionSymbol extends FunctionSymbol {

    Constant evaluateEmptyBag(TermFactory termFactory);
}
