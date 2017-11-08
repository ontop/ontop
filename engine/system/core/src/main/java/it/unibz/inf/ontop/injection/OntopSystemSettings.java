package it.unibz.inf.ontop.injection;


public interface OntopSystemSettings extends OntopReformulationSettings {

    /**
     * Needed by some in-memory DBs
     *  (e.g. an H2 DB storing a semantic index)
     */
    boolean isPermanentDBConnectionEnabled();

    //--------------------------
    // Keys
    //--------------------------

    String PERMANENT_DB_CONNECTION = "ontop.permanentConnection";


}
