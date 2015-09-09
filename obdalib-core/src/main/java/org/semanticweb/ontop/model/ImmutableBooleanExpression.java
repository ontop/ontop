package org.semanticweb.ontop.model;


public interface ImmutableBooleanExpression extends BooleanExpression, ImmutableFunctionalTerm {
    @Override
    ImmutableBooleanExpression clone();
}
