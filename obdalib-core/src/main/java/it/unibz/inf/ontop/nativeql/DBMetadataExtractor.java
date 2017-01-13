package it.unibz.inf.ontop.nativeql;

import it.unibz.inf.ontop.injection.OntopSQLSettings;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.DBMetadata;

import javax.annotation.Nullable;

/**
 * Extracts the metadata of the database by:
 *  (i) Connecting to it
 *  (ii) Analyzing the mappings
 *  (iii) Considering other DB constraints given by the user.
 *
 * This interface aims at being generic regarding the native query language.
 *
 */
public interface DBMetadataExtractor {

    DBMetadata extract(OBDAModel model, @Nullable DBConnectionWrapper dbConnection) throws DBMetadataException;

    DBMetadata extract(OBDAModel model, @Nullable DBConnectionWrapper dbConnection,
                       DBMetadata partiallyDefinedMetadata) throws DBMetadataException;
}
