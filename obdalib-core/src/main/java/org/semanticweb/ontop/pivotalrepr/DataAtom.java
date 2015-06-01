package org.semanticweb.ontop.pivotalrepr;

import org.semanticweb.ontop.model.Function;

/**
 * TODO: explain
 *
 * In the future, this class will be disassociated from the Function class.
 */
public interface DataAtom extends Function {

    AtomPredicate getPredicate();
}
