package it.unibz.inf.ontop.pivotalrepr;


import it.unibz.inf.ontop.model.OrderCondition;

import java.util.List;

/**
 * Query modifiers (DISTINCT, OFFSET, etc.)
 *
 * Allows immutable implementations.
 */
public interface QueryModifiers {

    boolean isDistinct();
    boolean hasOrder();
    boolean hasLimit();
    boolean hasOffset();
    long getLimit();
    long getOffset();
    List<OrderCondition> getSortConditions();
}
