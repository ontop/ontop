package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.dbschema.impl.JDBCMetadataProviderFactory;

/**
 * Extracts DB metadata directly from the DB through a connection
 *
 * See {@link JDBCMetadataProviderFactory} for instantiating them
 *
 */
public interface DBMetadataProvider extends MetadataProvider {
}
