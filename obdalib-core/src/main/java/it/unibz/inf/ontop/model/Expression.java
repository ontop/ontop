package it.unibz.inf.ontop.model;

/**
 * An expression has an Operation predicate.
 *
 * Can be used an Effective Boolean Value.
 *
 * https://www.w3.org/TR/sparql11-query/#ebv
 *
 */
public interface Expression extends Function {

    @Override
    OperationPredicate getFunctionSymbol();

    @Override
    Expression clone();
}
