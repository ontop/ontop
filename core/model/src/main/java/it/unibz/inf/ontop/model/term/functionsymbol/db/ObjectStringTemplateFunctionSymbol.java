package it.unibz.inf.ontop.model.term.functionsymbol.db;

/**
 * Such a function symbol is specific to object identifier (IRI, bnode) template
 *
 * NB: a functional term using this symbol is producing a DB string or a NULL
 */
public interface ObjectStringTemplateFunctionSymbol extends DBFunctionSymbol {
    String getTemplate();
}
