package org.semanticweb.ontop.model;

/**
 * A helper class to store the sort conditions.
 *
 * Immutable
 */
public interface OrderCondition extends Cloneable {
    int ORDER_ASCENDING = 1;
    int ORDER_DESCENDING = 2;

    Variable getVariable();

    int getDirection();

    OrderCondition clone();
}
