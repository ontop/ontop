package it.unibz.inf.ontop.injection;

import java.util.Optional;

/**
 * TODO: explain
 */
public interface OntopModelProperties {

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
}
