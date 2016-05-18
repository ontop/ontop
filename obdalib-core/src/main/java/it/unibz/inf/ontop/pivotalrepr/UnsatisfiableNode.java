package it.unibz.inf.ontop.pivotalrepr;

import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;

/**
 * Temporary QueryNode that says that replace a non-satisfied sub-tree.
 *
 * Is expected to remove quickly.
 */
public interface UnsatisfiableNode extends QueryNode {

    @Override
    UnsatisfiableNode clone();

    @Override
    UnsatisfiableNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException, NotNeededNodeException;

    @Override
    SubstitutionResults<UnsatisfiableNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) ;

    @Override
    SubstitutionResults<UnsatisfiableNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution) ;
}
