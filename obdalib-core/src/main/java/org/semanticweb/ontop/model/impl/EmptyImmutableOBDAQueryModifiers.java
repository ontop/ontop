package org.semanticweb.ontop.model.impl;

import org.semanticweb.ontop.model.OBDAQueryModifiers;
import org.semanticweb.ontop.model.OrderCondition;
import org.semanticweb.ontop.model.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * Useful when query modifiers are not supported.
 */
public class EmptyImmutableOBDAQueryModifiers implements OBDAQueryModifiers {
    @Override
    public OBDAQueryModifiers clone() {
        return this;
    }

    @Override
    public void copy(OBDAQueryModifiers other) {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public void setDistinct() {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public void setLimit(long limit) {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public void setOffset(long offset) {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public void addOrderCondition(Variable var, int direction) {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public void addGroupCondition(Variable var) {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public boolean hasModifiers() {
        return false;
    }

    @Override
    public boolean hasGroup() {
        return false;
    }

    @Override
    public List<Variable> getGroupConditions() {
        return new ArrayList<>();
    }

    @Override
    public boolean isDistinct() {
        return false;
    }

    @Override
    public boolean hasOrder() {
        return false;
    }

    @Override
    public boolean hasLimit() {
        return false;
    }

    @Override
    public boolean hasOffset() {
        return false;
    }

    @Override
    public long getLimit() {
        return 0;
    }

    @Override
    public long getOffset() {
        return 0;
    }

    @Override
    public List<OrderCondition> getSortConditions() {
        return new ArrayList<>();
    }
}
