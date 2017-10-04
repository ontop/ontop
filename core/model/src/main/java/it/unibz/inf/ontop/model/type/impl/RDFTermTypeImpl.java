package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.COL_TYPE;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

import javax.annotation.Nonnull;

public class RDFTermTypeImpl extends TermTypeImpl implements RDFTermType {

    protected RDFTermTypeImpl(@Nonnull COL_TYPE colType, TermTypeAncestry parentAncestry, boolean isAbstract) {
        super(colType, parentAncestry, isAbstract);
    }

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
