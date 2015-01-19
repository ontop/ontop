package org.semanticweb.ontop.pivotalrepr;

/**
 * Treats the "rdf:type" predicate as a special case.
 * Indeed, this predicate is not very discriminative.
 *
 */
public interface TypedObjectNode extends TripleNode {

    public String getObjectType();
}
