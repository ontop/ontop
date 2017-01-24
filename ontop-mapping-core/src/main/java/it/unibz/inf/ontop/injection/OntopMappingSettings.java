package it.unibz.inf.ontop.injection;


public interface OntopMappingSettings extends OntopOBDASettings, OntopOptimizationSettings {

    boolean isOntologyAnnotationQueryingEnabled();

    boolean isFullMetadataExtractionEnabled();

    //--------------------------
    // Keys
    //--------------------------

    String OBTAIN_FULL_METADATA = "ontop.fullMetadataExtraction";
    String QUERY_ONTOLOGY_ANNOTATIONS = "ontop.queryOntologyAnnotation";

}
