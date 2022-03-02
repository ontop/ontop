package it.unibz.inf.ontop.generation.algebra;

import it.unibz.inf.ontop.model.term.Variable;

import java.util.Optional;

public interface SQLFlattenExpression extends SQLExpression {

    SQLExpression getSubExpression();

    Variable getFlattenedVar();

    Variable getOutputVar();

    Optional<Variable> getIndexVar();
}
