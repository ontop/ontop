package it.unibz.inf.ontop.spec.dbschema.tools.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.ImmutableMetadata;
import it.unibz.inf.ontop.dbschema.MetadataProvider;
import it.unibz.inf.ontop.dbschema.impl.JDBCMetadataProviderFactory;
import it.unibz.inf.ontop.dbschema.impl.json.JsonMetadata;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.spec.dbschema.tools.DBMetadataExtractorAndSerializer;
import it.unibz.inf.ontop.utils.LocalJDBCConnectionUtils;

import java.sql.Connection;
import java.sql.SQLException;

public class RDBMetadataExtractorAndSerializerImpl implements DBMetadataExtractorAndSerializer {

    private final OntopSQLCredentialSettings settings;
    private final JDBCMetadataProviderFactory metadataProviderFactory;

    @Inject
    private RDBMetadataExtractorAndSerializerImpl(OntopSQLCredentialSettings settings,
                                                  JDBCMetadataProviderFactory metadataProviderFactory) {
        this.settings = settings;
        this.metadataProviderFactory = metadataProviderFactory;
    }

    @Override
    public String extractAndSerialize() throws MetadataExtractionException {

        try (Connection localConnection = LocalJDBCConnectionUtils.createConnection(settings)) {
            MetadataProvider metadataProvider = metadataProviderFactory.getMetadataProvider(localConnection);
            ImmutableMetadata metadata = ImmutableMetadata.extractImmutableMetadata(metadataProvider);

            ObjectMapper mapper = new ObjectMapper();
            JsonMetadata jsonMetadata = new JsonMetadata(metadata);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMetadata);
        }
        catch (SQLException e) {
            throw new MetadataExtractionException("Connection problem while extracting the metadata.\n" + e);
        }
        catch (JsonProcessingException e) {
            throw new MetadataExtractionException("problem with JSON processing.\n" + e);
        }
    }
}
