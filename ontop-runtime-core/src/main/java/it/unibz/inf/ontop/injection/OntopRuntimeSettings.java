package it.unibz.inf.ontop.injection;


public interface OntopRuntimeSettings extends OntopOBDASettings, OntopOptimizationSettings {

    boolean isRewritingEnabled();

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

    String  REWRITE 	= "ontop.rewrite";
}
