package unibz.inf.ontop.pivotalrepr;

import unibz.inf.ontop.model.VariableOrGroundTerm;
import unibz.inf.ontop.model.ImmutableSubstitution;

public interface UnionNode extends QueryNode {

    @Override
    UnionNode clone();

    @Override
    UnionNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException;

    @Override
    SubstitutionResults<UnionNode> applyAscendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) ;

    @Override
    SubstitutionResults<UnionNode> applyDescendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution) ;
}
