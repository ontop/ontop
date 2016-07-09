package it.unibz.inf.ontop.pivotalrepr;

import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.ImmutableSubstitution;

public interface UnionNode extends QueryNode {

    @Override
    UnionNode clone();

    @Override
    UnionNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException;

    @Override
    SubstitutionResults<UnionNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) ;

    @Override
    SubstitutionResults<UnionNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) ;
}
