package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;

import java.util.function.Function;


public interface DBFunctionSymbol extends FunctionSymbol {

    /**
     * Returns a String in the native query language.
     *
     */
    String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                             Function<ImmutableTerm, String> termConverter, TermFactory termFactory);
}
