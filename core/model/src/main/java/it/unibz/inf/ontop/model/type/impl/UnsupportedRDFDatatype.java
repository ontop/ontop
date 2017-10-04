package it.unibz.inf.ontop.model.type.impl;


import it.unibz.inf.ontop.model.type.COL_TYPE;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class UnsupportedRDFDatatype extends SimpleRDFDatatype {

    /**
     * TODO: remove it!
     */
    private static final IRI DEFAULT_ONTOP_UNSUPPORTED = SimpleValueFactory.getInstance().createIRI(
            "urn:it:unibz:inf:ontop:internal:unsupported");

    private UnsupportedRDFDatatype(TermTypeAncestry parentAncestry) {
        super(COL_TYPE.UNSUPPORTED, parentAncestry, false, DEFAULT_ONTOP_UNSUPPORTED);
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
