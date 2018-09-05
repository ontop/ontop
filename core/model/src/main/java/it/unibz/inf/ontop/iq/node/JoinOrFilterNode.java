package it.unibz.inf.ontop.iq.node;

import java.util.Optional;

import it.unibz.inf.ontop.model.term.ImmutableExpression;

/**
 * TODO: explain
 */
public interface JoinOrFilterNode extends QueryNode {

    Optional<ImmutableExpression> getOptionalFilterCondition();

}
