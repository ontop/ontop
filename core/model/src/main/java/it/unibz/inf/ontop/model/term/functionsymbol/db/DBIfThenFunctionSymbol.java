package it.unibz.inf.ontop.model.term.functionsymbol.db;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;

import java.util.stream.Stream;

/**
 * Abstraction for CASE, IF-ELSE-NULL and so on
 */
public interface DBIfThenFunctionSymbol extends DBFunctionSymbol {

    Stream<ImmutableTerm> extractPossibleValues(ImmutableList<? extends ImmutableTerm> arguments);
}
