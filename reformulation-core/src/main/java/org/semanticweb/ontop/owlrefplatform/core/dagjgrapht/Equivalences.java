package org.semanticweb.ontop.owlrefplatform.core.dagjgrapht;

import java.util.Set;

/**
 * Created by benjamin on 15/10/14.
 */
public interface Equivalences<T> extends Iterable<T> {
    void setRepresentative(T representative);

    T getRepresentative();

    boolean isIndexed();

    void setIndexed();

    Set<T> getMembers();

    int size();

    boolean contains(T v);
}
