package it.unibz.inf.ontop.injection;


public interface OntopReformulationSettings extends OntopOBDASettings, OntopOptimizationSettings {

    boolean isExistentialReasoningEnabled();

    /**
     * In the case of SQL, inserts REPLACE functions in the generated query
     */
    boolean isIRISafeEncodingEnabled();

    boolean isDistinctPostProcessingEnabled();


    //--------------------------
    // Keys
    //--------------------------

    String SQL_GENERATE_REPLACE = "ontop.iriSafeEncoding";
    String EXISTENTIAL_REASONING = "ontop.existentialReasoning";
    String DISTINCT_RESULTSET = "ontop.distinctResultSet";
}
