package it.unibz.inf.ontop.injection;


public interface OntopMappingSettings extends OntopOBDASettings, OntopOptimizationSettings {

    boolean isOntologyAnnotationQueryingEnabled();

    boolean isFullMetadataExtractionEnabled();

    /**
     * Completes user-provided DBMetadata
     */
    boolean isProvidedDBMetadataCompletionEnabled();

    //--------------------------
    // Keys
    //--------------------------

    String OBTAIN_FULL_METADATA = "ontop.fullMetadataExtraction";
    String QUERY_ONTOLOGY_ANNOTATIONS = "ontop.queryOntologyAnnotation";
    String COMPLETE_PROVIDED_METADATA = "ontop.completeProvidedMetadata";

    /**
     * Options to specify base IRI.
     *
     * @see <a href="http://www.w3.org/TR/r2rml/#dfn-base-iri">Base IRI</a>
     */
    String  BASE_IRI             	= "mapping.baseIri";

}
