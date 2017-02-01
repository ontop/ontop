package it.unibz.inf.ontop.injection;


public interface OntopQueryAnsweringSettings extends OntopOBDASettings, OntopOptimizationSettings {

    boolean isExistentialReasoningEnabled();

    boolean isDistinctPostProcessingEnabled();

    /**
     * In the case of SQL, inserts REPLACE functions in the generated query
     */
    boolean isIRISafeEncodingEnabled();


    //--------------------------
    // Keys
    //--------------------------

    String SQL_GENERATE_REPLACE = "org.obda.owlreformulationplatform.sqlGenerateReplace";
    String DISTINCT_RESULTSET = "org.obda.owlreformulationplatform.distinctResultSet";

    String EXISTENTIAL_REASONING = "ontop.existentialReasoning";
}
