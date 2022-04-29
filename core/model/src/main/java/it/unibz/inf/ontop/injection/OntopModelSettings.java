package it.unibz.inf.ontop.injection;

import java.util.Optional;

/**
 * TODO: explain
 */
public interface OntopModelSettings {


    CardinalityPreservationMode getCardinalityPreservationMode();

    boolean isTestModeEnabled();

    /**
     * If true, no Slice Node optimizations are present
     * If false, Slice Node optimizations are present to speed up Slice Queries with Values Node
     */
    boolean isLimitOptimizationDisabled();

    /**
     * Not for end-users!
     *
     * Please avoid using that class.
     */
    Optional<String> getProperty(String key);

    boolean contains(Object key);

    enum CardinalityPreservationMode {
        /**
         * Cardinality is not important and may not be respected
         * (allows to optimize more)
         */
        LOOSE,
//        /**
//         * Cardinality is preserved in case a cardinality-sensitive
//         * aggregation function is detected.
//         */
//        STRICT_FOR_AGGREGATION,
        /**
         * Cardinality is strictly preserved
         */
        STRICT
    }

    //-------
    // Keys
    //-------

    String CARDINALITY_MODE = "ontop.cardinalityMode";
    String TEST_MODE = "ontop.testMode";
    String DISABLE_LIMIT_OPTIMIZATION = "ontop.disableLimitOptimization";
}
