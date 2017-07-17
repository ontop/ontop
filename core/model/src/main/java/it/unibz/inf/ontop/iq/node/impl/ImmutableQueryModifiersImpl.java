package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.ImmutableQueryModifiers;
import it.unibz.inf.ontop.iq.node.OrderCondition;
import it.unibz.inf.ontop.iq.node.QueryModifiers;

import java.util.Optional;

public class ImmutableQueryModifiersImpl implements ImmutableQueryModifiers {

    private final boolean isDistinct;
    private final long limit;
    private final long offset;
    private final ImmutableList<OrderCondition> sortConditions;

    public ImmutableQueryModifiersImpl(boolean isDistinct, long limit,
                                       long offset, ImmutableList<OrderCondition> sortConditions) {
        this.isDistinct = isDistinct;
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
    public Optional<ImmutableQueryModifiers> newSortConditions(ImmutableList<OrderCondition> newSortConditions) {
        if (isDistinct || hasLimit() || hasOffset() || (!newSortConditions.isEmpty())) {
            ImmutableQueryModifiers newModifiers = new ImmutableQueryModifiersImpl(isDistinct, limit, offset,
                    newSortConditions);
            return Optional.of(newModifiers);
        }

        return Optional.empty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || !(obj instanceof ImmutableQueryModifiersImpl)) {
            return false;
        }
        ImmutableQueryModifiersImpl name2 = (ImmutableQueryModifiersImpl) obj;
        return this.limit == name2.limit && this.offset == name2.offset && this.isDistinct == name2.isDistinct && this.sortConditions.equals(name2.sortConditions);
    }

    private boolean hasModifiers() {
        return isDistinct || hasLimit() || hasOffset() || hasOrder();
    }


    /**
     * Merge two successively applied set of quantifiers (without intermediate operation)
     * Due to possible interactions,
     * a conservative approach is adopted.
     * <p>
     * Let p be the parent query modifiers, and s be the subquery modifiers.
     * If both p and s have more than one modifier within (OFFSET, LIMIT, ORDER or DISTINCT),
     * or if they have each exactly one,
     * but these are not of the same type,
     * then merging fails.
     */
    public static Optional<ImmutableQueryModifiers> merge(ImmutableQueryModifiers parentQueryModifiers,
                                                          ImmutableQueryModifiers subqueryModifers) {

        if (areMergeable(parentQueryModifiers, subqueryModifers)) {
            return Optional.of(
                    new ImmutableQueryModifiersImpl(
                            parentQueryModifiers.isDistinct() || subqueryModifers.isDistinct(),
                            mergeLimits(
                                    parentQueryModifiers.getLimit(),
                                    subqueryModifers.getLimit()
                            ),
                            mergeOffsets(
                                    parentQueryModifiers.getOffset(),
                                    subqueryModifers.getOffset()
                            ),
                            mergeSortConditions(
                                    parentQueryModifiers.getSortConditions(),
                                    subqueryModifers.getSortConditions()
                            )));

        }
        return Optional.empty();
    }

    private static boolean areMergeable(ImmutableQueryModifiers parentQueryModifiers, ImmutableQueryModifiers subqueryModifers) {
        int pModCount = countInteractingModifiers(parentQueryModifiers);
        int sModCount = countInteractingModifiers(subqueryModifers);
        if(pModCount == 0 || sModCount == 0){
            return true;
        }
        if(pModCount == 1 && sModCount == 1){
            if(parentQueryModifiers.isDistinct() && subqueryModifers.isDistinct()){
                return true;
            }
            if(parentQueryModifiers.hasOrder() && subqueryModifers.hasOrder()){
                return true;
            }
            if(parentQueryModifiers.hasLimit() && subqueryModifers.hasLimit()){
                return true;
            }
            if(parentQueryModifiers.hasOffset() && subqueryModifers.hasOffset()){
                return true;
            }
        }
        return false;
    }

    private static int countInteractingModifiers(ImmutableQueryModifiers modifiers) {
        int i = 0;
        if(modifiers.hasOffset()){
            i++;
        }
        if(modifiers.isDistinct()){
            i++;
        }
        if(modifiers.hasLimit()){
            i++;
        }
        if(modifiers.hasOrder()){
            i++;
        }
        return i;
    }

    public static long mergeOffsets(long offset1, long offset2) {
        if (offset1 != -1) {
            if (offset2 != -1) {
                return offset1 + offset2;
            }
            return offset1;
        }
        return offset2;
    }

    public static long mergeLimits(long limit1, long limit2) {
        if (limit1 != -1) {
            if (limit2 != -1) {
                return limit1 < limit2 ?
                        limit1 :
                        limit2;
            }
            return limit1;
        }
        return limit2;
    }

    public static ImmutableList<OrderCondition> mergeSortConditions(ImmutableList<OrderCondition> parentquerySortConditions,
                                                                    ImmutableList<OrderCondition> subQuerysortConditions) {
        return parentquerySortConditions.isEmpty() ?
                subQuerysortConditions :
                parentquerySortConditions;
    }

    @Override
    public boolean isIdle() {
        return !(hasOrder() || hasLimit() || hasOffset() || isDistinct());
    }
}
