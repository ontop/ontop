package unibz.inf.ontop.pivotalrepr.impl;

import java.util.Optional;
import unibz.inf.ontop.model.ImmutableBooleanExpression;
import unibz.inf.ontop.model.ImmutableSubstitution;
import unibz.inf.ontop.model.VariableOrGroundTerm;
import unibz.inf.ontop.pivotalrepr.JoinLikeNode;

public abstract class JoinLikeNodeImpl extends JoinOrFilterNodeImpl implements JoinLikeNode {

    protected JoinLikeNodeImpl(Optional<ImmutableBooleanExpression> optionalJoinCondition) {
        super(optionalJoinCondition);
    }

    protected Optional<ImmutableBooleanExpression> transformOptionalBooleanExpression(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            Optional<ImmutableBooleanExpression> optionalFilterCondition) {
        if (optionalFilterCondition.isPresent()) {
            return Optional.of(transformBooleanExpression(substitution, optionalFilterCondition.get()));
        }
        return Optional.empty();
    }

}
