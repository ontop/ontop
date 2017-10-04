package it.unibz.inf.ontop.model.type.impl;


import it.unibz.inf.ontop.model.type.COL_TYPE;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;
import org.eclipse.rdf4j.model.IRI;

/**
 * TODO: create constructors without col-types for the abstract datatypes
 */
public class NumericRDFDatatype extends SimpleRDFDatatype {

    protected NumericRDFDatatype(COL_TYPE colType, TermTypeAncestry parentAncestry, boolean isAbstract, IRI datatypeIRI) {
        super(colType, parentAncestry, isAbstract, datatypeIRI);
    }

    protected NumericRDFDatatype(IRI datatypeIRI, TermTypeAncestry parentAncestry, boolean isAbstract) {
        super(datatypeIRI, parentAncestry, isAbstract);
    }

    static RDFDatatype createNumericTermType(IRI datatypeIRI, TermTypeAncestry parentAncestry, boolean isAbstract) {
        return new NumericRDFDatatype(datatypeIRI, parentAncestry, isAbstract);
    }

    static RDFDatatype createNumericTermType(IRI datatypeIRI, TermTypeAncestry parentAncestry, COL_TYPE colType) {
        return new NumericRDFDatatype(colType, parentAncestry, false, datatypeIRI);
    }

}