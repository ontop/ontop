package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;
import org.apache.commons.rdf.api.IRI;

public abstract class AbstractRDFDatatype extends RDFTermTypeImpl implements RDFDatatype {

    private final IRI datatypeIRI;

    protected AbstractRDFDatatype(IRI datatypeIRI, TermTypeAncestry parentAncestry, boolean isAbstract) {
        super(datatypeIRI.getIRIString(), parentAncestry, isAbstract);
        this.datatypeIRI = datatypeIRI;
    }

    @Override
    public boolean isA(IRI baseDatatypeIri) {
        return getAncestry().getTermTypes()
                .filter(t -> t instanceof  RDFDatatype)
                .map(t -> ((RDFDatatype)t).getIRI())
                .anyMatch(baseDatatypeIri::equals);
    }

    @Override
    public IRI getIRI() {
        return datatypeIRI;
    }
}
