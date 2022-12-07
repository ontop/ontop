package it.unibz.inf.ontop.si.repository.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.vocabulary.OntopInternal;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

public enum COL_TYPE {

    UNSUPPORTED("UNSUPPORTED", OntopInternal.UNSUPPORTED), // created only in SesameRDFIterator, ignored by SI and exceptions in all other cases
    NULL("NULL", null),
    OBJECT("OBJECT", null),
    BNODE("BNODE", null),
    LANG_STRING("LANG_STRING", RDF.LANGSTRING),
    INTEGER("INTEGER", XSD.INTEGER),
    DECIMAL("DECIMAL", XSD.DECIMAL),
    DOUBLE("DOUBLE", XSD.DOUBLE),
    STRING("STRING", XSD.STRING),
    DATETIME("DATETIME", XSD.DATETIME),
    BOOLEAN("BOOLEAN", XSD.BOOLEAN),
    DATE("DATE", XSD.DATE),
    TIME("TIME", XSD.TIME),
    YEAR("YEAR", XSD.GYEAR),
    LONG("LONG", XSD.LONG),
    FLOAT("FLOAT", XSD.FLOAT),
    NEGATIVE_INTEGER("NEGATIVE_INTEGER", XSD.NEGATIVE_INTEGER),
    NON_NEGATIVE_INTEGER("NON_NEGATIVE_INTEGER", XSD.NON_NEGATIVE_INTEGER),
    POSITIVE_INTEGER("POSITIVE_INTEGER", XSD.POSITIVE_INTEGER),
    NON_POSITIVE_INTEGER("NON_POSITIVE_INTEGER", XSD.NON_POSITIVE_INTEGER),
    INT("INT", XSD.INT),
    UNSIGNED_INT("UNSIGNED_INT", XSD.UNSIGNED_INT),
    DATETIME_STAMP("DATETIME_STAMP", XSD.DATETIMESTAMP),
    WKT("WKT", new SimpleRDF().createIRI("http://www.opengis.net/ont/geosparql#wktLiteral"));

    private static final ImmutableMap<IRI, COL_TYPE> IRI_TO_COL_TYPE_MAP;

    static {
        IRI_TO_COL_TYPE_MAP = Arrays.stream(COL_TYPE.values())
                .filter(c -> c.getIri().isPresent())
                .collect(ImmutableCollectors.toMap(c -> c.getIri().get(), c -> c));
    }

    private final String label;
    @Nullable
    private final IRI iri;

    COL_TYPE(String label, @Nullable IRI iri) {
        this.label = label;
        this.iri = iri;
    }

    @Override
    public String toString() {
        return label;
    }

    public Optional<IRI> getIri() {
        return Optional.ofNullable(iri);
    }

    public static COL_TYPE getColType(@Nonnull IRI datatypeIRI) {
        return IRI_TO_COL_TYPE_MAP.getOrDefault(datatypeIRI, UNSUPPORTED);
    }
}
