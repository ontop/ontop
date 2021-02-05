package it.unibz.inf.ontop.model.type.impl;


import it.unibz.inf.ontop.model.type.NumericRDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;
import org.apache.commons.rdf.api.IRI;

public class AbstractNumericRDFDatatype extends SimpleRDFDatatype implements NumericRDFDatatype {

    private AbstractNumericRDFDatatype(IRI datatypeIRI, TermTypeAncestry parentAncestry) {
        super(datatypeIRI, parentAncestry);
    }

    static NumericRDFDatatype createAbstractNumericTermType(IRI datatypeIRI, TermTypeAncestry parentAncestry) {
        return new AbstractNumericRDFDatatype(datatypeIRI, parentAncestry);
    }
}