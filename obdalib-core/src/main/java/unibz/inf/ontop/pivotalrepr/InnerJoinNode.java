package unibz.inf.ontop.pivotalrepr;

import java.util.Optional;
import unibz.inf.ontop.model.ImmutableBooleanExpression;
import unibz.inf.ontop.model.ImmutableSubstitution;
import unibz.inf.ontop.model.VariableOrGroundTerm;

public interface InnerJoinNode extends CommutativeJoinNode {

    @Override
    InnerJoinNode clone();

    @Override
    InnerJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;

    @Override
    InnerJoinNode changeOptionalFilterCondition(Optional<ImmutableBooleanExpression> newOptionalFilterCondition);

    @Override
    SubstitutionResults<InnerJoinNode> applyAscendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query);

    @Override
    SubstitutionResults<InnerJoinNode> applyDescendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution);
}
