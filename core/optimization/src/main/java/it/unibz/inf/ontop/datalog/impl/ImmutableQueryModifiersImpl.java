package it.unibz.inf.ontop.datalog.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.datalog.ImmutableQueryModifiers;
import it.unibz.inf.ontop.datalog.OrderCondition;
import it.unibz.inf.ontop.datalog.QueryModifiers;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.SliceNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
    public IntermediateQueryBuilder initBuilder(IntermediateQueryFactory iqFactory, IntermediateQueryBuilder queryBuilder,
                                                DistinctVariableOnlyDataAtom projectionAtom, QueryNode childNode) {

        ImmutableList<UnaryOperatorNode> modifierNodes = extractModifierNodes(iqFactory);

        if (modifierNodes.isEmpty())
            queryBuilder.init(projectionAtom, childNode);
        else {
            queryBuilder.init(projectionAtom, modifierNodes.get(0));
            IntStream.range(1, modifierNodes.size())
                    .forEach(i -> queryBuilder.addChild(modifierNodes.get(i - 1), modifierNodes.get(i)));
            queryBuilder.addChild(modifierNodes.get(modifierNodes.size() - 1), childNode);
        }
        return queryBuilder;
    }

    @Override
    public IQTree insertAbove(IQTree childTree, IntermediateQueryFactory iqFactory) {
        ImmutableList<UnaryOperatorNode> modifierNodes = extractModifierNodes(iqFactory);

        return modifierNodes.reverse().stream()
                .reduce(childTree,
                        (t, n) -> iqFactory.createUnaryIQTree(n, t),
                        (t1, t2) ->  {throw new MinorOntopInternalBugException("Merging should never been executed"); });
    }

    /**
     * Top-down order
     */
    private ImmutableList<UnaryOperatorNode> extractModifierNodes(IntermediateQueryFactory iqFactory) {
        long correctedOffset = offset > 0 ? offset : 0;

        Optional<SliceNode> sliceNode = Optional.of(limit)
                .filter(l -> l >= 0)
                .map(l -> Optional.of(iqFactory.createSliceNode(correctedOffset, l)))
                .orElseGet(() -> Optional.of(correctedOffset)
                        .filter(o -> o > 0)
                        .map(iqFactory::createSliceNode));

        Optional<UnaryOperatorNode> distinctNode = isDistinct()
                ? Optional.of(iqFactory.createDistinctNode())
                : Optional.empty();

        ImmutableList<OrderByNode.OrderComparator> orderComparators = getSortConditions().stream()
                .map(o -> iqFactory.createOrderComparator(o.getVariable(),
                        o.getDirection() == OrderCondition.ORDER_ASCENDING))
                .collect(ImmutableCollectors.toList());

        Optional<UnaryOperatorNode> orderByNode = orderComparators.isEmpty()
                ? Optional.empty()
                : Optional.of(iqFactory.createOrderByNode(orderComparators));

         return Stream.of(sliceNode, distinctNode, orderByNode)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableCollectors.toList());
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
}
