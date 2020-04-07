package it.unibz.inf.ontop.spec.dbschema.tools.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.BasicDBMetadata;
import it.unibz.inf.ontop.dbschema.ImmutableDBMetadata;
import it.unibz.inf.ontop.dbschema.RDBMetadataExtractionTools;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.dbschema.tools.DBMetadataExtractorAndSerializer;
import it.unibz.inf.ontop.utils.LocalJDBCConnectionUtils;

import java.sql.Connection;
import java.sql.SQLException;

public class RDBMetadataExtractorAndSerializerImpl implements DBMetadataExtractorAndSerializer {

    private final OntopSQLCredentialSettings settings;
    private final TypeFactory typeFactory;

    @Inject
    private RDBMetadataExtractorAndSerializerImpl(OntopSQLCredentialSettings settings, TypeFactory typeFactory) {
        this.settings = settings;
        this.typeFactory = typeFactory;
    }

    @Override
    public String extractAndSerialize() throws MetadataExtractionException {

        try (Connection localConnection = LocalJDBCConnectionUtils.createConnection(settings)) {
            ImmutableDBMetadata metadata = RDBMetadataExtractionTools.loadFullMetadata(localConnection, typeFactory.getDBTypeFactory());

            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(metadata);
            return jsonString;

        } catch (SQLException e) {
            throw new MetadataExtractionException("Connection problem while extracting the metadata.\n" + e);
        } catch (JsonProcessingException e) {
            throw new MetadataExtractionException("problem with JSON processing.\n" + e);
        }
    }
}
