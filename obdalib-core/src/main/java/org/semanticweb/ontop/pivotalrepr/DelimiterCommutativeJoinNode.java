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
     * TODO: specialize it further (no variable duplication)
     */
    @Override
    DistinctVariableDataAtom getProjectionAtom();
}
