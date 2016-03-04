package it.unibz.inf.ontop.pivotalrepr.impl;

import java.util.Optional;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;
import it.unibz.inf.ontop.pivotalrepr.JoinLikeNode;

public abstract class JoinLikeNodeImpl extends JoinOrFilterNodeImpl implements JoinLikeNode {

    protected JoinLikeNodeImpl(Optional<ImmutableExpression> optionalJoinCondition) {
        super(optionalJoinCondition);
    }

    protected Optional<ImmutableExpression> transformOptionalBooleanExpression(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            Optional<ImmutableExpression> optionalFilterCondition) {
        if (optionalFilterCondition.isPresent()) {
            return Optional.of(transformBooleanExpression(substitution, optionalFilterCondition.get()));
        }
        return Optional.empty();
    }

}
