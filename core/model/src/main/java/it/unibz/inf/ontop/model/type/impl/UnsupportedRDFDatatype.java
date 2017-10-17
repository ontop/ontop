package it.unibz.inf.ontop.model.type.impl;


import it.unibz.inf.ontop.model.type.COL_TYPE;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;
import it.unibz.inf.ontop.model.vocabulary.OntopInternal;
import org.apache.commons.rdf.api.IRI;

public class UnsupportedRDFDatatype extends SimpleRDFDatatype {

    private UnsupportedRDFDatatype(TermTypeAncestry parentAncestry) {
        super(COL_TYPE.UNSUPPORTED, parentAncestry, false, OntopInternal.UNSUPPORTED);
    }

    private UnsupportedRDFDatatype(TermTypeAncestry parentAncestry, IRI concreteIRI) {
        super(COL_TYPE.UNSUPPORTED, parentAncestry, false, concreteIRI);
    }

    static RDFDatatype createUnsupportedDatatype(TermTypeAncestry parentAncestry, IRI concreteIRI) {
        return new UnsupportedRDFDatatype(parentAncestry, concreteIRI);
    }

    static RDFDatatype createUnsupportedDatatype(TermTypeAncestry parentAncestry) {
        return new UnsupportedRDFDatatype(parentAncestry);
    }
}
