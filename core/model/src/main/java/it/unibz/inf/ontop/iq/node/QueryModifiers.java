package it.unibz.inf.ontop.iq.node;


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

    boolean isIdle();
}
