package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.OBDAQueryModifiers;
import org.semanticweb.ontop.model.OrderCondition;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.pivotalrepr.ImmutableQueryModifiers;
import org.semanticweb.ontop.pivotalrepr.QueryModifiers;

import java.util.List;

/**
 * Immutable implementation of QueryModifiers
 *
 * For backward compatibility, it also implements OBDAQueryModifiers
 * BUT DOES NOT SUPPORT ANY MUTABLE OPERATION.
 *
 */
public class ImmutableQueryModifiersImpl implements ImmutableQueryModifiers, OBDAQueryModifiers {

    private final boolean isDistinct;
    private final boolean isCount;
    private final long limit;
    private final long offset;
    private final ImmutableList<Variable> groupConditions;
    private final ImmutableList<OrderCondition> sortConditions;

    /**
     * Tip: use a mutable implementation of QueryModifiers
     * as a builder and then create an immutable object with this constructor.
     */
    public ImmutableQueryModifiersImpl(QueryModifiers modifiers) {
        isDistinct = modifiers.isDistinct();
        isCount = modifiers.isCount();
        limit = modifiers.getLimit();
        offset = modifiers.getOffset();
        groupConditions = ImmutableList.copyOf(modifiers.getGroupConditions());
        sortConditions = ImmutableList.copyOf(modifiers.getSortConditions());
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
    public boolean hasGroup() {
        return !groupConditions.isEmpty() ? true : false;
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
    public List<Variable> getGroupConditions() {
        return groupConditions;
    }

    @Override
    public List<OrderCondition> getSortConditions() {
        return sortConditions;
    }


    @Override
    public boolean hasModifiers() {
        return isDistinct || hasLimit() || hasOffset() || hasOrder() || hasGroup();
    }

    @Override
    public OBDAQueryModifiers clone() {
        return this;
    }

    @Override
    public void copy(OBDAQueryModifiers other) {
        throw new UnsupportedOperationException("Does not support mutable operations");
    }

    @Override
    public void setDistinct() {
        throw new UnsupportedOperationException("Does not support mutable operations");
    }

    @Override
    public void setCount() {
        throw new UnsupportedOperationException("Does not support mutable operations");
    }

    @Override
    public void setLimit(long limit) {
        throw new UnsupportedOperationException("Does not support mutable operations");
    }

    @Override
    public void setOffset(long offset) {
        throw new UnsupportedOperationException("Does not support mutable operations");
    }


    @Override
    public void addOrderCondition(Variable var, int direction) {
        throw new UnsupportedOperationException("Does not support mutable operations");
    }

    @Override
    public void addGroupCondition(Variable var) {
        throw new UnsupportedOperationException("Does not support mutable operations");
    }
}
