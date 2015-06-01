package org.semanticweb.ontop.pivotalrepr;

/**
 * TODO: keep it?
 *
 * Immutable.
 */
@Deprecated
public interface QueryTerm {

    public String getDatatype();
    public boolean hasDatatype();

    public URITemplate getURITemplate();
    public boolean hasURITemplate();
}
