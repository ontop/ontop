package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.Connection;

public class H2DBMetadataProvider extends  DefaultDBMetadataProvider {

    @AssistedInject
    H2DBMetadataProvider(@Assisted Connection connection, TypeFactory typeFactory) throws MetadataExtractionException {
        super(connection, "SELECT SCHEMA()", typeFactory);
        // http://www.h2database.com/html/functions.html#current_schema
    }
}
