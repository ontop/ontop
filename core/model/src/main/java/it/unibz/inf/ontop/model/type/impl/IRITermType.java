package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.COL_TYPE;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;


public class IRITermType extends RDFTermTypeImpl implements ObjectRDFType {

    protected IRITermType(TermTypeAncestry parentAncestry) {
        super(COL_TYPE.OBJECT, parentAncestry, false);
    }

    @Override
    public boolean isBlankNode() {
        return false;
    }
}
