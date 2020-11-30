package it.unibz.inf.ontop.spec.dbschema.tools.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.ImmutableMetadata;
import it.unibz.inf.ontop.dbschema.impl.BasicDBParametersImpl;
import it.unibz.inf.ontop.dbschema.impl.ImmutableMetadataImpl;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.io.File;
import java.io.IOException;


public class RDBMetadataLoaderImpl implements RDBMetadataLoader {

    private final File dbMetadataFile;

    @Inject
    public RDBMetadataLoaderImpl(File dbMetadataFile) {
        this.dbMetadataFile = dbMetadataFile;
    }

    @Override
    public ImmutableMetadata loadAndDeserialize() throws MetadataExtractionException, IOException {

        try {
            // create object mapper instance
            ObjectMapper mapper = new ObjectMapper();
            // convert JSON file to dbschema
            ImmutableMetadata metadata = mapper.readValue(dbMetadataFile, ImmutableMetadataImpl.class);
            return metadata;
        }
        catch (JsonProcessingException e) {
            throw new MetadataExtractionException("problem with JSON processing.\n" + e);
        }
    }

    public DBParameters loadDBParameters() throws MetadataExtractionException, IOException {

        DBParameters dbParameters0 = new ObjectMapper()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .readerFor(BasicDBParametersImpl.class)
            .readValue(dbMetadataFile);

        DBParameters dbParameters = new BasicDBParametersImpl(dbParameters0.getDriverName(),
            dbParameters0.getDriverVersion(),
            dbParameters0.getDbmsProductName(),
            dbParameters0.getDbmsVersion(),
            dbParameters0.getQuotedIDFactory(),
            dbParameters0.getDBTypeFactory());

        return dbParameters;
    }
}
