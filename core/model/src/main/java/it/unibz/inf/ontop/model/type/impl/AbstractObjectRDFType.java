package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;


public class AbstractObjectRDFType extends RDFTermTypeImpl implements ObjectRDFType {

    private AbstractObjectRDFType(TermTypeAncestry parentAncestry) {
        super("IRI or Bnode", parentAncestry);
    }

    @Override
    public boolean isBlankNode() {
        throw new UnsupportedOperationException(this + " is abstract, therefore we don't know if it is Bnode or not");
    }

    static ObjectRDFType createAbstractObjectRDFType(TermTypeAncestry parentAncestry) {
        return new AbstractObjectRDFType(parentAncestry);
    }
}
