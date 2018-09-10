package it.unibz.inf.ontop.datalog;

import it.unibz.inf.ontop.model.term.Variable;

import java.util.List;

/**
 * Old-style mutable interface for QueryModifiers.
 *
 * Please prefer the new QueryModifiers interface that allows immutable implementations.
 *
 * ACCEPTS GROUP BY!
 *
 */
public interface MutableQueryModifiers extends QueryModifiers {

    MutableQueryModifiers clone();

    void setDistinct();

    void setLimit(long limit);

    void setOffset(long offset);

    void addOrderCondition(Variable var, int direction);

    boolean hasModifiers();

    boolean hasGroup();
}