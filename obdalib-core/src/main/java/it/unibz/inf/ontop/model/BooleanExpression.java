package it.unibz.inf.ontop.model;

/**
 * Boolean functional term
 */
public interface BooleanExpression extends Function {

    @Override
    OperationPredicate getFunctionSymbol();

    @Override
    BooleanExpression clone();
}
