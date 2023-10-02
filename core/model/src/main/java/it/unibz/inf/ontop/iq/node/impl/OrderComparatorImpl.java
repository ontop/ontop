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
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof OrderComparatorImpl) {
            OrderComparatorImpl that = (OrderComparatorImpl)o;
            return this.term.equals(that.term) && this.isAscending == that.isAscending;
        }
        return false;
    }

    @Override
    public String toString() {
        return (isAscending ? ASC_PREFIX : DESC_PREFIX) + term + ")";
    }
}
