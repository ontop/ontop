package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

/**
 * Follows a NULLS FIRST semantics, similarly to SPARQL
 *
 * See https://www.w3.org/TR/sparql11-query/#modOrderBy
 */
public interface OrderByNode extends QueryModifierNode {

    ImmutableList<OrderComparator> getComparators();

    OrderByNode applySubstitution(ImmutableSubstitution<? extends ImmutableTerm> substitution);

    @Override
    OrderByNode clone();


    interface OrderComparator {

        boolean isAscending();

        NonGroundTerm getTerm();
    }
}
