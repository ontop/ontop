package org.semanticweb.ontop.pivotalrepr;

import org.semanticweb.ontop.model.DataAtom;

/**
 * Abstraction of the ConstructionNode. Useful for some extensions.
 *
 * TODO: explain further.
 *
 */
public interface SubTreeDelimiterNode extends QueryNode {
    /**
     * Data atom containing the projected variables
     */
    DataAtom getProjectionAtom();
}
