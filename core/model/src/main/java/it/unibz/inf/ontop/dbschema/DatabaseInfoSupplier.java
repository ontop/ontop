package it.unibz.inf.ontop.dbschema;

import java.util.Optional;

/**
 * Carries information that is not yet available at the moment where the dependency-injection framework is initialized.
 */
public interface DatabaseInfoSupplier {

    Optional<String> getDatabaseVersion();

    /**
     * Can only be set once
     */
    void setDatabaseVersion(String version) throws IllegalStateException;
}
