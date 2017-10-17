package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.COL_TYPE;
import it.unibz.inf.ontop.model.type.LanguageTag;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

/**
 * Without a language tag
 */
public class SimpleRDFDatatype extends AbstractRDFDatatype {

    protected SimpleRDFDatatype(COL_TYPE colType, TermTypeAncestry parentAncestry, boolean isAbstract,
                                IRI datatypeIRI) {
        super(colType, parentAncestry, isAbstract, datatypeIRI);
        if (colType == COL_TYPE.LANG_STRING)
            throw new IllegalArgumentException("A lang string is must have a language tag");
    }

    protected SimpleRDFDatatype(IRI datatypeIRI, TermTypeAncestry parentAncestry, boolean isAbstract) {
        super(datatypeIRI, parentAncestry, isAbstract);
    }

    static RDFDatatype createSimpleRDFDatatype(IRI datatypeIRI, TermTypeAncestry parentAncestry, boolean isAbstract) {
        return new SimpleRDFDatatype(datatypeIRI, parentAncestry, isAbstract);
    }

    static RDFDatatype createSimpleRDFDatatype(IRI datatypeIRI, TermTypeAncestry parentAncestry, boolean isAbstract,
                                               COL_TYPE colType) {
        return new SimpleRDFDatatype(colType, parentAncestry, isAbstract, datatypeIRI);
    }

    static RDFDatatype createSimpleRDFDatatype(IRI datatypeIRI, TermTypeAncestry parentAncestry, COL_TYPE colType) {
        return new SimpleRDFDatatype(colType, parentAncestry, false, datatypeIRI);
    }

    @Override
    public Optional<LanguageTag> getLanguageTag() {
        return Optional.empty();
    }
}
