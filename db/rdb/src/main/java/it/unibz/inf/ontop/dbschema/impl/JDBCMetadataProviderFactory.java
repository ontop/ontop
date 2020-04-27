package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.sql.*;

public interface JDBCMetadataProviderFactory {

	DBMetadataProvider getMetadataProvider(Connection connection) throws MetadataExtractionException;
}
