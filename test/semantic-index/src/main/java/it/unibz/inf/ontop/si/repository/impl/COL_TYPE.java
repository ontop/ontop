package it.unibz.inf.ontop.si.repository.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.vocabulary.OntopInternal;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public enum COL_TYPE {

    UNSUPPORTED(-1, "UNSUPPORTED", OntopInternal.UNSUPPORTED), // created only in SesameRDFIterator, ignored by SI and exceptions in all other cases
    NULL(0, "NULL", null),
    OBJECT(1, "OBJECT", null),
    BNODE(2, "BNODE", null),
    LANG_STRING(-3, "LANG_STRING", RDF.LANGSTRING), // not to be mapped from code // BC: Why not?
    INTEGER(4, "INTEGER", XSD.INTEGER),
    DECIMAL(5, "DECIMAL", XSD.DECIMAL),
    DOUBLE(6, "DOUBLE", XSD.DOUBLE),
    STRING(7, "STRING", XSD.STRING),
    DATETIME(8, "DATETIME", XSD.DATETIME),
    BOOLEAN(9, "BOOLEAN", XSD.BOOLEAN),
    DATE(10, "DATE", XSD.DATE),
    TIME(11, "TIME", XSD.TIME),
    YEAR(12, "YEAR", XSD.GYEAR),
    LONG(13, "LONG", XSD.LONG),
    FLOAT(14, "FLOAT", XSD.FLOAT),
    NEGATIVE_INTEGER(15, "NEGATIVE_INTEGER", XSD.NEGATIVE_INTEGER),
    NON_NEGATIVE_INTEGER(16, "NON_NEGATIVE_INTEGER", XSD.NON_NEGATIVE_INTEGER),
    POSITIVE_INTEGER(17, "POSITIVE_INTEGER", XSD.POSITIVE_INTEGER),
    NON_POSITIVE_INTEGER(18, "NON_POSITIVE_INTEGER", XSD.NON_POSITIVE_INTEGER),
    INT(19, "INT", XSD.INT),
    UNSIGNED_INT(20, "UNSIGNED_INT", XSD.UNSIGNED_INT),
    DATETIME_STAMP(21, "DATETIME_STAMP", XSD.DATETIMESTAMP),
    WKT(22, "WKT", new SimpleRDF().createIRI("http://www.opengis.net/ont/geosparql#wktLiteral"));

    private static final ImmutableMap<Integer, COL_TYPE> CODE_TO_TYPE_MAP;
    private static final ImmutableMap<IRI, COL_TYPE> IRI_TO_COL_TYPE_MAP;

    static {
        ImmutableMap.Builder<Integer, COL_TYPE> mapBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<IRI, COL_TYPE> iriMapBuilder = ImmutableMap.builder();
        for (COL_TYPE type : COL_TYPE.values()) {
            // ignore UNSUPPORTED (but not LITERAL_LANG anymore)
            if (type.code != -1)
                mapBuilder.put(type.code, type);
            type.getIri()
                    .ifPresent(iri -> iriMapBuilder.put(iri, type));
        }
        CODE_TO_TYPE_MAP = mapBuilder.build();
        IRI_TO_COL_TYPE_MAP = iriMapBuilder.build();
    }

    private final int code;
    private final String label;
    @Nullable
    private final IRI iri;

    // private constructor
    private COL_TYPE(int code, String label, @Nullable IRI iri) {
        this.code = code;
        this.label = label;
        this.iri = iri;
    }

    public int getQuestCode() {
        return code;
    }

    @Override
    public String toString() {
        return label;
    }

    public Optional<IRI> getIri() {
        return Optional.ofNullable(iri);
    }

    public static COL_TYPE getQuestType(int code) {
        return CODE_TO_TYPE_MAP.get(code);
    }

    public static COL_TYPE getColType(@Nonnull IRI datatypeIRI) {
        return IRI_TO_COL_TYPE_MAP.getOrDefault(datatypeIRI, UNSUPPORTED);
    }
}
