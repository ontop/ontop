package org.semanticweb.ontop.pivotalrepr;

/**
 * TODO: complete it.
 *
 * Immutable.
 */
public interface QueryTerm extends ExpressionElement {

    public String getDatatype();
    public boolean hasDatatype();

    public URITemplate getURITemplate();
    public boolean hasURITemplate();
}
