package org.semanticweb.ontop.pivotalrepr;

/**
 * TODO: explain
 */
public interface Rule {

    DataAtom getHead();

    IntermediateQuery getBody();
}
