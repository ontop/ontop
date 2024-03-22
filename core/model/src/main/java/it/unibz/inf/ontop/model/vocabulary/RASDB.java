package it.unibz.inf.ontop.model.vocabulary;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

public class RASDB {
    //TODO
    // Add raster functions like GEOF
    // Use the vocabulary
    // ------------------------------------[STEP 01]------------------------------------------
    public static final String PREFIX = "http://www.semanticweb.org/RasterDataCube/";

    // Prefix for properties
    public static final String PREFIX_PROP = "http://www.semanticweb.org/RasterDataCube/";

    // Simple Feature functions
    public static final IRI SF_EQUALS;

    // Simple Raster Functions
    public static final IRI RAS_CONNECTION;

    public static final IRI RAS_GET_META;

    // Raster Aggregation Functions
    public static final IRI RAS_SPATIAL_AVERAGE;

    public static final IRI RAS_SPATIAL_MAXIMUM;

    public static final IRI RAS_SPATIAL_MINIMUM;

    public static final IRI RAS_SPATIAL_TEMPORAL_AVERAGE;

    // Raster Filter Array Functions
    public static final IRI RAS_CLIP_RASTER_SPATIAL;

    static {
        org.apache.commons.rdf.api.RDF factory = new SimpleRDF();

        // Simple Feature functions
        SF_EQUALS = factory.createIRI(PREFIX + "sfEquals");

        // Simple Raster Functions
        RAS_CONNECTION = factory.createIRI(PREFIX + "rasConnection");

        RAS_GET_META = factory.createIRI(PREFIX + "rasGetMeta");


        // Raster Aggregation Functions

        RAS_SPATIAL_AVERAGE = factory.createIRI(PREFIX + "rasSpatialAverage");

        RAS_SPATIAL_MAXIMUM = factory.createIRI(PREFIX + "rasSpatialMaximum");

        RAS_SPATIAL_MINIMUM = factory.createIRI(PREFIX + "rasSpatialMinimum");

        RAS_SPATIAL_TEMPORAL_AVERAGE = factory.createIRI(PREFIX + "rasSpatialTemporalAverage");


        // Raster Filter Array Functions

        RAS_CLIP_RASTER_SPATIAL = factory.createIRI(PREFIX + "rasClipRaster");

    }
}
