package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.*;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;
import java.util.function.Function;

/**
 * Without a language tag
 */
public class SimpleRDFDatatype extends AbstractRDFDatatype {

    /**
     * Concrete
     */
    protected SimpleRDFDatatype(IRI datatypeIRI, TermTypeAncestry parentAncestry,
                                Function<DBTypeFactory, DBTermType> closestDBTypeFct) {
        super(datatypeIRI, parentAncestry, closestDBTypeFct);
    }

    /**
     * Abstract
     */
    protected SimpleRDFDatatype(IRI datatypeIRI, TermTypeAncestry parentAncestry) {
        super(datatypeIRI, parentAncestry);
    }

    static RDFDatatype createSimpleConcreteRDFDatatype(IRI datatypeIRI, TermTypeAncestry parentAncestry,
                                                       Function<DBTypeFactory, DBTermType> closestDBTypeFct) {
        return new SimpleRDFDatatype(datatypeIRI, parentAncestry, closestDBTypeFct);
    }

    static RDFDatatype createSimpleAbstractRDFDatatype(IRI datatypeIRI, TermTypeAncestry parentAncestry) {
        return new SimpleRDFDatatype(datatypeIRI, parentAncestry);
    }

    @Override
    public Optional<LanguageTag> getLanguageTag() {
        return Optional.empty();
    }
}
