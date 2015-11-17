package org.semanticweb.ontop.model;


import com.google.common.collect.ImmutableList;

public interface ImmutableBooleanExpression extends BooleanExpression, ImmutableFunctionalTerm {
    @Override
    ImmutableBooleanExpression clone();

    /**
     * Flattens AND expressions.
     */
    ImmutableList<ImmutableBooleanExpression> flatten();
}
