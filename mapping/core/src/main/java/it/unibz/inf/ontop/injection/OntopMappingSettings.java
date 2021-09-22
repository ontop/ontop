package it.unibz.inf.ontop.injection;


public interface OntopMappingSettings extends OntopOBDASettings, OntopOptimizationSettings {

    boolean isOntologyAnnotationQueryingEnabled();

    /**
     * If false, throws an exception if the system is not able to infer the datatype from the database
     * If true use default datatype (xsd:string)
     */
    boolean isDefaultDatatypeInferred();

    /**
     * If false, throws an exception is an abstract datatype is used for a literal in a mapping assertion.
     * If true, abstract datatypes will be replaced by concrete ones.
     */
    boolean areAbstractDatatypesToleratedInMapping();

    /**
     * If true, metadata about the black-box view will be retrieved by querying the DB.
     */
    boolean allowRetrievingBlackBoxViewMetadataFromDB();


    /**
     * Let S be the data source, and if M is a set of mapping assertions, let M(S) be the graph derived by applying M to S (without ontology).
     * And let dom(M(S)) (resp. range(M(S))) be all subjects (resp. objects) of some triple in M(S).
     *
     * Now let C be all mapping assertions with isCanonicalIRIOf as predicate,
     * and let A_sub (resp(A_obj)) be all mapping assertions whose subject (resp. object) is built with a URI template, and whose predicate is not isCanonicalIRIOf.
     *
     * If this parameter is set to true, then for any a in A_sub,
     * either dom({a}(S)) \cap range(C(S)) = \emptyset,
     * or dom({a}(S)) \subseteq range(C(S))).
     *
     * Similarly, for any a in A_obj,
     * either range({a}(S)) \cap range(C(S)) = \emptyset,
     * or range({a}(S)) \subseteq range(C(S))).
     */
    boolean isCanIRIComplete();

    /**
     * If false, use Union Node instead of Values Node
     * If true use Values Node
     */
    boolean isValuesNodeEnabled();

    //--------------------------
    // Keys
    //--------------------------

    String QUERY_ONTOLOGY_ANNOTATIONS = "ontop.queryOntologyAnnotation";
    String INFER_DEFAULT_DATATYPE = "ontop.inferDefaultDatatype";
    String TOLERATE_ABSTRACT_DATATYPE = "ontop.tolerateAbstractDatatype";
    String IS_CANONICAL_IRI_COMPLETE = "ontop.isCanonicalIRIComplete";
    String ENABLE_VALUES_NODE = "ontop.enableValuesNode";
    String ALLOW_RETRIEVING_BLACK_BOX_VIEW_METADATA_FROM_DB = "ontop.allowRetrievingBlackBoxViewMetadataFromDB";


    /**
     * Options to specify base IRI.
     *
     * @see <a href="http://www.w3.org/TR/r2rml/#dfn-base-iri">Base IRI</a>
     */
    String  BASE_IRI             	= "mapping.baseIri";

}
