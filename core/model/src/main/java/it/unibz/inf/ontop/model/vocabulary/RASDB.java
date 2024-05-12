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

    // Simple Raster Functions
    public static final IRI RAS_CONNECTION;

    public static final IRI RAS_DATE_TO_GRID;

    public static final IRI RAS_GET_DIMENSION;

    public static final IRI RAS_PROCESS_RASTER_ARRAY;



    // Raster Aggregation Functions

    public static final IRI RAS_SPATIAL_AVERAGE;

    public static final IRI RAS_SPATIAL_MAXIMUM;

    public static final IRI RAS_SPATIAL_MINIMUM;

    public static final IRI RAS_SPATIAL_MINIMUM_X;

    public static final IRI RAS_SPATIAL_MAXIMUM_X;

    public static final IRI RAS_SPATIAL_AVERAGE_X;

    public static final IRI RAS_SPATIAL_AVERAGE_FINAL;

    public static final IRI RAS_SPATIAL_TEMPORAL_AVERAGE;



    // Raster Filter Array Functions

    public static final IRI RAS_CLIP_SMALL_ARRAY_SPATIAL;

    public static final IRI RAS_CLIP_SMALL_ARRAY_TEMPORAL;

    public static final IRI RAS_CLIP_RASTER_SPATIAL;

    public static final IRI RAS_CLIP_RASTER_SPATIAL_ANY_GEOM;

    static {
        org.apache.commons.rdf.api.RDF factory = new SimpleRDF();

        // Simple Raster Functions
        RAS_CONNECTION = factory.createIRI(PREFIX + "rasConnection");

        RAS_DATE_TO_GRID = factory.createIRI(PREFIX + "rasDate2Grid");

        RAS_GET_DIMENSION = factory.createIRI(PREFIX + "rasDimension");

        RAS_PROCESS_RASTER_ARRAY = factory.createIRI(PREFIX + "rasProcessRasterOp");


        // Raster Aggregation Functions
        //TODO
        // Add raster functions like GEOF
        // Use the vocabulary
        // ------------------------------------[STEP 01a]------------------------------------------

        RAS_SPATIAL_AVERAGE = factory.createIRI(PREFIX + "rasSpatialAverage");

        RAS_SPATIAL_MAXIMUM = factory.createIRI(PREFIX + "rasSpatialMaximum");

        RAS_SPATIAL_MINIMUM = factory.createIRI(PREFIX + "rasSpatialMinimum");

        RAS_SPATIAL_MINIMUM_X = factory.createIRI(PREFIX + "rasSpatialMinimumX");

        RAS_SPATIAL_MAXIMUM_X = factory.createIRI(PREFIX + "rasSpatialMaximumX");

        RAS_SPATIAL_AVERAGE_X = factory.createIRI(PREFIX + "rasSpatialAverageX");

        RAS_SPATIAL_AVERAGE_FINAL = factory.createIRI(PREFIX + "rasSpatialAverageFINAL");

        RAS_SPATIAL_TEMPORAL_AVERAGE = factory.createIRI(PREFIX + "rasSpatialTemporalAverage");


        // Raster Filter Array Functions

        RAS_CLIP_RASTER_SPATIAL = factory.createIRI(PREFIX + "rasClipRaster");

        RAS_CLIP_RASTER_SPATIAL_ANY_GEOM = factory.createIRI(PREFIX + "rasClipRasterAnyGeom");

        RAS_CLIP_SMALL_ARRAY_SPATIAL = factory.createIRI(PREFIX + "rasSmallRasterArraySpatial");

        RAS_CLIP_SMALL_ARRAY_TEMPORAL = factory.createIRI(PREFIX + "rasSmallRasterArrayTemp");



    }
}
