package it.unibz.inf.ontop.model.type.impl;


import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;


public class BlankNodeTermType extends TermTypeImpl implements ObjectRDFType {

    protected BlankNodeTermType(TermTypeAncestry parentAncestry) {
        super("BNODE", parentAncestry, false);
    }

    @Override
    public boolean isBlankNode() {
        return true;
    }
}
