package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;

/**
 * TODO: find a better name
 */
public interface DataAtomQueryNode<P extends AtomPredicate> extends QueryNode {

    /**
     * Data atom containing the projected variables
     */
    DataAtom<P> getDataAtom();
}
