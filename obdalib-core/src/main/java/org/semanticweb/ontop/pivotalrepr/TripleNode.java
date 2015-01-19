package org.semanticweb.ontop.pivotalrepr;

/**
 *
 */
public interface TripleNode extends DataNode {
    public QueryTerm getSubject();
    public QueryTerm getPredicate();
    public QueryTerm getObject();
}
