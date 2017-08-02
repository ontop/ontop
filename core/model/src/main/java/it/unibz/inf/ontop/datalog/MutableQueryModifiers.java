package it.unibz.inf.ontop.datalog;

import it.unibz.inf.ontop.iq.node.QueryModifiers;
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

    void copy(MutableQueryModifiers other);

    void setDistinct();


    void setLimit(long limit);

    void setOffset(long offset);

    void addOrderCondition(Variable var, int direction);

    void addGroupCondition(Variable var);

    boolean hasModifiers();

    boolean hasGroup();
    List<Variable> getGroupConditions();
}