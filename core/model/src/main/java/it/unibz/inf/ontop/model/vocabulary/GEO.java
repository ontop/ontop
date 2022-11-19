package it.unibz.inf.ontop.model.vocabulary;

import com.google.common.collect.ImmutableList;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

public class GEO {
    public static final String PREFIX = "http://www.opengis.net/ont/geosparql#";
    public static final IRI GEO_WKT_LITERAL;
    public static final IRI FEATURE;
    public static final IRI GEOMETRY;
    public static final IRI HASDEFAULTGEOMETRY;
    public static final IRI GEO_AS_WKT;

    // Simple Feature functions
    public static final IRI SF_EQUALS;

    public static final IRI SF_DISJOINT;

    public static final IRI SF_INTERSECTS;

    public static final IRI SF_TOUCHES;

    public static final IRI SF_CROSSES;

    public static final IRI SF_WITHIN;

    public static final IRI SF_CONTAINS;

    public static final IRI SF_OVERLAPS;

    // Egenhofer functions
    public static final IRI EH_EQUALS;

    public static final IRI EH_DISJOINT;

    public static final IRI EH_MEET;

    public static final IRI EH_OVERLAP;

    public static final IRI EH_COVERS;

    public static final IRI EH_COVEREDBY;

    public static final IRI EH_INSIDE;

    public static final IRI EH_CONTAINS;

    // RCC8 functions
    public static final IRI RCC8_EQ;

    public static final IRI RCC8_DC;

    public static final IRI RCC8_EC;

    public static final IRI RCC8_PO;

    public static final IRI RCC8_TPPI;

    public static final IRI RCC8_TPP;

    public static final IRI RCC8_NTPP;

    public static final IRI RCC8_NTPPI;


    public static final ImmutableList<IRI> QUERY_REWRITE_FUNCTIONS;

    static {
        org.apache.commons.rdf.api.RDF factory = new SimpleRDF();
        GEO_WKT_LITERAL = factory.createIRI(GEO.PREFIX + "wktLiteral");
        // Possibly rename
        //http://www.opengis.net/ont/geosparql#Feature
        //http://www.opengis.net/ont/geosparql#Geometry
        //Make case insensitive???
        FEATURE = factory.createIRI(GEO.PREFIX + "Feature");
        GEOMETRY = factory.createIRI(GEO.PREFIX + "Geometry");
        HASDEFAULTGEOMETRY = factory.createIRI(GEO.PREFIX + "hasDefaultGeometry");
        GEO_AS_WKT = factory.createIRI(GEO.PREFIX + "asWKT"); // Duplicating with the datatype factor???

        // Simple Feature functions
        SF_EQUALS = factory.createIRI(PREFIX + "sfEquals");

        SF_DISJOINT = factory.createIRI(PREFIX + "sfDisjoint");

        SF_INTERSECTS = factory.createIRI(PREFIX + "sfIntersects");

        SF_TOUCHES = factory.createIRI(PREFIX + "sfTouches");

        SF_CROSSES = factory.createIRI(PREFIX + "sfCrosses");

        SF_WITHIN = factory.createIRI(PREFIX + "sfWithin");

        SF_CONTAINS = factory.createIRI(PREFIX + "sfContains");

        SF_OVERLAPS = factory.createIRI(PREFIX + "sfOverlaps");

        // Egenhofer functions
        EH_EQUALS = factory.createIRI(PREFIX + "ehEquals");

        EH_DISJOINT = factory.createIRI(PREFIX + "ehDisjoint");

        EH_MEET = factory.createIRI(PREFIX + "ehMeet");

        EH_OVERLAP = factory.createIRI(PREFIX + "ehOverlap");

        EH_COVERS = factory.createIRI(PREFIX + "ehCovers");

        EH_COVEREDBY = factory.createIRI(PREFIX + "ehCoveredBy");

        EH_INSIDE = factory.createIRI(PREFIX + "ehInside");

        EH_CONTAINS = factory.createIRI(PREFIX + "ehContains");

        // RCC8 functions
        RCC8_EQ = factory.createIRI(PREFIX + "rcc8eq");

        RCC8_DC = factory.createIRI(PREFIX + "rcc8dc");

        RCC8_EC = factory.createIRI(PREFIX + "rcc8ec");

        RCC8_PO = factory.createIRI(PREFIX + "rcc8po");

        RCC8_TPP = factory.createIRI(PREFIX + "rcc8tpp");

        RCC8_TPPI = factory.createIRI(PREFIX + "rcc8tppi");

        RCC8_NTPP = factory.createIRI(PREFIX + "rcc8ntpp");

        RCC8_NTPPI = factory.createIRI(PREFIX + "rcc8ntppi");

        // functions for which the query rewrite extension applies
        QUERY_REWRITE_FUNCTIONS = ImmutableList.of(GEO.SF_EQUALS, GEO.SF_DISJOINT, GEO.SF_INTERSECTS, GEO.SF_TOUCHES,
                GEO.SF_CROSSES, GEO.SF_WITHIN, GEO.SF_CONTAINS, GEO.SF_OVERLAPS, GEO.EH_EQUALS, GEO.EH_DISJOINT,
                GEO.EH_MEET, GEO.EH_OVERLAP, GEO.EH_COVERS, GEO.EH_COVEREDBY, GEO.EH_INSIDE, GEO.EH_CONTAINS,
                GEO.RCC8_EQ, GEO.RCC8_DC, GEO.RCC8_EC, GEO.RCC8_PO, GEO.RCC8_TPP, GEO.RCC8_TPPI, GEO.RCC8_NTPP,
                GEO.RCC8_NTPPI);

    }
}
