package it.unibz.inf.ontop.model.term;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.predicate.OperationPredicate;

public interface ImmutableExpression extends Expression, ImmutableFunctionalTerm {
    @Override
    ImmutableExpression clone();

    /**
     * Flattens AND expressions.
     */
    ImmutableSet<ImmutableExpression> flattenAND();

    /**
     * Flattens OR expressions.
     */
    ImmutableSet<ImmutableExpression> flattenOR();

    /**
     * Generalization of flattening (AND, OR, etc.).
     *
     * It is the responsibility of the caller to make sure such a flattening makes sense.
     */
    ImmutableSet<ImmutableExpression> flatten(OperationPredicate operator);

    boolean isVar2VarEquality();
}
