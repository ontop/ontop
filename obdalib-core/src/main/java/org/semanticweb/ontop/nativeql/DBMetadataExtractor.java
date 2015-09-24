package org.semanticweb.ontop.nativeql;

import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.DataSourceMetadata;
import org.semanticweb.ontop.sql.ImplicitDBConstraints;

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
}
