package it.unibz.inf.ontop.datalog;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;

/**
 * ImmutableQueryModifiers contains information about:
 *   - DISTINCT
 *   - ORDER BY
 *   - LIMIT
 *   - OFFSET
 *
 * BUT NOT ABOUT GROUP BY
 * (since the latter strongly changes the semantic of the query).
 *
 * Cannot be empty.
 *
 */
public interface ImmutableQueryModifiers extends QueryModifiers {

    @Override
    ImmutableList<OrderCondition> getSortConditions();

    /**
     * Inserts the relevant QueryModifierNodes at the root
     */
    IntermediateQueryBuilder initBuilder(IntermediateQueryFactory iqFactory, IntermediateQueryBuilder queryBuilder,
                                         DistinctVariableOnlyDataAtom projectionAtom, QueryNode childNode);

    IQTree insertAbove(IQTree childTree, IntermediateQueryFactory iqFactory);
}
