package it.unibz.inf.ontop.generation.algebra;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

/**
 * TODO: find a better name
 */
public interface SQLTable extends SQLExpression {

    RelationDefinition getRelationDefinition();

    ImmutableMap<Integer, ? extends VariableOrGroundTerm> getArgumentMap();

}
