package org.semanticweb.ontop.pivotalrepr;

import org.semanticweb.ontop.model.DataAtom;

/**
 * TODO: describe
 */
public interface DataNode extends SubTreeDelimiterNode {

    /**
     * Returns a new DataNode of the same type that will use the new atom
     */
    DataNode newAtom(DataAtom newAtom);
}
