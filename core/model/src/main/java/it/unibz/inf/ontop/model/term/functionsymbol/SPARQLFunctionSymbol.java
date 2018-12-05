package it.unibz.inf.ontop.model.term.functionsymbol;

import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

public interface SPARQLFunctionSymbol extends FunctionSymbol {

    Optional<IRI> getIRI();

    /**
     * Either the string of an IRI or a regular string if such an IRI does not exist.
     *
     * Examples:
     *   - http://www.w3.org/2005/xpath-functions#upper-case
     *   - MD5
     */
    String getOfficialName();
}
