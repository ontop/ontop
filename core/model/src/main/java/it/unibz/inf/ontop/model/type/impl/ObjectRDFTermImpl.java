package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.type.ObjectRDFType;


public class ObjectRDFTermImpl extends AbstractTermType implements ObjectRDFType {
    protected ObjectRDFTermImpl(Predicate.COL_TYPE colType) {
        super(colType);
    }
}
