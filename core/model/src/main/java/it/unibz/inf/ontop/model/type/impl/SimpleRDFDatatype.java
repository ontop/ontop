package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.LanguageTag;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

/**
 * Without a language tag
 */
public class SimpleRDFDatatype extends AbstractRDFDatatype {

    protected SimpleRDFDatatype(IRI datatypeIRI, TermTypeAncestry parentAncestry, boolean isAbstract) {
        super(datatypeIRI, parentAncestry, isAbstract);
    }

    static RDFDatatype createSimpleRDFDatatype(IRI datatypeIRI, TermTypeAncestry parentAncestry, boolean isAbstract) {
        return new SimpleRDFDatatype(datatypeIRI, parentAncestry, isAbstract);
    }

    static RDFDatatype createSimpleRDFDatatype(IRI datatypeIRI, TermTypeAncestry parentAncestry) {
        return new SimpleRDFDatatype(datatypeIRI, parentAncestry, false);
    }

    @Override
    public Optional<LanguageTag> getLanguageTag() {
        return Optional.empty();
    }
}
