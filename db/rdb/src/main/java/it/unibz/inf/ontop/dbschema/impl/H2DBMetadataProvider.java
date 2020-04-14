package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

import java.sql.Connection;

public class H2DBMetadataProvider extends  DefaultDBMetadataProvider {

    private final QuotedID defaultSchema;

    H2DBMetadataProvider(Connection connection, DBTypeFactory dbTypeFactory) throws MetadataExtractionException {
        super(connection, dbTypeFactory);
        // http://www.h2database.com/html/functions.html#current_schema
        defaultSchema = retrieveDefaultSchema("SELECT SCHEMA()");
    }

    @Override
    public QuotedID getDefaultSchema() { return defaultSchema; }
}
