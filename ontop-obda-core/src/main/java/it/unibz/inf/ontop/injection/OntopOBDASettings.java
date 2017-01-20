package it.unibz.inf.ontop.injection;


public interface OntopOBDASettings extends OntopModelSettings {

    boolean isEquivalenceOptimizationEnabled();

    boolean isSameAsInMappingsEnabled();

    //--------------------------
    // Keys
    //--------------------------

    String  SAME_AS = "ontop.sameAs";
    String  OPTIMIZE_EQUIVALENCES 	= "ontop.optimizeEquivalences";
}
