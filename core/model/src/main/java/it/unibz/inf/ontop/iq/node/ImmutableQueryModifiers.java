package it.unibz.inf.ontop.iq.node;

import java.util.Optional;
import com.google.common.collect.ImmutableList;

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
