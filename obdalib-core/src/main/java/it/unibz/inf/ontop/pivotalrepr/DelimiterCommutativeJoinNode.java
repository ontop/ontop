package it.unibz.inf.ontop.pivotalrepr;

import it.unibz.inf.ontop.model.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.DistinctVariableDataAtom;
import it.unibz.inf.ontop.model.ImmutableSubstitution;

/**
 * TODO: find a better name
 *
 * Sub-interface that is the conjunction of CommutativeJoinNode and SubTreeDelimiterNode.
 *
 * Useful for extensions
 *
 */
public interface DelimiterCommutativeJoinNode extends CommutativeJoinNode, SubTreeDelimiterNode {

    /**
     * Specialization
     */
    @Override
    DistinctVariableDataAtom getProjectionAtom();


    @Override
    DelimiterCommutativeJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException, NotNeededNodeException;

    @Override
    SubstitutionResults<? extends DelimiterCommutativeJoinNode> applyAscendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) throws QueryNodeSubstitutionException;

    @Override
    SubstitutionResults<? extends DelimiterCommutativeJoinNode> applyDescendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution) throws QueryNodeSubstitutionException;

}
