package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.*;

import java.util.function.Function;

public class RDFTermTypeImpl extends RDFStarTermTypeImpl implements RDFTermType {

    /**
     * Concrete RDF term type
     */
    protected RDFTermTypeImpl(String name, TermTypeAncestry parentAncestry,
                              Function<DBTypeFactory, DBTermType> closestDBTypeFct) {
        super(name, parentAncestry, closestDBTypeFct);
    }

    protected RDFTermTypeImpl(String name, TermTypeAncestry parentAncestry) {
        super(name, parentAncestry);
    }

    /**
     * For the root of all the RDF term types ONLY
     */
    private RDFTermTypeImpl(TermTypeAncestry parentAncestry) {
        this("RDFTerm", parentAncestry);
    }

    static RDFTermType createRDFTermRoot(TermTypeAncestry parentAncestry) {
        return new RDFTermTypeImpl(parentAncestry);
    }
}
