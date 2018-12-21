package it.unibz.inf.ontop.injection;


public interface OntopReformulationSettings extends OntopOBDASettings, OntopOptimizationSettings {

    boolean isExistentialReasoningEnabled();

    boolean isDistinctPostProcessingEnabled();


    //--------------------------
    // Keys
    //--------------------------

    String EXISTENTIAL_REASONING = "ontop.existentialReasoning";
    String DISTINCT_RESULTSET = "ontop.distinctResultSet";
}
