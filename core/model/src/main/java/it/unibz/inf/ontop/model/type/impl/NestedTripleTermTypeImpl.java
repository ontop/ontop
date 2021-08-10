package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.NestedTripleTermType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

public class NestedTripleTermTypeImpl extends RDFStarTermTypeImpl implements NestedTripleTermType {

    /**
     * For the root of all the nested triple term types ONLY
     */
    private NestedTripleTermTypeImpl(TermTypeAncestry parentAncestry) {
        super("NestedTripleTerm", parentAncestry);
    }

    static NestedTripleTermType createNestedTripleTermTypeRoot(TermTypeAncestry parentAncestry) {
        return new NestedTripleTermTypeImpl(parentAncestry);
    }
}
