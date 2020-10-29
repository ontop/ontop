package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.Connection;

public class PostgreSQLDBMetadataProvider extends DefaultDBMetadataProvider {

    @AssistedInject
    PostgreSQLDBMetadataProvider(@Assisted Connection connection, TypeFactory typeFactory) throws MetadataExtractionException {
        super(connection, "SELECT current_schema", typeFactory);
        // https://www.postgresql.org/docs/9.3/functions-info.html
        // CAREFUL: PostgreSQL uses a chain of schemas and goes through the list until it finds the relevant object
        // https://www.postgresql.org/docs/current/ddl-schemas.html
    }
}
