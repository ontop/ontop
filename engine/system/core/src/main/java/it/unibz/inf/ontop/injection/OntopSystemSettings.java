package it.unibz.inf.ontop.injection;


import java.util.Optional;

public interface OntopSystemSettings extends OntopReformulationSettings {

    /**
     * Query EVALUATION timeout (executed on the DB engine)
     *
     * Has no effect if negative or equal to 0.
     */
    Optional<Integer> getDefaultQueryTimeout();

    /**
     * Needed by some in-memory DBs
     *  (e.g. an H2 DB storing a semantic index)
     */
    boolean isPermanentDBConnectionEnabled();

    //--------------------------
    // Keys
    //--------------------------

    String DEFAULT_QUERY_TIMEOUT = "ontop.query.defaultTimeout";
    String PERMANENT_DB_CONNECTION = "ontop.permanentConnection";


}
