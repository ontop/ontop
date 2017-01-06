package it.unibz.inf.ontop.injection;

import java.util.Optional;

/**
 * TODO: explain
 */
public interface OntopModelProperties {


    CardinalityPreservationMode getCardinalityPreservationMode();


    //-------------------
    // Low-level methods
    //-------------------

    Optional<Boolean> getBoolean(String key);
    boolean getRequiredBoolean(String key);

    Optional<Integer> getInteger(String key);
    int getRequiredInteger(String key);

    Optional<String> getProperty(String key);
    String getRequiredProperty(String key);

    boolean contains(Object key);

    enum CardinalityPreservationMode {
        /**
         * Cardinality is not important and may not be respected
         * (allows to optimize more)
         */
        LOOSE,
        /**
         * Cardinality is preserved in case a cardinality-sensitive
         * aggregation function is detected.
         */
        STRICT_FOR_AGGREGATION,
        /**
         * Cardinality is strictly preserved
         */
        STRICT
    }

    //-------
    // Keys
    //-------

    String CARDINALITY_MODE = "CARDINALITY_MODE";
}
