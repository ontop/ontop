package org.semanticweb.ontop.pivotalrepr;

import org.semanticweb.ontop.model.DistinctVariableDataAtom;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.VariableOrGroundTerm;

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
            QueryNode descendantNode, IntermediateQuery query);

    @Override
    SubstitutionResults<? extends DelimiterCommutativeJoinNode> applyDescendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution);

}
