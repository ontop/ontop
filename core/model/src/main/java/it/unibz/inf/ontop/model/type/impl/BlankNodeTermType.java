package it.unibz.inf.ontop.model.type.impl;


import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;


public class BlankNodeTermType extends RDFTermTypeImpl implements ObjectRDFType {

    protected BlankNodeTermType(TermTypeAncestry parentAncestry) {
        super("BNODE", parentAncestry, DBTypeFactory::getDBStringType);
    }

    @Override
    public boolean isBlankNode() {
        return true;
    }
}
