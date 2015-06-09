package org.semanticweb.ontop.model;

import org.semanticweb.ontop.pivotalrepr.QueryModifiers;

/**
 * Old-style mutable interface for QueryModifiers.
 *
 * Please prefer the new QueryModifiers interface that allows immutable implementations.
 *
 * TODO: rename it into MutableQueryModifiers
 *
 */
public interface OBDAQueryModifiers extends QueryModifiers {

    OBDAQueryModifiers clone();

    void copy(OBDAQueryModifiers other);

    void setDistinct();

    void setCount();

    void setLimit(long limit);

    void setOffset(long offset);

    boolean hasOffset();

    void addOrderCondition(Variable var, int direction);

    void addGroupCondition(Variable var);

    boolean hasModifiers();
}
