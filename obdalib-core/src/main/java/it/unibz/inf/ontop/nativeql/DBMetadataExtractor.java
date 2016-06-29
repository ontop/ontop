package it.unibz.inf.ontop.nativeql;

import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.DataSourceMetadata;

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

    DataSourceMetadata extract(OBDADataSource dataSource, OBDAModel model, @Nullable DBConnectionWrapper dbConnection) throws DBMetadataException;

    DataSourceMetadata extract(OBDADataSource dataSource, OBDAModel model, @Nullable DBConnectionWrapper dbConnection,
                               DataSourceMetadata partiallyDefinedMetadata) throws DBMetadataException;
}
