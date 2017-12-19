package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.TermTypeAncestry;
import it.unibz.inf.ontop.model.type.UnboundRDFTermType;

public final class UnboundRDFTermTypeImpl extends RDFTermTypeImpl implements UnboundRDFTermType {

    private UnboundRDFTermTypeImpl(TermTypeAncestry parentAncestry) {
        super("NULL", parentAncestry, false);
    }

    static UnboundRDFTermType createUnboundRDFTermType(TermTypeAncestry parentAncestry) {
        return new UnboundRDFTermTypeImpl(parentAncestry);
    }
}
