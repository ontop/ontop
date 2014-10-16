package org.semanticweb.ontop.owlrefplatform.core;

import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.ontology.Description;

import java.util.Map;
import java.util.Set;

/**
 * Automatically extracted from the implementation.
 * TODO: explain
 */
public interface EquivalenceMap {
    Assertion getNormal(Assertion assertion);

    // TESTS ONLY
    boolean containsKey(Predicate p);

    // TESTS ONLY
    int keySetSize();

    // TO BE REMOVED
    @Deprecated
    Description getValue(Predicate p);

    // TO BE REMOVED: USED ONLY ONCE
    @Deprecated
    Set<Predicate> keySet();

    // TO BE REMOVED
    @Deprecated
    Map<Predicate, Description> getInternalMap();

    Function getNormal(Function atom);
}
