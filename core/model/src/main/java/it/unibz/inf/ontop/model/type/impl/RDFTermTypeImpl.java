package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

public class RDFTermTypeImpl extends TermTypeImpl implements RDFTermType {

    protected RDFTermTypeImpl(String name, TermTypeAncestry parentAncestry, boolean isAbstract) {
        super(name, parentAncestry, isAbstract);
    }

    /**
     * For the root of all the RDF term types ONLY
     */
    private RDFTermTypeImpl(TermTypeAncestry parentAncestry) {
        super("RDFTerm", parentAncestry, true);
    }

    static RDFTermType createRDFTermRoot(TermTypeAncestry parentAncestry) {
        return new RDFTermTypeImpl(parentAncestry);
    }
}
