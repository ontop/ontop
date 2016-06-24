package it.unibz.inf.ontop.pivotalrepr.impl;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.*;

public class UnionNodeImpl extends QueryNodeImpl implements UnionNode {

    private static final String UNION_NODE_STR = "UNION";

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public UnionNode clone() {
        return new UnionNodeImpl();
    }

    @Override
    public UnionNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public SubstitutionResults<UnionNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) {
        return applyDescendingSubstitution(substitution, query);
    }

    @Override
    public SubstitutionResults<UnionNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {
        return new SubstitutionResultsImpl<>(clone(), substitution);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof UnionNode);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return ImmutableSet.of();
    }

    @Override
    public String toString() {
        return UNION_NODE_STR;
    }
}
