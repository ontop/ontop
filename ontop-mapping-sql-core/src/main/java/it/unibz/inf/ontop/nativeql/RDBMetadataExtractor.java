package it.unibz.inf.ontop.nativeql;

import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.sql.RDBMetadata;

import javax.annotation.Nullable;
import java.io.File;
import java.sql.Connection;
import java.util.Optional;

/**
 * Extracts the metadata of the database by:
 *  (i) Connecting to it
 *  (ii) Analyzing the mappings
 *  (iii) Considering other DB constraints given by the user.
 *
 * This interface aims at being generic regarding the native query language.
 *
 */
public interface RDBMetadataExtractor {

    RDBMetadata extract(OBDAModel model, @Nullable Connection dbConnection, Optional<File> constraintFile)
            throws DBMetadataExtractionException;

    RDBMetadata extract(OBDAModel model, @Nullable Connection dbConnection,
                       DBMetadata partiallyDefinedMetadata, Optional<File> constraintFile)
            throws DBMetadataExtractionException;
}
