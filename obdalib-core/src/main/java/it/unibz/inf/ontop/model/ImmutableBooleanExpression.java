package it.unibz.inf.ontop.model;


import com.google.common.collect.ImmutableSet;

public interface ImmutableBooleanExpression extends BooleanExpression, ImmutableFunctionalTerm {
    @Override
    ImmutableBooleanExpression clone();

    /**
     * Flattens AND expressions.
     */
    ImmutableSet<ImmutableBooleanExpression> flattenAND();

    /**
     * Flattens OR expressions.
     */
    ImmutableSet<ImmutableBooleanExpression> flattenOR();

    /**
     * Generalization of flattening (AND, OR, etc.).
     *
     * It is the responsibility of the caller to make sure such a flattening makes sense.
     */
    ImmutableSet<ImmutableBooleanExpression> flatten(OperationPredicate operator);
}
