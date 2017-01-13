package it.unibz.inf.ontop.injection;

import java.util.Optional;

/**
 * General properties.
 *
 * Focuses on implementation class declaration
 * for the core module of Ontop.
 *
 * Validation is not done at construction time but on demand.
 *
 * Immutable!
 *
 * TODO: update this description
 *
 */
public interface OBDASettings extends OntopSQLSettings {

    //-------------------
    // High-level methods
    //-------------------

    Optional<String> getMappingFilePath();

    boolean isFullMetadataExtractionEnabled();


    String OBTAIN_FULL_METADATA = "OBTAIN_FULL_METADATA";

    // String DB_CONSTRAINTS = "DB_CONSTRAINTS";

    String MAPPING_FILE_PATH = "MAPPING_FILE_PATH";

}
