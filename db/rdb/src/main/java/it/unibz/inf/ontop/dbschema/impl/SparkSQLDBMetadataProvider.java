package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;

import java.sql.Connection;

public class SparkSQLDBMetadataProvider extends DefaultSchemaDBMetadataProvider {

    @AssistedInject
    SparkSQLDBMetadataProvider(@Assisted Connection connection, CoreSingletons coreSingletons) throws MetadataExtractionException {
        super(connection, metadata -> new SparkSQLQuotedIDFactory(), coreSingletons);
    }
}
