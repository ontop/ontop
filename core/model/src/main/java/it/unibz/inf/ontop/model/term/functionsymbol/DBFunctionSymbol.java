package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableList;


public interface DBFunctionSymbol extends FunctionSymbol {

    /**
     * Returns a String in the native query language.
     *
     */
    String getNativeDBString(ImmutableList<String> termStrings);
}
