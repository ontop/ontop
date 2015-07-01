package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.OBDAQueryModifiers;
import org.semanticweb.ontop.model.OrderCondition;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.pivotalrepr.ImmutableQueryModifiers;
import org.semanticweb.ontop.pivotalrepr.QueryModifiers;

import java.util.List;

public class ImmutableQueryModifiersImpl implements ImmutableQueryModifiers {

    private final boolean isDistinct;
    private final boolean isCount;
    private final long limit;
    private final long offset;
    private final ImmutableList<OrderCondition> sortConditions;

    public ImmutableQueryModifiersImpl(boolean isDistinct, boolean isCount, long limit,
                                       long offset, ImmutableList<OrderCondition> sortConditions) {
        this.isDistinct = isDistinct;
        this.isCount = isCount;
        this.limit = limit;
        this.offset = offset;
        this.sortConditions = sortConditions;
    }

    /**
     * Tip: use a mutable implementation of QueryModifiers
     * as a builder and then create an immutable object with this constructor.
     */
    public ImmutableQueryModifiersImpl(QueryModifiers modifiers) {

        isDistinct = modifiers.isDistinct();
        isCount = modifiers.isCount();
        limit = modifiers.getLimit();
        offset = modifiers.getOffset();
        sortConditions = ImmutableList.copyOf(modifiers.getSortConditions());

        if (!hasModifiers()) {
            throw new IllegalArgumentException("Empty QueryModifiers given." +
                    "Please use an Optional instead of creating an empty object.");
        }

    }

    @Override
    public boolean isDistinct() {
        return isDistinct;
    }

    @Override
    public boolean isCount() {
        return isCount;
    }

    @Override
    public boolean hasOrder() {
        return !sortConditions.isEmpty() ? true : false;
    }

    @Override
    public boolean hasLimit() {
        return limit != -1 ? true : false;
    }

    @Override
    public long getLimit() {
        return limit;
    }

    @Override
    public boolean hasOffset() {
        return offset != -1 ? true : false;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public ImmutableList<OrderCondition> getSortConditions() {
        return sortConditions;
    }

    @Override
    public ImmutableQueryModifiers newSortConditions(ImmutableList<OrderCondition> newSortConditions) {
        return new ImmutableQueryModifiersImpl(isDistinct, isCount, limit, offset, newSortConditions);
    }

    private boolean hasModifiers() {
        return isDistinct || hasLimit() || hasOffset() || hasOrder();
    }
}
