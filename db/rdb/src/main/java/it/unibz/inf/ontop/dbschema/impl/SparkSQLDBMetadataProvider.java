package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class SparkSQLDBMetadataProvider extends  DefaultDBMetadataProvider{

    private final QuotedID defaultSchema;
    private final DatabaseMetaData metadata;

    @AssistedInject
    SparkSQLDBMetadataProvider(@Assisted Connection connection, TypeFactory typeFactory) throws MetadataExtractionException {
        super(connection, getQuotedIDFactory(connection), typeFactory);
        try {
            this.metadata = connection.getMetaData();
            defaultSchema = retrieveDefaultSchema("SHOW SCHEMAS");
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    protected static QuotedIDFactory getQuotedIDFactory(Connection connection) throws MetadataExtractionException {
        try {
            DatabaseMetaData md = connection.getMetaData();
            return new SparkSQLQuotedIDFactory(false);
        }catch (SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    @Override
    public QuotedID getDefaultSchema() { return defaultSchema; }
}
