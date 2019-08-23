package it.unibz.inf.ontop.datalog;

import it.unibz.inf.ontop.model.term.Variable;

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

    /**
     * Creates a new OrderCondition using the new variable
     */
    OrderCondition newVariable(Variable newVariable);
}
