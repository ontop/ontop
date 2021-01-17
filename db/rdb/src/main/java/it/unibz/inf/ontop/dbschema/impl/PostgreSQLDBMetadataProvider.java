package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.Connection;

public class PostgreSQLDBMetadataProvider extends DefaultSchemaDBMetadataProvider {

    @AssistedInject
    PostgreSQLDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, metadata -> new PostgreSQLQuotedIDFactory(), coreSingletons);
                // current_catalog AS TABLE_CAT
                //"SELECT current_schema AS TABLE_SCHEM");
        // https://www.postgresql.org/docs/current/functions-info.html
        // CAREFUL: PostgreSQL uses a chain of schemas and goes through the list until it finds the relevant object
        // https://www.postgresql.org/docs/current/ddl-schemas.html
        // If you write a database name, it must be the same as the database you are connected to.
    }
}
