package unibz.inf.ontop.pivotalrepr;

import java.util.Optional;

import unibz.inf.ontop.model.ImmutableBooleanExpression;
import unibz.inf.ontop.model.VariableOrGroundTerm;
import unibz.inf.ontop.model.ImmutableSubstitution;

/**
 * Commutative: the children order does not matter
 */
public interface CommutativeJoinNode extends JoinLikeNode {

    @Override
    CommutativeJoinNode changeOptionalFilterCondition(Optional<ImmutableBooleanExpression> newOptionalFilterCondition);

    @Override
    SubstitutionResults<? extends CommutativeJoinNode> applyAscendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) throws QueryNodeSubstitutionException;

    @Override
    SubstitutionResults<? extends CommutativeJoinNode> applyDescendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution) throws QueryNodeSubstitutionException;

}
