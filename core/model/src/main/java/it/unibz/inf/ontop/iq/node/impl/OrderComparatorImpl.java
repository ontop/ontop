package it.unibz.inf.ontop.iq.node.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.model.term.NonGroundTerm;

public class OrderComparatorImpl implements OrderByNode.OrderComparator {

    private static final String ASC_PREFIX = "ASC(";
    private static final String DESC_PREFIX = "DESC(";

    private final NonGroundTerm term;
    private final boolean isAscending;

    @AssistedInject
    private OrderComparatorImpl(@Assisted NonGroundTerm term, @Assisted boolean isAscending) {
        this.term = term;
        this.isAscending = isAscending;
    }

    @Override
    public boolean isAscending() {
        return isAscending;
    }

    @Override
    public NonGroundTerm getTerm() {
        return term;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof OrderByNode.OrderComparator
                && ((OrderByNode.OrderComparator) other).getTerm().equals(term)
                && ((OrderByNode.OrderComparator) other).isAscending() == isAscending;
    }

    @Override
    public String toString() {
        String prefix = isAscending ? ASC_PREFIX : DESC_PREFIX;
        return prefix + term + ")";
    }
}
