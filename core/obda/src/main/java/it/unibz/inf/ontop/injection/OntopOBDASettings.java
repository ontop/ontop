package it.unibz.inf.ontop.injection;


public interface OntopOBDASettings extends OntopModelSettings {

    boolean isSameAsInMappingsEnabled();

    //--------------------------
    // Keys
    //--------------------------

    String  SAME_AS = "ontop.sameAs";
}
