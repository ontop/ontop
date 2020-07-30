package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;


public class IRITermType extends RDFTermTypeImpl implements ObjectRDFType {

    protected IRITermType(TermTypeAncestry parentAncestry) {
        super("IRI", parentAncestry, DBTypeFactory::getDBStringType);
    }

    @Override
    public boolean isBlankNode() {
        return false;
    }
}
