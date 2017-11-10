package it.unibz.inf.ontop.injection;


public interface OntopMappingSettings extends OntopOBDASettings, OntopOptimizationSettings {

    boolean isOntologyAnnotationQueryingEnabled();

    boolean isFullMetadataExtractionEnabled();

    /**
     * Completes user-provided DBMetadata
     */
    boolean isProvidedDBMetadataCompletionEnabled();

    /**
     * If false, throws an exception if the system is not able to infer the datatype from the database
     * If true use default datatype (xsd:string)
     */
    boolean isDefaultDatatypeInferred();

    //--------------------------
    // Keys
    //--------------------------

    String OBTAIN_FULL_METADATA = "ontop.fullMetadataExtraction";
    String QUERY_ONTOLOGY_ANNOTATIONS = "ontop.queryOntologyAnnotation";
    String COMPLETE_PROVIDED_METADATA = "ontop.completeProvidedMetadata";
    String INFER_DEFAULT_DATATYPE = "ontop.inferDefaultDatatype";

    /**
     * Options to specify base IRI.
     *
     * @see <a href="http://www.w3.org/TR/r2rml/#dfn-base-iri">Base IRI</a>
     */
    String  BASE_IRI             	= "mapping.baseIri";

}
