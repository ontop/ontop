package org.semanticweb.ontop.nativeql;

import com.sun.istack.internal.Nullable;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.ImplicitDBConstraints;

import java.sql.Connection;

/**
 * Extracts the metadata of the database by:
 *  (i) Connecting to it
 *  (ii) Analyzing the mappings
 *  (iii) Considering other DB constraints given by the user.
 *
 * This interface aims at being generic regarding the native query language.
 *
 * TODO: See if having SQL connection is not a problem.
 */
public interface DBMetadataExtractor {

    DBMetadata extract(OBDADataSource dataSource, Connection dbConnection, OBDAModel model,
                       @Nullable ImplicitDBConstraints userConstraints) throws DBMetadataException;
}
