package it.unibz.inf.ontop.pivotalrepr.impl;

import java.util.Optional;

import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.pivotalrepr.*;

public class InnerJoinNodeImpl extends JoinLikeNodeImpl implements InnerJoinNode {

    private static final String JOIN_NODE_STR = "JOIN" ;

    public InnerJoinNodeImpl(Optional<ImmutableExpression> optionalFilterCondition) {
        super(optionalFilterCondition);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public InnerJoinNode clone() {
        return new InnerJoinNodeImpl(getOptionalFilterCondition());
    }

    @Override
    public InnerJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public InnerJoinNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition) {
        return new InnerJoinNodeImpl(newOptionalFilterCondition);
    }

    @Override
    public SubstitutionResults<InnerJoinNode> applyAscendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) {
        return applyDescendentSubstitution(substitution);
    }

    @Override
    public SubstitutionResults<InnerJoinNode> applyDescendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution) {

        Optional<ImmutableExpression> newOptionalCondition = transformOptionalBooleanExpression(substitution, getOptionalFilterCondition());
        InnerJoinNode newNode = new InnerJoinNodeImpl(newOptionalCondition);

        return new SubstitutionResultsImpl<>(newNode, substitution);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof InnerJoinNode);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public String toString() {
        return JOIN_NODE_STR + getOptionalFilterString();
    }
}
