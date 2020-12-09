package it.unibz.inf.ontop.spec.dbschema.tools.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.ImmutableMetadata;
import it.unibz.inf.ontop.dbschema.impl.BasicDBParametersImpl;
import it.unibz.inf.ontop.dbschema.impl.ImmutableMetadataImpl;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.dbschema.tools.RDBMetadataLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class RDBMetadataLoaderImpl implements RDBMetadataLoader {

    private final TypeFactory typeFactory;

    @Inject
    private RDBMetadataLoaderImpl(TypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    /*@Override
    public ImmutableMetadata loadAndDeserialize(File dbMetadataFile) throws MetadataExtractionException, IOException {

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
    }*/

    @Override
    public List<ImmutableMetadataImpl> loadAndDeserialize(File dbMetadataFile) throws MetadataExtractionException, IOException {

        try {
            ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new GuavaModule())
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
            CollectionType javatype = objectMapper.getTypeFactory().constructCollectionType(List.class,ImmutableMetadataImpl.class);
            List<ImmutableMetadataImpl> metadata = objectMapper.readValue(dbMetadataFile, javatype);
            return metadata;

        }
        catch (JsonProcessingException e) {
            throw new MetadataExtractionException("problem with JSON processing.\n" + e);
        }
    }



    /*public DBParameters loadDBParameters() throws MetadataExtractionException, IOException {

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
    }*/
}
