package org.semanticweb.ontop.model;

import org.semanticweb.ontop.pivotalrepr.QueryModifiers;

import java.util.List;

/**
 * Old-style mutable interface for QueryModifiers.
 *
 * Please prefer the new QueryModifiers interface that allows immutable implementations.
 *
 * ACCEPTS GROUP BY!
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

    void addOrderCondition(Variable var, int direction);

    void addGroupCondition(Variable var);

    boolean hasModifiers();

    boolean hasGroup();
    List<Variable> getGroupConditions();
}
