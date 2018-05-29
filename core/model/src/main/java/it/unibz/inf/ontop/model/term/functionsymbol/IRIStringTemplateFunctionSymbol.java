package it.unibz.inf.ontop.model.term.functionsymbol;

/**
 * Such a function symbol is specific to IRI template
 *
 * NB: a functional term using this symbol is producing a DB string or a NULL
 */
public interface IRIStringTemplateFunctionSymbol extends FunctionSymbol {

    String getIRITemplate();
}
