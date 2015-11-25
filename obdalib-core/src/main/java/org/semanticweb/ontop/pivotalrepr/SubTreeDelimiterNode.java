package org.semanticweb.ontop.pivotalrepr;

import org.semanticweb.ontop.model.DataAtom;

/**
 * Common abstraction for the ConstructionNode and DataNode.
 * Also useful for some extensions.
 *
 * A SubTreeDelimiterNode defines with one projection atom
 * the variables used in a sub-tree (composed at least of one node, itself).
 *
 */
public interface SubTreeDelimiterNode extends QueryNode {
    /**
     * Data atom containing the projected variables
     */
    DataAtom getProjectionAtom();
}
