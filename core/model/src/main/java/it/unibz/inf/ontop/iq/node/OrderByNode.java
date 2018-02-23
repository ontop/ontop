package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.NonGroundTerm;

public interface OrderByNode extends QueryModifierNode {

    ImmutableList<OrderComparator> getComparators();

    @Override
    OrderByNode clone();


    interface OrderComparator {

        boolean isAscending();

        NonGroundTerm getTerm();
    }
}
