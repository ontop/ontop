package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.OrderCondition;

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

    Optional<ImmutableQueryModifiers> newSortConditions(ImmutableList<OrderCondition> sortConditions);
}
