package it.unibz.inf.ontop.generation.algebra;

import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;

public interface SQLFlattenExpression extends SQLExpression {

    SQLExpression getSubExpression();

    Variable getFlattenedVar();

    DBTermType getFlattenedType();

    Variable getOutputVar();

    Optional<Variable> getIndexVar();
}
