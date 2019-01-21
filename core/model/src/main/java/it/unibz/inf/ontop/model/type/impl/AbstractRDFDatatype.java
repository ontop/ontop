package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.apache.commons.rdf.api.IRI;

import java.util.function.Function;

public abstract class AbstractRDFDatatype extends RDFTermTypeImpl implements RDFDatatype {

    private final IRI datatypeIRI;

    /**
     * Concrete
     */
    protected AbstractRDFDatatype(IRI datatypeIRI, TermTypeAncestry parentAncestry,
                                  Function<DBTypeFactory, DBTermType> closestDBTypeFct) {
        super(extractIRIString(datatypeIRI), parentAncestry, closestDBTypeFct);
        this.datatypeIRI = datatypeIRI;
    }

    /**
     * Abstract
     */
    protected AbstractRDFDatatype(IRI datatypeIRI, TermTypeAncestry parentAncestry) {
        super(extractIRIString(datatypeIRI), parentAncestry);
        this.datatypeIRI = datatypeIRI;
    }

    private static String extractIRIString(IRI datatypeIRI) {
        String iriString = datatypeIRI.getIRIString();
        if (iriString.startsWith(XSD.PREFIX))
            return "xsd:" + iriString.substring(XSD.PREFIX.length());
        else if (iriString.startsWith(RDFS.PREFIX))
            return "rdfs:" + iriString.substring(RDFS.PREFIX.length());
        return iriString;
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
