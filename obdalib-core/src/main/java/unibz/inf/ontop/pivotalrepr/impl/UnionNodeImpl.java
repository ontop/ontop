package unibz.inf.ontop.pivotalrepr.impl;


import com.google.common.collect.ImmutableSet;
import unibz.inf.ontop.model.ImmutableSubstitution;
import unibz.inf.ontop.model.Variable;
import unibz.inf.ontop.model.VariableOrGroundTerm;
import unibz.inf.ontop.pivotalrepr.*;

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
    public SubstitutionResults<UnionNode> applyAscendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) {
        return applyDescendentSubstitution(substitution);
    }

    @Override
    public SubstitutionResults<UnionNode> applyDescendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution) {
        return new SubstitutionResultsImpl<>(clone(), substitution);
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
