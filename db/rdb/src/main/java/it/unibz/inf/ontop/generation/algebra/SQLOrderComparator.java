package it.unibz.inf.ontop.generation.algebra;

import it.unibz.inf.ontop.model.term.NonConstantTerm;

/**
 * Differs from OrderByNode.OrderComparator by allowing aggregation functional terms, which my be ground.
 *
 * E.g. COUNT(), SUM(1) etc.
 *
 */
public interface SQLOrderComparator {

    boolean isAscending();

    NonConstantTerm getTerm();
}
