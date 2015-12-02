package org.semanticweb.ontop.pivotalrepr;

import org.semanticweb.ontop.model.DistinctVariableDataAtom;

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

    /**
     * Returns a new DelimiterCommutativeJoinNode
     */
    DelimiterCommutativeJoinNode newAtom(DistinctVariableDataAtom newAtom);
}
