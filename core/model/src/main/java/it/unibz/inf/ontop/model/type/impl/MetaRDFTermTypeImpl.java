package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.MetaRDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

public class MetaRDFTermTypeImpl extends TermTypeImpl implements MetaRDFTermType {

    private MetaRDFTermTypeImpl(TermTypeAncestry parentAncestry) {
        super("meta-rdf", parentAncestry, false);
    }

    static MetaRDFTermType createMetaRDFTermType(TermTypeAncestry parentAncestry) {
        return new MetaRDFTermTypeImpl(parentAncestry);
    }
}
